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
}
