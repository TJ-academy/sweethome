package com.example.sweethome.home;

import com.example.sweethome.home.Home;
import com.example.sweethome.home.HomeService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class CompareController {

    private final HomeService homeService; // Home 데이터를 조회하기 위한 서비스

    // 생성자 주입 (Dependency Injection)
    public CompareController(HomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * 숙소 비교 페이지를 처리하는 메서드
     * URL: /compare?ids=27,26,8
     *
     * @param ids 콤마(,)로 구분된 숙소 ID 문자열 (예: "27,26,8")
     * @param model Thymeleaf로 데이터를 전달하기 위한 Model 객체
     * @return 템플릿 경로 ("home/compare")
     */
    @GetMapping("/compare")
    public String compareHomes(
            @RequestParam("ids") String ids,
            Model model) {

        // 1. 쿼리 파라미터 문자열을 Long 타입의 ID 리스트로 변환
        List<Long> homeIdList = Arrays.stream(ids.split(","))
                .map(String::trim)        // 공백 제거
                .filter(s -> !s.isEmpty()) // 빈 문자열 제거
                .map(Long::parseLong)     // Long 타입으로 변환
                .collect(Collectors.toList());

        // 2. ID 리스트를 이용하여 Home 데이터 조회
        // 이 메서드는 HomeService에 구현되어 있어야 합니다.
        List<Home> compareHomes = homeService.getHomesByIds(homeIdList);

        // 3. 템플릿으로 데이터 전달
        model.addAttribute("compareHomes", compareHomes);
        model.addAttribute("homeIdList", homeIdList); // 순서 유지를 위해 ID 리스트도 함께 전달할 수 있습니다.

        // templates/home/compare.html 템플릿 반환
        return "home/compare";
    }
}