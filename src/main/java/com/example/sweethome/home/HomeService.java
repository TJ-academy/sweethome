package com.example.sweethome.home;

import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;
import com.example.sweethome.util.FileHandlerService;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 숙소 관련 비즈니스 로직을 처리하는 서비스 클래스
 * HomeWriteDTO를 사용하여 폼 데이터와 파일을 처리
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HomeService {

    private final HomeRepository homeRepository;
    private final UserRepository userRepository; 
    private final HomePhotoRepository homePhotoRepository;

    private final AccommodationOptionRepository accommodationOptionRepository;
    private final OptionRepository optionRepository;
    
    private final HashtagRepository hashtagRepository;  

    private final FileHandlerService fileHandlerService;

    /**
     * 옵션 데이터를 조회하고 그룹별로 맵핑하여 반환합니다.
     * 컨트롤러에서 showWriteForm 메서드가 호출합니다.
     * @return Map<String, List<Option>> 옵션 그룹 이름(String)을 키로, 해당 그룹의 Option 리스트를 값으로 가집니다.
     */
    public Map<String, List<Option>> getGroupedOptions() {
        // 1. 모든 옵션 데이터를 조회합니다.
        List<Option> allOptions = optionRepository.findAll();

        // 2. ⭐ 수정: Option 엔티티의 실제 그룹 필드인 optionGroup을 기준으로 그룹화합니다.
        return allOptions.stream()
                .collect(Collectors.groupingBy(Option::getOptionGroup));
    }


    //숙소 등록 처리 메서드
    @Transactional
    public int registerHome(HomeWriteDTO dto) {

        // 1. Host (User) 엔티티 조회 및 유효성 검사
        // DTO의 hostId는 String (이메일) 타입이며, UserRepository는 String Key를 사용
        User host = userRepository.findById(dto.getHostId()) 
                .orElseThrow(() -> new RuntimeException("호스트 사용자(ID: " + dto.getHostId() + ")를 찾을 수 없습니다."));

        // 2. 파일 저장 및 파일 경로 확보 (썸네일)
        String thumbnailUrl = fileHandlerService.saveFile(dto.getThumbnail());

        // 3. DTO -> Home 엔티티 변환 (비즈니스 로직 적용)
        Home newHome = Home.builder()
                .host(host)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .address(dto.getAddress() + " " + dto.getDetailAddress()) // 주소와 상세 주소를 병합하여 저장
                .costBasic(dto.getCostBasic())
                .costExpen(dto.getCostExpen())
                .homeType(HomeType.valueOf(dto.getHomeType())) // String -> Enum 변환
                .thumbnail(thumbnailUrl) // 저장된 파일 경로 사용
                .maxPeople(dto.getMaxPeople())
                .room(dto.getRoom())
                
                // bath와 bed가 DTO에서 int 타입이므로 null 가능성은 없지만, 방어적 코드 유지
                .bath(Optional.ofNullable(dto.getBath()).orElse(0))
                .bed(Optional.ofNullable(dto.getBed()).orElse(0))

                .checkIn(convertTimeToInt(dto.getCheckIn())) // 시간 문자열 ("HH:mm") -> 시간 정수 (int)로 변환
                .checkOut(convertTimeToInt(dto.getCheckOut()))
                .build();

        // 4. Home 엔티티 저장
        Home savedHome = homeRepository.save(newHome);

        // 5. 숙소 사진 (HomePhoto) 처리
        processHomePhotos(savedHome, dto);

        // 옵션 처리 - DTO의 실제 getter 이름인 getOptionIds()를 사용
        processAccommodationOptions(savedHome, dto.getOptionIds());

        // 6. 해시태그 처리
        processHashtag(savedHome, dto);
        
        return savedHome.getIdx();
    }

    //옵션 저장 메서드
    private void processAccommodationOptions(Home home, List<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) {
            return; // 선택된 옵션이 없으면 종료
        }
        
        // 옵션 ID 목록 (Long)을 Option 엔티티 객체 목록으로 변환
        List<Option> options = selectedOptionIds.stream()
            // Option ID가 int 타입이므로 Long을 int로 변환하여 findById 호출
            .map(optionId -> optionRepository.findById(optionId.intValue()) 
                .orElseThrow(() -> new RuntimeException("옵션 ID(" + optionId + ")를 찾을 수 없습니다.")))
            .collect(Collectors.toList());

        // AccommodationOption 엔티티 리스트 생성 및 저장
        List<AccommodationOption> optionsToSave = options.stream()
            .map(option -> AccommodationOption.builder()
                .home(home)            // Home 엔티티 객체 참조
                .option(option)        // Option 엔티티 객체 참조
                .exist(true)           // 옵션이 존재함을 표시
                .build())
            .collect(Collectors.toList());

        accommodationOptionRepository.saveAll(optionsToSave);
    }

    //이미지 저장 메서드
 // 숙소 이미지 저장 메서드 (수정 버전)
    private void processHomePhotos(Home home, HomeWriteDTO dto) {
        List<MultipartFile> imageFiles = dto.getHomeImages();

        if (imageFiles == null || imageFiles.isEmpty()) {
            return; // 업로드된 이미지가 없으면 종료
        }

        // 파일 저장 후 경로 리스트 생성
        List<String> filePaths = imageFiles.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(fileHandlerService::saveFile)
                .collect(Collectors.toList());

        if (!filePaths.isEmpty()) {
            // 최대 5장까지만 저장 (DB 컬럼 수 제한 고려)
            HomePhoto homePhoto = HomePhoto.builder()
                    .home(home)
                    .imgOne(filePaths.size() > 0 ? filePaths.get(0) : null)
                    .imgTwo(filePaths.size() > 1 ? filePaths.get(1) : null)
                    .imgThree(filePaths.size() > 2 ? filePaths.get(2) : null)
                    .imgFour(filePaths.size() > 3 ? filePaths.get(3) : null)
                    .imgFive(filePaths.size() > 4 ? filePaths.get(4) : null)
                    .imgSix(filePaths.size() > 5 ? filePaths.get(5) : null)
                    .imgSeven(filePaths.size() > 6 ? filePaths.get(6) : null)
                    .imgEight(filePaths.size() > 7 ? filePaths.get(7) : null)
                    .imgNine(filePaths.size() > 8 ? filePaths.get(8) : null)
                    .imgTen(filePaths.size() > 9 ? filePaths.get(9) : null)
                    .build();

            homePhotoRepository.save(homePhoto);
        }
    }

    //시간(time) -> 정수(int) 변환 로직
    private int convertTimeToInt(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return 0; // 또는 기본값 설정
        }

        // LocalTime.parse를 사용하여 시간만 추출 (int로 반환)
        return LocalTime.parse(timeStr).getHour();
    }
    
    //해시태그 저장 메서드
    @Transactional
    private void processHashtag(Home home, HomeWriteDTO dto) {
    	//DTO의 boolean 값 -> Hashtag 엔티티로 매핑
    	Hashtag hashtag = Hashtag.builder()
    			.home(home)
    			.wifi(dto.isWifi())
                .tv(dto.isTv())
                .kitchen(dto.isKitchen())
                .freePark(dto.isFreePark())
                .selfCheckin(dto.isSelfCheckin())
                .coldWarm(dto.isColdWarm())
                .petFriendly(dto.isPetFriendly())
                .barrierFree(dto.isBarrierFree())
                .elevator(dto.isElevator())
                .build();
    	
    	//Hashtag 엔티티 저장
    	hashtagRepository.save(hashtag);
    }
}
