package com.example.sweethome.home;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession;

@Controller
public class CompareController {

    private final HomeService homeService; // Home 데이터를 조회하기 위한 서비스

    // 생성자 주입 (Dependency Injection)
    public CompareController(HomeService homeService) {
        this.homeService = homeService;
    }

    /**
     * ✅ 숙소 비교 페이지를 처리하는 메서드
     * URL: /compare?ids=27,26,8
     *
     * @param ids 콤마(,)로 구분된 숙소 ID 문자열 (예: "27,26,8")
     * @param model Thymeleaf로 데이터를 전달하기 위한 Model 객체
     * @return 템플릿 경로 ("home/compare")
     */
    @GetMapping("/compare")
    public String compareHomes(
            @RequestParam("ids") String ids,
            Model model, HttpSession session) {

        // 1. 쿼리 파라미터 문자열을 Long 타입의 ID 리스트로 변환
        List<Long> homeIdList = Arrays.stream(ids.split(","))
                .map(String::trim)        
                .filter(s -> !s.isEmpty()) 
                .map(Long::parseLong)     
                .collect(Collectors.toList());

        // 2. DTO를 사용하여 상세 비교 데이터 조회
        // 이 메서드는 HomeService에 구현되어 있습니다.
        List<CompareHomeDetail> compareHomeDetails = homeService.getCompareHomeDetails(homeIdList);
        
        // 3. ✨ 옵션 그룹 통합 및 정렬 리스트 생성 (템플릿 동적 행 생성을 위한 데이터)
        // 이 메서드는 HomeService에 추가되어 Thymeleaf 보안 오류를 해결합니다.
        List<String> allOptionGroups = homeService.getAllUniqueOptionGroups(compareHomeDetails);


        // 4. 템플릿으로 데이터 전달
        // 템플릿 코드에 맞게 모델 속성 이름을 'compareHomeDetails'로 변경합니다.
        model.addAttribute("compareHomeDetails", compareHomeDetails); 
        model.addAttribute("allOptionGroups", allOptionGroups); 
        
        Object userProfile = session.getAttribute("userProfile");
        model.addAttribute("userProfile", userProfile); // userProfile이 null일 수 있습니다.
        
        // templates/home/compare.html 템플릿 반환
        return "home/compare";
    }
}