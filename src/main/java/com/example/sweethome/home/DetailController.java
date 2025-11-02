package com.example.sweethome.home;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sweethome.review.Review;
import com.example.sweethome.review.ReviewDirection;
import com.example.sweethome.review.ReviewDto;
import com.example.sweethome.review.ReviewRepository;
import com.example.sweethome.user.User;
import com.example.sweethome.wishlist.Wishlist;
import com.example.sweethome.wishlist.WishlistFolder;
import com.example.sweethome.wishlist.WishlistFolderRepository;
import com.example.sweethome.wishlist.WishlistRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/home/detail")
@RequiredArgsConstructor
public class DetailController {

    private final HomeRepository homeRepository;
    private final HashtagRepository hashtagRepository;
    private final OptionRepository optionRepository;
    private final AccommodationOptionRepository accommodationOptionRepository;
    private final HomePhotoRepository homePhotoRepository;
    private final WishlistRepository wishlistRepository;
    private final WishlistFolderRepository wishlistFolderRepository;
    private final ReviewRepository reviewRepository;
    
    /** 상세 페이지: /home/detail/{idx} */
    @GetMapping("/{idx}")
    public String show(@PathVariable("idx") int idx, Model model, HttpSession session, @RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "checkin", required = false) String checkin, @RequestParam(value = "checkout", required = false) String checkout, @RequestParam(value = "adults", required = false, defaultValue = "0") Integer adults, @RequestParam(value = "children", required = false, defaultValue = "0") Integer children) {
    	
    	// ******************** 수정된 부분: userProfile 추가 ********************
        Object userProfile = session.getAttribute("userProfile");
        model.addAttribute("userProfile", userProfile); // userProfile이 null일 수 있습니다.
        // *******************************************************************
        
        // 좋아요 폴더 목록 조회
        List<WishlistFolder> folders = new ArrayList<>(); // 기본적으로 빈 리스트로 초기화
        
        if (userProfile instanceof User) {
            User user = (User) userProfile;
            
            // 👈 (기존 57줄) 폴더 목록 조회 (UI 구성용)
            folders = wishlistFolderRepository.findByUser(user); 
            
            // 🚀 성능 개선: 폴더별 반복 조회(N+1) 대신, 유저의 모든 위시리스트를 한 번의 쿼리로 가져옵니다.
            // 이 메서드는 Wishlist, Folder, Home을 JOIN FETCH해야 합니다.
            List<Wishlist> allWishlists = wishlistRepository.findAllWishlistsByUserWithFolderAndHome(user);
            
            // 메모리 상에서 폴더 ID별로 그룹핑합니다. (DB 쿼리 반복 방지)
            Map<Long, List<Wishlist>> folderWishlists = allWishlists.stream()
                .collect(Collectors.groupingBy(w -> w.getFolder().getIdx()));
                
            // JSON 변환 시 Instant 필드를 제거한 간단한 DTO로 변환
            Map<Long, List<Map<String, Object>>> safeFolderWishlists = folderWishlists.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().stream().map(wish -> {
                        Map<String, Object> safe = new HashMap<>();
                        
                        // 이미 Home 엔티티가 FETCH JOIN으로 로드되어 있어 지연 로딩 문제(N+1) 없음
                        String thumbnail = wish.getHome().getThumbnail() != null
                        	    ? wish.getHome().getThumbnail()
                        	    : "/images/default_main_image.jpg";

                        	safe.put("home", Map.of(
                        	    "idx", wish.getHome().getIdx(),
                        	    "title", wish.getHome().getTitle(),
                        	    "thumbnail", thumbnail
                        	));
                        return safe;
                    }).collect(Collectors.toList())
                ));

            model.addAttribute("folderWishlists", safeFolderWishlists);

	         // 로그 찍기 로직 (그룹핑된 데이터를 사용)
	            folderWishlists.forEach((folderId, wishlists) -> {
	                System.out.println("폴더ID: " + folderId + ", 위시리스트 개수: " + wishlists.size());
	                wishlists.forEach(w -> System.out.println("  - " + w.getHome().getTitle()));
	            });
	          
        }
        
        // 3. 폴더 목록 Model 추가 (로그인 여부와 관계없이)
        model.addAttribute("folders", folders);
    	
        // 1) Home 조회 (PK: idx)
        //Home home = homeRepository.findById(idx)
        //        .orElseThrow(() -> new IllegalArgumentException("숙소가 존재하지 않습니다. idx=" + idx));
        //HomePhoto homePhoto = homePhotoRepository.findByHome(home); // Home에 해당하는 사진 데이터를 가져옵니다.
        
     // ⭐️ 이 쿼리를 반드시 사용하도록 HomeRepository에 정의해야 합니다.
        Home home = homeRepository.findByIdWithAll(idx) // <--- 반드시 findByIdWithAll로 변경
                .orElseThrow(() -> new IllegalArgumentException("숙소가 존재하지 않습니다. idx=" + idx));
        HomePhoto homePhoto = home.getHomePhoto(); // <--- Home 엔티티에서 직접 가져옴 (추가 쿼리 없음)
        
     // homePhoto가 null인 경우를 처리
        if (homePhoto == null) {
            // Home 객체와 연결하여 빈 HomePhoto 객체 생성 (Builder 사용)
            homePhoto = HomePhoto.builder().home(home).build(); 
        }
        
        // 2) Hashtag 조회 (없을 수 있음)
        // Hashtag hashtag = hashtagRepository.findByHome(home).orElse(null);
        Hashtag hashtag = home.getHashtag();
        model.addAttribute("hashtag", hashtag);
        
        // 보기 좋은 태그 리스트 생성
        List<String> activeTags = new ArrayList<>();
        if (hashtag != null) {
            if (hashtag.isWifi())         activeTags.add("#와이파이");
            if (hashtag.isTv())           activeTags.add("#TV");
            if (hashtag.isKitchen())      activeTags.add("#주방");
            if (hashtag.isFreePark())     activeTags.add("#무료주차");
            if (hashtag.isSelfCheckin())  activeTags.add("#셀프체크인");
            if (hashtag.isColdWarm())     activeTags.add("#냉난방");
            if (hashtag.isPetFriendly())  activeTags.add("#반려동물동반");
            if (hashtag.isBarrierFree())  activeTags.add("#장애물없는시설");
            if (hashtag.isElevator())     activeTags.add("#엘리베이터");
        }
        model.addAttribute("activeTags", activeTags);

        // 3) 숙소 옵션 조회 및 그룹핑 (기존 로직 유지)
        // ⚠️ AccommodationOptionRepository.findByHome(home)의 내부 쿼리가 
        //    Option 엔티티를 JOIN FETCH하도록 최적화되어 있어야 N+1 문제가 해결됩니다.
        //List<AccommodationOption> accOptions = accommodationOptionRepository.findByHome(home);
        
     // ⭐️ 수정된 코드: Option을 JOIN FETCH로 가져오는 메서드 사용
        List<AccommodationOption> accOptions = accommodationOptionRepository.findByHomeWithOption(home);
        
        // AccommodationOption 목록에서 Option 엔티티를 추출하고, optionGroup별로 그룹핑합니다.
        Map<String, List<Option>> existingOptionsByGroup = accOptions.stream()
                // exist=true 인 옵션만 필터링 (Optional)
                .filter(AccommodationOption::isExist) 
                // Option 엔티티로 매핑 (JOIN FETCH 덕분에 N+1 발생 안 함)
                .map(AccommodationOption::getOption)
                // 그룹별로 Option 객체들을 모으기 전에 정렬
                .sorted(Comparator.comparing(Option::getOptionGroup)
                        .thenComparing(Option::getOptionName))
                // OptionGroup 기준으로 그룹핑
                .collect(groupingBy(Option::getOptionGroup, toList()));
        
     // ⭐️ 좋아요 상태 확인 로직 추가
        boolean isLiked = false;
        if (userProfile instanceof User) {
            User user = (User) userProfile;
            // existsByHomeAndUser 메서드를 사용하여 상태 확인
            isLiked = wishlistRepository.existsByHomeAndUser(home, user);
        }
        
        // Model에 좋아요 상태 추가
        model.addAttribute("isLiked", isLiked); // ⭐️ 추가
        
        // 4) 뷰 모델 바인딩 (엔티티 기준 필드명 그대로)
        model.addAttribute("home", home);               
        model.addAttribute("hashtag", hashtag);         
        model.addAttribute("optionsByGroup", existingOptionsByGroup);
        model.addAttribute("homePhoto", homePhoto); 
        
        //검색조건
        model.addAttribute("keyword", keyword);
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("adults", adults);
        model.addAttribute("children", children);
        
        
        // ✅ 4. 리뷰 관련 데이터 추가 (GUEST_TO_HOST 기준)
        
     // 4-1. 호스트 활동 기간 (기존 로직 유지)
        /* (생략된 주석 처리된 로직) */
        
        // 4-2. 총 리뷰 개수 (GUEST_TO_HOST)
        int reviewCount = reviewRepository.countByHomeAndDirection(home, ReviewDirection.GUEST_TO_HOST); 
        model.addAttribute("reviewCount", reviewCount); 

        // 4-3. 평균 평점 (GUEST_TO_HOST)
        Double avgRating = reviewRepository.findAverageRatingByHomeAndDirection(home, ReviewDirection.GUEST_TO_HOST);
        // 소수점 한 자리까지 반올림
        model.addAttribute("rating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

        // 4-4. 최신 리뷰 4개 (JOIN FETCH 적용)
        List<Review> recentReviews = reviewRepository.findRecentReviewsWithWriterAndReply(
            home, ReviewDirection.GUEST_TO_HOST, PageRequest.of(0, 4) // PageRequest는 Pageable 인터페이스를 구현합니다.
        );
        model.addAttribute("recentReviews", recentReviews);
        
        return "home/detail";
    }
    
    /*
    @GetMapping("/{idx}")
    public String show(@PathVariable("idx") int idx, Model model, HttpSession session, @RequestParam(value = "keyword", required = false) String keyword, @RequestParam(value = "checkin", required = false) String checkin, @RequestParam(value = "checkout", required = false) String checkout, @RequestParam(value = "adults", required = false, defaultValue = "0") Integer adults, @RequestParam(value = "children", required = false, defaultValue = "0") Integer children) {
    	
    	// ******************** 수정된 부분: userProfile 추가 ********************
        Object userProfile = session.getAttribute("userProfile");
        model.addAttribute("userProfile", userProfile); // userProfile이 null일 수 있습니다.
        // *******************************************************************
        
        // 좋아요 폴더 목록 조회
        List<WishlistFolder> folders = new ArrayList<>(); // 기본적으로 빈 리스트로 초기화
        
        if (userProfile instanceof User) {
            User user = (User) userProfile;
            // 👈 완성된 폴더 조회 로직
            folders = wishlistFolderRepository.findByUser(user); 
            
            //나래추가
            Map<Long, List<Wishlist>> folderWishlists = folders.stream()
                    .collect(Collectors.toMap(
                            WishlistFolder::getIdx,
                            folder -> wishlistRepository.findByFolderWithHome(folder)
                    ));
         // JSON 변환 시 Instant 필드를 제거한 간단한 DTO로 변환
            Map<Long, List<Map<String, Object>>> safeFolderWishlists = folderWishlists.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().stream().map(wish -> {
                        Map<String, Object> safe = new HashMap<>();
//                        safe.put("home", Map.of(
//                            "idx", wish.getHome().getIdx(),
//                            "title", wish.getHome().getTitle(),
//                            "thumbnail", wish.getHome().getThumbnail()
//                        ));
                        String thumbnail = wish.getHome().getThumbnail() != null
                        	    ? wish.getHome().getThumbnail()
                        	    : "/images/default_main_image.jpg";

                        	safe.put("home", Map.of(
                        	    "idx", wish.getHome().getIdx(),
                        	    "title", wish.getHome().getTitle(),
                        	    "thumbnail", thumbnail
                        	));
                        return safe;
                    }).collect(Collectors.toList())
                ));

            model.addAttribute("folderWishlists", safeFolderWishlists);

	         // 로그 찍기
	            folderWishlists.forEach((folderId, wishlists) -> {
	                System.out.println("폴더ID: " + folderId + ", 위시리스트 개수: " + wishlists.size());
	                wishlists.forEach(w -> System.out.println("  - " + w.getHome().getTitle()));
	            });
	          //나래추가끝

        }
        
        // 3. 폴더 목록 Model 추가 (로그인 여부와 관계없이)
        // 이 부분을 기존 코드 블록 밖으로 빼내고 중복을 제거합니다.
        model.addAttribute("folders", folders);
    	
        // 1) Home 조회 (PK: idx)
        Home home = homeRepository.findById(idx)
                .orElseThrow(() -> new IllegalArgumentException("숙소가 존재하지 않습니다. idx=" + idx));
        HomePhoto homePhoto = homePhotoRepository.findByHome(home); // Home에 해당하는 사진 데이터를 가져옵니다.
        
     // homePhoto가 null인 경우를 처리 (1번 수정 방안이 적용되었다면,
        // 이 곳에서 빈 HomePhoto를 만들더라도 getImages()는 빈 리스트를 반환할 것입니다.)
        if (homePhoto == null) {
            // Home 객체와 연결하여 빈 HomePhoto 객체 생성 (Builder 사용)
            homePhoto = HomePhoto.builder().home(home).build(); 
        }
        
        // 2) Hashtag 조회 (없을 수 있음)
        Hashtag hashtag = hashtagRepository.findByHome(home).orElse(null);
        model.addAttribute("hashtag", hashtag);
        
        // 보기 좋은 태그 리스트 생성
        List<String> activeTags = new ArrayList<>();
        if (hashtag != null) {
            if (hashtag.isWifi())         activeTags.add("#와이파이");
            if (hashtag.isTv())           activeTags.add("#TV");
            if (hashtag.isKitchen())      activeTags.add("#주방");
            if (hashtag.isFreePark())     activeTags.add("#무료주차");
            if (hashtag.isSelfCheckin())  activeTags.add("#셀프체크인");
            if (hashtag.isColdWarm())     activeTags.add("#냉난방");
            if (hashtag.isPetFriendly())  activeTags.add("#반려동물동반");
            if (hashtag.isBarrierFree())  activeTags.add("#장애물없는시설");
            if (hashtag.isElevator())     activeTags.add("#엘리베이터");
        }
        model.addAttribute("activeTags", activeTags);

        // 3) 숙소 옵션 조회 및 그룹핑 (기존 로직 수정)
        // 해당 숙소(Home)에 연결된 AccommodationOption 목록을 조회합니다.
        List<AccommodationOption> accOptions = accommodationOptionRepository.findByHome(home);
        
        // AccommodationOption 목록에서 Option 엔티티를 추출하고, optionGroup별로 그룹핑합니다.
        // 그리고 OptionGroup과 OptionName 기준으로 정렬합니다.
        Map<String, List<Option>> existingOptionsByGroup = accOptions.stream()
                // exist=true 인 옵션만 필터링 (Optional)
                .filter(AccommodationOption::isExist) 
                // Option 엔티티로 매핑
                .map(AccommodationOption::getOption)
                // 그룹별로 Option 객체들을 모으기 전에 정렬
                .sorted(Comparator.comparing(Option::getOptionGroup)
                        .thenComparing(Option::getOptionName))
                // OptionGroup 기준으로 그룹핑
                .collect(groupingBy(Option::getOptionGroup, toList()));
        
     // ⭐️ 좋아요 상태 확인 로직 추가
        boolean isLiked = false;
        if (userProfile instanceof User) {
            User user = (User) userProfile;
            // existsByHomeAndUser 메서드를 사용하여 상태 확인
            isLiked = wishlistRepository.existsByHomeAndUser(home, user);
        }
        
        // Model에 좋아요 상태 추가
        model.addAttribute("isLiked", isLiked); // ⭐️ 추가
        
        // 4) 뷰 모델 바인딩 (엔티티 기준 필드명 그대로)
        model.addAttribute("home", home);               // title, description, location, address, thumbnail, maxPeople, room, checkIn, checkOut, costBasic, costExpen, homeType, host 등
        model.addAttribute("hashtag", hashtag);         // wifi, tv, kitchen, freePark, selfCheckin, coldWarm, petFriendly, barrierFree, elevator
        model.addAttribute("optionsByGroup", existingOptionsByGroup);
        model.addAttribute("homePhoto", homePhoto); // 이미 위에서 바인딩했으므로 중복 제거 (아래 코드는 삭제)
        
        //검색조건
        model.addAttribute("keyword", keyword);
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("adults", adults);
        model.addAttribute("children", children);
        
        
        // ✅ 4. 리뷰 관련 데이터 추가 (GUEST_TO_HOST 기준)
        
     // 4-1. 호스트 활동 기간 (기존 로직 유지)
        /*
        // 요청에 따라 활동 기간 계산 로직을 삭제합니다.
        if (home.getHost() != null && home.getHost().getCreatedAt() != null) {
            LocalDate hostJoinDate = home.getHost().getCreatedAt().toLocalDate();
            long years = ChronoUnit.YEARS.between(hostJoinDate, LocalDate.now());
            model.addAttribute("hostMemberSince", years > 0 ? years : 1);
        }
        
        // 4-2. 총 리뷰 개수 (GUEST_TO_HOST)
        int reviewCount = reviewRepository.countByHomeAndDirection(home, ReviewDirection.GUEST_TO_HOST); 
        model.addAttribute("reviewCount", reviewCount); 

        // 4-3. 평균 평점 (GUEST_TO_HOST)
        Double avgRating = reviewRepository.findAverageRatingByHomeAndDirection(home, ReviewDirection.GUEST_TO_HOST);
        // 소수점 한 자리까지 반올림
        model.addAttribute("rating", avgRating != null ? Math.round(avgRating * 10.0) / 10.0 : 0.0);

        // 4-4. 최신 리뷰 4개 (GUEST_TO_HOST)
        // PageRequest.of(0, 4)를 사용해 상위 4개만 가져옵니다.
        List<Review> recentReviews = reviewRepository.findByHomeAndDirectionOrderByCreatedAtDesc(
            home, ReviewDirection.GUEST_TO_HOST, PageRequest.of(0, 4)
        );
        model.addAttribute("recentReviews", recentReviews);
        
        return "home/detail";
    }
	*/

    /** 파라미터 없는 접근은 목록으로 */
    @GetMapping
    public String redirectToList() {
        return "redirect:/home";
    }
    
    // ✅ 리뷰 모달 팝업용 전체 리뷰 조회 (JSON API)
    @GetMapping("/{idx}/reviews/all")
    @ResponseBody
    public List<ReviewDto> getAllReviews(@PathVariable("idx") int idx) {
        //Home home = homeRepository.findById(idx)
        //        .orElseThrow(() -> new IllegalArgumentException("숙소가 존재하지 않습니다. idx=" + idx));
    	Home home = homeRepository.findByIdWithAll(idx) // <--- 1차 수정
                .orElseThrow(() -> new IllegalArgumentException("숙소가 존재하지 않습니다. idx=" + idx));
        
     // GUEST_TO_HOST 리뷰 전체 목록을 최신순으로 조회
        // Fetch Join을 사용하여 Review와 Reply를 한 번에 가져와 Lazy Loading 오류 방지
        List<Review> reviews = reviewRepository.findByHomeAndDirectionWithReply(
            home, 
            ReviewDirection.GUEST_TO_HOST // 게스트가 호스트에게 쓴 리뷰만 조회
        );
        
        // ✅ Review 엔티티 목록을 ReviewDto 목록으로 변환하여 반환
        return reviews.stream()
                .map(ReviewDto::fromEntity)
                .collect(Collectors.toList());
        
    }
}
