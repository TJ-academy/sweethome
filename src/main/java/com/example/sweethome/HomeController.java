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

    /** ✅ 기본 홈 페이지 */
    @GetMapping("/")
    public String basic(HttpSession session, Model model) {
        // 숙소 리스트(좋아요 개수 포함) 조회
        List<HomeResponseDto> homeList = homeService.getHomeListWithLikeCounts();
        model.addAttribute("homeList", homeList);

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

    /** ✅ 여행지 검색 */
    @GetMapping("/search")
    public String searchHomes(@RequestParam("keyword") String keyword,
                              HttpSession session,
                              Model model) {
        // 검색어를 기반으로 숙소 검색 (location 컬럼 기준)
        List<HomeResponseDto> searchResults = homeService.searchHomesByLocation(keyword);

        // 검색 결과 모델에 추가
        model.addAttribute("homeList", searchResults);
        model.addAttribute("keyword", keyword);

        // 세션에서 사용자 프로필 추가 (일관성 유지)
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile != null) {
            model.addAttribute("userProfile", userProfile);
        }

        // home.html 재사용
        return "home";
    }
}