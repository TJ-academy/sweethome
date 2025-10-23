package com.example.sweethome.home;

import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;
import com.example.sweethome.util.FileHandlerService;
import com.example.sweethome.wishlist.WishlistRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import java.util.Set;    
import java.util.HashSet;  

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
    private final WishlistRepository wishlistRepository;

    /**
     * ✅ 전체 숙소 목록 조회 (좋아요 개수 포함)
     */
    public List<HomeResponseDto> getHomeListWithLikeCounts() {
        List<Home> homes = homeRepository.findAll();
        List<Object[]> likeCounts = wishlistRepository.countWishlistsByHome();

        Map<Integer, Long> likeCountMap = likeCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (Integer) arr[0],
                        arr -> (Long) arr[1]
                ));

        return homes.stream()
                .map(home -> new HomeResponseDto(
                        home,
                        likeCountMap.getOrDefault(home.getIdx(), 0L)
                ))
                .collect(Collectors.toList());
    }

    /**
     * ✅ 여행지(location) 기준 숙소 검색
     * @param keyword 검색어 (예: "서울", "제주")
     * @return location에 keyword가 포함된 숙소 목록 (좋아요 개수 포함)
     */
    public List<HomeResponseDto> searchHomesByLocation(String keyword) {
        // 1️⃣ location 컬럼 기준으로 LIKE 검색
        List<Home> homes = homeRepository.findByLocationContainingIgnoreCase(keyword);

        // 2️⃣ 각 숙소의 좋아요 개수 조회
        List<Object[]> likeCounts = wishlistRepository.countWishlistsByHome();

        Map<Integer, Long> likeCountMap = likeCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (Integer) arr[0],
                        arr -> (Long) arr[1]
                ));

        // 3️⃣ Home + 좋아요 결합 → DTO 반환
        return homes.stream()
                .map(home -> new HomeResponseDto(
                        home,
                        likeCountMap.getOrDefault(home.getIdx(), 0L)
                ))
                .collect(Collectors.toList());
    }

    // 🔽 이하 기존 메서드 그대로 유지 🔽

    public Map<String, List<Option>> getGroupedOptions() {
        List<Option> allOptions = optionRepository.findAll();
        return allOptions.stream()
                .collect(Collectors.groupingBy(Option::getOptionGroup));
    }

    @Transactional
    public int registerHome(HomeWriteDTO dto) {
        User host = userRepository.findById(dto.getHostId())
                .orElseThrow(() -> new RuntimeException("호스트 사용자(ID: " + dto.getHostId() + ")를 찾을 수 없습니다."));

        String thumbnailUrl = fileHandlerService.saveFile(dto.getThumbnail());

        Home newHome = Home.builder()
                .host(host)
                .title(dto.getTitle())
                .description(dto.getDescription())
                .location(dto.getLocation())
                .address(dto.getAddress() + " " + dto.getDetailAddress())
                .costBasic(dto.getCostBasic())
                .costExpen(dto.getCostExpen())
                .homeType(HomeType.valueOf(dto.getHomeType()))
                .thumbnail(thumbnailUrl)
                .maxPeople(dto.getMaxPeople())
                .room(dto.getRoom())
                .bath(Optional.ofNullable(dto.getBath()).orElse(0))
                .bed(Optional.ofNullable(dto.getBed()).orElse(0))
                .checkIn(convertTimeToInt(dto.getCheckIn()))
                .checkOut(convertTimeToInt(dto.getCheckOut()))
                .build();

        Home savedHome = homeRepository.save(newHome);
        processHomePhotos(savedHome, dto);
        processAccommodationOptions(savedHome, dto.getOptionIds());
        processHashtag(savedHome, dto);

        return savedHome.getIdx();
    }

    private void processAccommodationOptions(Home home, List<Long> selectedOptionIds) {
        if (selectedOptionIds == null || selectedOptionIds.isEmpty()) return;

        List<Option> options = selectedOptionIds.stream()
                .map(optionId -> optionRepository.findById(optionId.intValue())
                        .orElseThrow(() -> new RuntimeException("옵션 ID(" + optionId + ")를 찾을 수 없습니다.")))
                .collect(Collectors.toList());

        List<AccommodationOption> optionsToSave = options.stream()
                .map(option -> AccommodationOption.builder()
                        .home(home)
                        .option(option)
                        .exist(true)
                        .build())
                .collect(Collectors.toList());

        accommodationOptionRepository.saveAll(optionsToSave);
    }

    private void processHomePhotos(Home home, HomeWriteDTO dto) {
        List<MultipartFile> imageFiles = dto.getHomeImages();
        if (imageFiles == null || imageFiles.isEmpty()) return;

        List<String> filePaths = imageFiles.stream()
                .filter(file -> file != null && !file.isEmpty())
                .map(fileHandlerService::saveFile)
                .collect(Collectors.toList());

        if (!filePaths.isEmpty()) {
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

    private int convertTimeToInt(String timeStr) {
        if (timeStr == null || timeStr.isEmpty()) return 0;
        return LocalTime.parse(timeStr).getHour();
    }

    @Transactional
    private void processHashtag(Home home, HomeWriteDTO dto) {
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

        hashtagRepository.save(hashtag);
    }
    
    public List<Home> getHomesByIds(List<Long> ids) {
        // JpaRepository의 기본 메서드인 findAllById(Iterable<ID>)를 사용하여 구현
        // HomeRepository에 정의된 findByIds(List<Long>) 등의 커스텀 메서드가 있다면 그것을 사용해도 됩니다.
        // 여기서는 Home의 PK가 Integer이지만, Long으로 넘어왔으므로, Integer 리스트로 변환하여 조회합니다.
        
        List<Integer> integerIds = ids.stream()
            .map(Long::intValue)
            .collect(Collectors.toList());
            
        return homeRepository.findAllById(integerIds);
    }
        
    /**
     * ✅ 숙소 비교 대상의 상세 정보를 모두 조회 (기본정보, 좋아요 개수, 옵션)
     * @param homeIds Long 타입의 숙소 ID 리스트 (URL 쿼리 파라미터로 받은 값)
     * @return 상세 비교 데이터 DTO 리스트
     */
    public List<CompareHomeDetail> getCompareHomeDetails(List<Long> homeIds) {
        
        // Home ID를 Integer 리스트로 변환 (DB PK 타입에 맞춤)
        List<Integer> integerIds = homeIds.stream()
                .map(Long::intValue)
                .collect(Collectors.toList());

        // 1. Home 기본 정보 조회
        List<Home> homes = homeRepository.findAllById(integerIds);

        // 2. Wishlist (좋아요 개수) 조회
        // 이 메서드는 WishlistRepository에 정의되어 있어야 합니다.
        List<Object[]> likeCounts = wishlistRepository.countWishlistsByHomeIds(integerIds);
        Map<Integer, Long> likeCountMap = likeCounts.stream()
                .collect(Collectors.toMap(
                        arr -> (Integer) arr[0], // Home idx
                        arr -> (Long) arr[1]     // Like Count
                ));

        // 3. AccommodationOption (옵션) 조회
        List<AccommodationOption> accOptions = accommodationOptionRepository.findOptionsByHomeIds(integerIds);
        
        // Home ID별로 옵션 그룹 및 이름을 매핑
        Map<Integer, Map<String, List<String>>> homeOptionsMap = accOptions.stream()
                .collect(Collectors.groupingBy(
                        ao -> ao.getHome().getIdx(), // 숙소 ID로 1차 그룹핑
                        Collectors.groupingBy(
                                ao -> ao.getOption().getOptionGroup(), // 옵션 그룹으로 2차 그룹핑
                                Collectors.mapping(
                                        ao -> ao.getOption().getOptionName(), // 옵션 이름만 리스트로 매핑
                                        Collectors.toList()
                                )
                        )
                ));

        // 4. 모든 데이터를 DTO로 조합 (Builder 패턴 사용)
        return homes.stream()
                .map(home -> 
                    CompareHomeDetail.builder()
                        .idx(home.getIdx())
                        .title(home.getTitle())
                        .thumbnail(home.getThumbnail())
                        .costBasic(home.getCostBasic())
                        .costExpen(home.getCostExpen())
                        .maxPeople(home.getMaxPeople())
                        .room(home.getRoom())
                        .bath(home.getBath())
                        .bed(home.getBed())
                        // 나머지 필드 채우기
                        .likeCount(likeCountMap.getOrDefault(home.getIdx(), 0L))
                        .groupedOptions(homeOptionsMap.getOrDefault(home.getIdx(), Map.of()))
                        .build()
                )
                .collect(Collectors.toList());
    }
    
    /**
     * ✅ 모든 비교 대상 숙소의 옵션 그룹 이름을 추출하고 정렬된 단일 리스트로 반환합니다.
     * Thymeleaf 템플릿에서 옵션 행을 동적으로 생성하는 기준이 됩니다.
     * @param details CompareHomeDetail 리스트
     * @return 모든 숙소가 포함하는 고유한 옵션 그룹 이름 리스트 (알파벳 순 정렬)
     */
    public List<String> getAllUniqueOptionGroups(List<CompareHomeDetail> details) {
        // 1. 모든 숙소의 모든 옵션 그룹을 하나의 Set에 모아 중복을 제거합니다.
        Set<String> allGroups = new HashSet<>();
        for (CompareHomeDetail detail : details) {
            allGroups.addAll(detail.getGroupedOptions().keySet());
        }
        
        // 2. Set을 List로 변환하고 정렬합니다.
        return allGroups.stream()
                .sorted() // 그룹 이름을 알파벳 순으로 정렬
                .collect(Collectors.toList());
    }
    
    
}
