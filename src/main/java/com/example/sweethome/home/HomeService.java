package com.example.sweethome.home;

import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.review.ReviewRepository;
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
import java.util.ArrayList;
import java.util.Arrays;
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
    private final ReviewRepository reviewRepository;
    private final ReservationRepository reservationRepository;

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
    
    public List<HomeResponseDto> searchHomesByLocationAndMaxPeople(String keyword, int adults) {
        // 1️⃣ location 및 maxPeople 필터링 조건을 적용하여 Home 목록을 가져옵니다.
        List<Home> homes = homeRepository.findByLocationContainingIgnoreCaseAndMaxPeopleGreaterThanEqual(keyword, adults);

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
    
    public Home getHomeById(int homeIdx) {
        return homeRepository.findById(homeIdx).orElse(null);
    }
    
    //수정폼으로 데이터보내는 메서드
    @Transactional(readOnly = true)
    public HomeWriteDTO getHomeWriteDTOForUpdate(Home home) {
        if (home == null) return null;

        // 1️ 기존 옵션 ID 리스트 조회
        List<Long> optionIds = accommodationOptionRepository.findByHome(home).stream()
                .filter(AccommodationOption::isExist)
                .map(ao -> Long.valueOf(ao.getOption().getOptionId()))
                .collect(Collectors.toList());

        // 2️ 해시태그 조회
        Hashtag hashtag = hashtagRepository.findByHome(home).orElse(null);
        
        // 3️ HomePhoto 엔티티 조회 (사진 경로를 가져오기 위함)
        HomePhoto homePhoto = homePhotoRepository.findByHome(home);

        // 4️ DTO 생성 및 기본 데이터 매핑
        HomeWriteDTO dto = new HomeWriteDTO();
        dto.setIdx(home.getIdx());
        dto.setHostId(home.getHost().getEmail());
        dto.setTitle(home.getTitle());
        dto.setDescription(home.getDescription());
        dto.setLocation(home.getLocation());

        // ⭐️ 주소 분리 로직 수정 시작 ⭐️
        String fullAddress = home.getAddress() != null ? home.getAddress().trim() : "";
        String basicAddress = fullAddress; // 기본 주소
        String detailAddress = "";         // 상세 주소

        // 공백을 기준으로 문자열을 분리합니다.
        String[] parts = fullAddress.split("\\s+");
        
        // 최소 두 개 이상의 단어가 있어야 상세 주소 분리가 의미 있음
        if (parts.length > 1) {
            // 상세 주소: 가장 마지막 단어
            detailAddress = parts[parts.length - 1];
            
            // 기본 주소: 나머지 단어들을 공백으로 다시 연결
            basicAddress = String.join(" ", Arrays.copyOf(parts, parts.length - 1));
        } else {
            // 공백이 없거나 단어 하나면 전체를 기본 주소로 유지
            basicAddress = fullAddress;
            detailAddress = "";
        }

        dto.setAddress(basicAddress);
        dto.setDetailAddress(detailAddress);
        // ⭐️ 주소 분리 로직 수정 끝 ⭐️

        dto.setCostBasic(home.getCostBasic());
        dto.setCostExpen(home.getCostExpen());
        dto.setHomeType(home.getHomeType() != null ? home.getHomeType().name() : "");
        dto.setThumbnail(null); // MultipartFile은 null 처리

        // ⭐️ 5️⃣ 기존 이미지 경로 로딩 ⭐️
        // 썸네일 경로: Home 엔티티의 필드를 바로 사용
        dto.setCurrentThumbnailPath(home.getThumbnail());

        // 숙소 사진 경로: HomePhoto 엔티티의 개별 필드를 수동으로 읽어 리스트에 추가
        List<String> imagePaths = new ArrayList<>();
        if (homePhoto != null) {
            // HomePhoto 엔티티의 필드들을 순서대로 리스트에 추가
            if (homePhoto.getImgOne() != null && !homePhoto.getImgOne().isEmpty()) { imagePaths.add(homePhoto.getImgOne()); }
            if (homePhoto.getImgTwo() != null && !homePhoto.getImgTwo().isEmpty()) { imagePaths.add(homePhoto.getImgTwo()); }
            if (homePhoto.getImgThree() != null && !homePhoto.getImgThree().isEmpty()) { imagePaths.add(homePhoto.getImgThree()); }
            if (homePhoto.getImgFour() != null && !homePhoto.getImgFour().isEmpty()) { imagePaths.add(homePhoto.getImgFour()); }
            if (homePhoto.getImgFive() != null && !homePhoto.getImgFive().isEmpty()) { imagePaths.add(homePhoto.getImgFive()); }
            if (homePhoto.getImgSix() != null && !homePhoto.getImgSix().isEmpty()) { imagePaths.add(homePhoto.getImgSix()); }
            if (homePhoto.getImgSeven() != null && !homePhoto.getImgSeven().isEmpty()) { imagePaths.add(homePhoto.getImgSeven()); }
            if (homePhoto.getImgEight() != null && !homePhoto.getImgEight().isEmpty()) { imagePaths.add(homePhoto.getImgEight()); }
            if (homePhoto.getImgNine() != null && !homePhoto.getImgNine().isEmpty()) { imagePaths.add(homePhoto.getImgNine()); }
            if (homePhoto.getImgTen() != null && !homePhoto.getImgTen().isEmpty()) { imagePaths.add(homePhoto.getImgTen()); }
        }
        dto.setCurrentHomeImagePaths(imagePaths);
        // --------------------------------------------------------

        dto.setMaxPeople(home.getMaxPeople());
        dto.setRoom(home.getRoom());
        dto.setBath(home.getBath() != null ? home.getBath() : 0);
        dto.setBed(home.getBed() != null ? home.getBed() : 0);
        dto.setCheckIn(String.format("%02d:00", home.getCheckIn()));
        dto.setCheckOut(String.format("%02d:00", home.getCheckOut()));
        dto.setOptionIds(optionIds);

        // 해시태그 매핑
        if (hashtag != null) {
            dto.setWifi(hashtag.isWifi());
            dto.setTv(hashtag.isTv());
            dto.setKitchen(hashtag.isKitchen());
            dto.setFreePark(hashtag.isFreePark());
            dto.setSelfCheckin(hashtag.isSelfCheckin());
            dto.setColdWarm(hashtag.isColdWarm());
            dto.setPetFriendly(hashtag.isPetFriendly());
            dto.setBarrierFree(hashtag.isBarrierFree());
            dto.setElevator(hashtag.isElevator());
        }

        return dto;
    }
    
    @Transactional // 수정 트랜잭션
    public void updateHome(int homeIdx, HomeWriteDTO dto, User currentUser) {
        
        // 1. Home 엔티티 조회 및 권한 확인
        Home home = homeRepository.findById(homeIdx)
            .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 숙소입니다."));
        
        // 권한 확인: 숙소의 호스트와 현재 사용자가 일치하는지 확인
        if (!home.getHost().getEmail().equals(currentUser.getEmail())) {
            throw new IllegalArgumentException("숙소 수정 권한이 없습니다.");
        }

        // 2. 파일 처리
        
        // 2-1. 썸네일 처리 (변경 없음: 새 파일 없으면 기존 경로 유지)
        String newThumbnailPath = home.getThumbnail(); 
        if (dto.getThumbnail() != null && !dto.getThumbnail().isEmpty()) {
            newThumbnailPath = fileHandlerService.saveFile(dto.getThumbnail()); 
        } else if (dto.getCurrentThumbnailPath() != null) {
            newThumbnailPath = dto.getCurrentThumbnailPath();
        } 
        
        // 2-2. 숙소 사진 처리
        List<String> finalHomePhotoPaths; // ⭐️ 최종적으로 DB에 저장할 사진 경로 리스트 ⭐️
        
        // 새 이미지가 업로드되었는지 확인
        boolean hasNewImages = dto.getHomeImages() != null && dto.getHomeImages().stream().anyMatch(f -> f != null && !f.isEmpty());
        
        if (hasNewImages) {
            // 새 파일이 있으면 새로 저장하여 최종 경로 리스트 구성
            List<String> uploadedPaths = new ArrayList<>();
            for (MultipartFile file : dto.getHomeImages()) {
                if (file != null && !file.isEmpty()) {
                    uploadedPaths.add(fileHandlerService.saveFile(file));
                }
            }
            finalHomePhotoPaths = uploadedPaths;
        } else {
            // ⭐️ 새 파일이 없으면 DTO에 담겨온 기존 경로를 그대로 최종 경로로 사용 ⭐️
            // (DTO의 currentHomeImagePaths 필드가 폼에서 Hidden Field로 넘어와야 함)
            finalHomePhotoPaths = dto.getCurrentHomeImagePaths() != null ? dto.getCurrentHomeImagePaths() : new ArrayList<>();
        }
        
        // 3. Home 엔티티 업데이트
        home.setTitle(dto.getTitle());
        home.setDescription(dto.getDescription());
        home.setLocation(dto.getLocation());
        
        // 주소 재조합 (기본주소 + 상세주소)
        String fullAddress = dto.getAddress().trim() + (dto.getDetailAddress().isEmpty() ? "" : " " + dto.getDetailAddress().trim());
        home.setAddress(fullAddress);
        
        home.setCostBasic(dto.getCostBasic());
        home.setCostExpen(dto.getCostExpen());
        if (dto.getHomeType() != null && !dto.getHomeType().isEmpty()) {
            home.setHomeType(HomeType.valueOf(dto.getHomeType()));
        } else {
            home.setHomeType(null);
        }

        home.setThumbnail(newThumbnailPath); 
        home.setMaxPeople(dto.getMaxPeople());
        home.setRoom(dto.getRoom());
        home.setBath(dto.getBath());
        home.setBed(dto.getBed());
        
        // 시간 포맷팅 및 변환 로직
        try {
            home.setCheckIn(LocalTime.parse(dto.getCheckIn()).getHour()); 
            home.setCheckOut(LocalTime.parse(dto.getCheckOut()).getHour());
        } catch (Exception e) {
            // 오류 시 기본값 설정 (혹은 예외를 던지거나 유효성 검사 필요)
            home.setCheckIn(15); 
            home.setCheckOut(11);
        }

        homeRepository.save(home); // Home 엔티티 저장 (업데이트)


        // 4. HomePhoto 엔티티 업데이트 (숙소 사진)
        HomePhoto homePhoto = homePhotoRepository.findByHome(home);
        if (homePhoto == null) {
            homePhoto = HomePhoto.builder().home(home).build();
        }

        // ⭐️⭐️⭐️ finalHomePhotoPaths 리스트를 사용하여 필드 업데이트 (NULL 초기화 방지) ⭐️⭐️⭐️
        // 리스트 크기에 맞춰 할당하고, 리스트에 포함되지 않은 필드는 명시적으로 NULL 처리합니다.
        homePhoto.setImgOne(finalHomePhotoPaths.size() > 0 ? finalHomePhotoPaths.get(0) : null);
        homePhoto.setImgTwo(finalHomePhotoPaths.size() > 1 ? finalHomePhotoPaths.get(1) : null);
        homePhoto.setImgThree(finalHomePhotoPaths.size() > 2 ? finalHomePhotoPaths.get(2) : null);
        homePhoto.setImgFour(finalHomePhotoPaths.size() > 3 ? finalHomePhotoPaths.get(3) : null);
        homePhoto.setImgFive(finalHomePhotoPaths.size() > 4 ? finalHomePhotoPaths.get(4) : null);
        homePhoto.setImgSix(finalHomePhotoPaths.size() > 5 ? finalHomePhotoPaths.get(5) : null);
        homePhoto.setImgSeven(finalHomePhotoPaths.size() > 6 ? finalHomePhotoPaths.get(6) : null);
        homePhoto.setImgEight(finalHomePhotoPaths.size() > 7 ? finalHomePhotoPaths.get(7) : null);
        homePhoto.setImgNine(finalHomePhotoPaths.size() > 8 ? finalHomePhotoPaths.get(8) : null);
        homePhoto.setImgTen(finalHomePhotoPaths.size() > 9 ? finalHomePhotoPaths.get(9) : null);
        
        homePhotoRepository.save(homePhoto); // HomePhoto 업데이트


        // 5. AccommodationOption 업데이트 (옵션)
        
        // 기존 옵션 삭제
        List<AccommodationOption> existingOptions = accommodationOptionRepository.findByHome(home);
        accommodationOptionRepository.deleteAll(existingOptions);

        // 새 옵션 등록
        if (dto.getOptionIds() != null) {
            List<AccommodationOption> newOptions = dto.getOptionIds().stream()
                .map(optionId -> {
                    Option option = optionRepository.findById(optionId.intValue())
                        .orElseThrow(() -> new IllegalArgumentException("유효하지 않은 옵션 ID입니다: " + optionId));
                    return AccommodationOption.builder()
                            .home(home)
                            .option(option)
                            .exist(true)
                            .build();
                })
                .collect(Collectors.toList());
            accommodationOptionRepository.saveAll(newOptions);
        }


        // 6. Hashtag 엔티티 업데이트
        Hashtag hashtag = hashtagRepository.findByHome(home).orElse(null);
        if (hashtag == null) {
            hashtag = new Hashtag();
            hashtag.setHome(home);
        }

        hashtag.setWifi(dto.isWifi());
        hashtag.setTv(dto.isTv());
        hashtag.setKitchen(dto.isKitchen());
        hashtag.setFreePark(dto.isFreePark());
        hashtag.setSelfCheckin(dto.isSelfCheckin());
        hashtag.setColdWarm(dto.isColdWarm());
        hashtag.setPetFriendly(dto.isPetFriendly());
        hashtag.setBarrierFree(dto.isBarrierFree());
        hashtag.setElevator(dto.isElevator());
        
        hashtagRepository.save(hashtag); // Hashtag 업데이트
    }
    
    //숙소삭제
    @Transactional 
    public void deleteHome(int homeIdx, User userProfile) {
        
        // 1. Home 엔티티 조회 및 존재 여부 확인
        Home home = homeRepository.findById(homeIdx)
                .orElseThrow(() -> new IllegalArgumentException("해당 ID의 숙소를 찾을 수 없습니다."));

        // 2. 🔑 호스트 권한 확인
        if (!home.getHost().getEmail().equals(userProfile.getEmail())) {
            throw new IllegalStateException("숙소 삭제 권한이 없습니다.");
        }

        // 3. 외래 키 제약 조건이 있는 테이블부터 역순으로 삭제 (순서 중요!)
        
        // 3-1. Review 테이블 삭제 (home_idx 컬럼 참조)
        // 💡 ReviewRepository에 deleteByHome_Idx(int homeIdx) 메서드가 필요합니다.
        reviewRepository.deleteByHome_Idx(homeIdx); 

        // 3-2. Reservation 테이블 삭제 (reserved_home 컬럼 참조)
        // 💡 ReservationRepository에 deleteByReservedHome_Idx(int homeIdx) 메서드가 필요합니다.
        reservationRepository.deleteByReservedHome_Idx(homeIdx); 

        // 3-3. Wishlist 테이블 삭제 (wisilist폴더)
        // 💡 WishlistRepository에 deleteByHome_Idx(int homeIdx) 메서드가 필요합니다.
        wishlistRepository.deleteByHome_Idx(homeIdx); 

        // 3-4. Accommodation_Option 테이블 삭제 (home_idx 컬럼 참조)
        // 💡 AccommodationOptionRepository에 deleteByHome_Idx(int homeIdx) 메서드가 필요합니다.
        accommodationOptionRepository.deleteByHome_Idx(homeIdx); 

        // 3-5. Home_Photo 테이블 삭제 (home_idx 컬럼 참조)
        // 💡 HomePhotoRepository에 deleteByHome_Idx(int homeIdx) 메서드가 필요합니다.
        // (선택 사항: 파일 삭제 로직을 FileHandlerService를 통해 먼저 실행할 수 있습니다.)
        homePhotoRepository.deleteByHome_Idx(homeIdx); 

        // 3-6. Hashtag 테이블 삭제 (home_idx 컬럼 참조)
        // 💡 HashtagRepository에 deleteByHome_Idx(int homeIdx) 메서드가 필요합니다.
        hashtagRepository.deleteByHome_Idx(homeIdx); 

        // 4. 최종적으로 Home 테이블 레코드 삭제
        homeRepository.delete(home);
    }  
      
}
