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
    
    // ⭐ 옵션 매핑 Repository
    private final AccommodationOptionRepository accommodationOptionRepository;
    
    // ⭐ Option 객체 조회를 위한 Repository 주입
    private final OptionRepository optionRepository; 
    
    // 로컬 파일 핸들러 구현체가 주입됩니다.
    private final FileHandlerService fileHandlerService; 

    /**
     * 숙소 등록 프로세스를 처리
     * @param dto HomeWriteDTO (폼 데이터 및 파일 포함)
     * @return 등록된 Home 엔티티의 ID (idx)
     */
    @Transactional
    public int registerHome(HomeWriteDTO dto) { 
        
        // 1. Host (User) 엔티티 조회 및 유효성 검사
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
                // 주소와 상세 주소를 병합하여 저장
                .address(dto.getAddress() + " " + dto.getDetailAddress())
                .costBasic(dto.getCostBasic())
                .costExpen(dto.getCostExpen())
                .homeType(HomeType.valueOf(dto.getHomeType())) // String -> Enum 변환
                .thumbnail(thumbnailUrl) // 저장된 파일 경로 사용
                .maxPeople(dto.getMaxPeople())
                .room(dto.getRoom())
                .bath(dto.getBath())
                .bed(dto.getBed())
                // 시간 문자열 ("HH:mm") -> 시간 정수 (int)로 변환
                .checkIn(convertTimeToInt(dto.getCheckIn()))
                .checkOut(convertTimeToInt(dto.getCheckOut()))
                .build();
        
        // 4. Home 엔티티 저장
        Home savedHome = homeRepository.save(newHome);

        // 5. 숙소 사진 (HomePhoto) 처리
        processHomePhotos(savedHome, dto); 
        
        // 6. 옵션 매핑 (AccommodationOption) 처리
        // ⭐ 수정된 부분: HomeWriteDTO의 getter 이름을 getSelectedOptions()로 수정했습니다.
        processAccommodationOptions(savedHome, dto.getSelectedOptions());

        return savedHome.getIdx();
    }
    
    /**
     * DTO에서 넘어온 선택된 옵션 ID들(List<Integer>)을 Option 객체로 변환하여 AccommodationOption 테이블에 저장합니다.
     * @param home 등록된 Home 엔티티
     * @param selectedOptions 폼에서 넘어온 선택된 옵션 ID 목록
     */
    private void processAccommodationOptions(Home home, List<Integer> selectedOptions) {
        // ⭐ 수정된 부분: 파라미터 이름을 selectedOptionIds에서 selectedOptions로 통일하여 가독성을 높였습니다.
        if (selectedOptions == null || selectedOptions.isEmpty()) {
            return; // 선택된 옵션이 없으면 종료
        }
        
        // 옵션 ID 목록을 Option 엔티티 객체 목록으로 변환
        List<Option> options = selectedOptions.stream()
            .map(optionId -> optionRepository.findById(optionId)
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
    
    /**
     * HomeWriteDTO에 있는 10개의 이미지 파일을 처리하고 HomePhoto 엔티티를 저장.
     */
    private void processHomePhotos(Home home, HomeWriteDTO dto) { // DTO 이름 수정
        // DTO의 10개 이미지 필드를 리스트로 모음
        List<MultipartFile> imageFiles = Arrays.asList(
            dto.getImgOne(), dto.getImgTwo(), dto.getImgThree(), dto.getImgFour(), 
            dto.getImgFive(), dto.getImgSix(), dto.getImgSeven(), dto.getImgEight(), 
            dto.getImgNine(), dto.getImgTen()
        );

        // 유효한 파일(널이 아니거나 비어있지 않은)만 필터링하여 저장
        List<String> filePaths = imageFiles.stream()
            .filter(file -> file != null && !file.isEmpty())
            .map(fileHandlerService::saveFile) // 파일 저장 및 경로 리턴
            .collect(Collectors.toList());

        // HomePhoto 엔티티에 파일 경로를 설정하고 저장
        if (!filePaths.isEmpty()) {
            HomePhoto homePhoto = HomePhoto.builder()
                    .home(home)
                    // 최대 10개의 컬럼에 파일 경로를 순서대로 매핑
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
    
    /**
     * 시간 문자열 ("HH:mm")을 시간 정수 (int)로 변환 (예: "15:00" -> 15)
     */
    private int convertTimeToInt(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) {
            return 0; // 또는 기본값 설정
        }
        // LocalTime.parse를 사용하여 시간만 추출 (int로 반환)
        return LocalTime.parse(timeStr).getHour();
    }
}
