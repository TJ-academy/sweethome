package com.example.sweethome;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sweethome.home.HomeResponseDto;
import com.example.sweethome.home.HomeService;
import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {

    private final HomeService homeService;

    @Autowired
    public HomeController(HomeService homeService) {
        this.homeService = homeService;
    }

    @GetMapping("/")
    public String basic(HttpSession session, Model model) {
        // 1. 전체 숙소 리스트(좋아요 개수 포함) 조회
        List<HomeResponseDto> homeList = homeService.getHomeListWithLikeCounts();
        model.addAttribute("homeList", homeList);

        // 2. 서울의 인기 숙소 조회
        List<HomeResponseDto> seoulHomeList = homeService.getSeoulPopularHomesWithLikeCounts();
        model.addAttribute("seoulHomeList", seoulHomeList); 
        
        // 3. 장기숙박 인기 숙소 조회
        List<HomeResponseDto> longTermHomeList = homeService.getLongTermPopularHomesWithLikeCounts();
        model.addAttribute("longTermHomeList", longTermHomeList);
        
	    // 4. 단체 숙소 인기 목록 조회 
        List<HomeResponseDto> largeHomeList = homeService.getLargePopularHomesWithLikeCounts();
        model.addAttribute("largeHomeList", largeHomeList);
        
        // 사용자 프로필 세션 처리
        User userProfile = (User) session.getAttribute("userProfile");
        if (session.getAttribute("kakaouser") != null) {
            session.removeAttribute("kakaouser");
        }
        if (userProfile != null) {
            model.addAttribute("userProfile", userProfile);
        }

        return "home";
    }

    /** ✅ /home은 "/"로 리다이렉트 */
    @GetMapping("/home")
    public String homeRedirect() {
        return "redirect:/";
    }

    @GetMapping("/search")
    public String searchHomes(@RequestParam("keyword") String keyword,
                              @RequestParam(value = "adults", defaultValue = "0") int adults,
                              @RequestParam(value = "checkin", required = false) String checkin,
                              @RequestParam(value = "checkout", required = false) String checkout,
                              @RequestParam(value = "children", defaultValue = "0") int children,
                              @RequestParam(value = "filter", required = false) String filter,
                              @RequestParam(value = "hashtags", required = false) List<String> hashtags,
                              HttpSession session,
                              Model model) {
        
        // 1. 필터 값에 따라 정렬 기준 결정 (HomeService에서 직접 filter 값을 사용하여 정렬하므로 이 로직은 불필요)
        /*
        String orderBy = "default"; // 기본 정렬
        if ("price".equals(filter)) {
            orderBy = "price_asc";
        } else if ("review".equals(filter)) {
            orderBy = "review_count_desc";
        } else if ("recommend".equals(filter)) {
            orderBy = "recommend_count_desc";
        }
        */
        
        // ⭐⭐⭐ [수정] homeService.searchHomesByLocationAndMaxPeople 메서드에 모든 파라미터를 전달합니다. ⭐⭐⭐
        List<HomeResponseDto> searchResults = homeService.searchHomesByLocationAndMaxPeople(
            keyword, 
            adults, 
            children, 
            checkin, 
            checkout, 
            hashtags, 
            filter
        );


        // 3. 검색 결과 모델에 추가
        model.addAttribute("homeList", searchResults);
        
        // 4. ✅ [추가] 모든 검색 조건 및 *현재 필터 값*을 Model에 추가하여 HTML로 전달합니다.
        model.addAttribute("keyword", keyword);
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("adults", adults);
        model.addAttribute("children", children);
        model.addAttribute("hashtags", hashtags); // 해시태그도 추가
        // ⭐⭐ [필수] 현재 선택된 필터 값을 뷰에 전달하여 라디오 버튼 상태 유지 및 JS에서 검색에 사용 ⭐⭐
        model.addAttribute("currentFilter", filter); 

        // 5. 세션에서 사용자 프로필 추가 (일관성 유지)
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile != null) {
            model.addAttribute("userProfile", userProfile);
        }

        return "home";
    }    
    /*
    //여행지 검색
    @GetMapping("/search")
    public String searchHomes(@RequestParam("keyword") String keyword,
                              @RequestParam(value = "adults", defaultValue = "0") int adults,
                              @RequestParam(value = "checkin", required = false) String checkin, // ✅ [추가]
                              @RequestParam(value = "checkout", required = false) String checkout, // ✅ [추가]
                              @RequestParam(value = "children", defaultValue = "0") int children, // ✅ [추가]
                              HttpSession session,
                              Model model) {
        
        // 이전에 HomeService에 추가 요청했던 메서드로 가정합니다.
        // 현재는 인원수 필터링까지만 구현 요청되었으므로, 날짜 필터링은 제외하고 호출합니다.
        List<HomeResponseDto> searchResults = homeService.searchHomesByLocationAndMaxPeople(keyword, adults);

        // 검색 결과 모델에 추가
        model.addAttribute("homeList", searchResults);
        
        // ✅ [추가] 모든 검색 조건을 Model에 추가하여 HTML로 전달합니다.
        model.addAttribute("keyword", keyword);
        model.addAttribute("checkin", checkin);
        model.addAttribute("checkout", checkout);
        model.addAttribute("adults", adults);
        model.addAttribute("children", children);

        // 세션에서 사용자 프로필 추가 (일관성 유지)
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile != null) {
            model.addAttribute("userProfile", userProfile);
        }

        // home.html 재사용
        return "home";
    }
    */
}