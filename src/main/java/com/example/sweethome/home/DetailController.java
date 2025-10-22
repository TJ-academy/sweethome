package com.example.sweethome.home;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.sweethome.user.User;
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

    /** 상세 페이지: /home/detail/{idx} */
    @GetMapping("/{idx}")
    public String show(@PathVariable("idx") int idx, Model model, HttpSession session) {

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

        return "home/detail";
    }

    /** 파라미터 없는 접근은 목록으로 */
    @GetMapping
    public String redirectToList() {
        return "redirect:/home";
    }
}
