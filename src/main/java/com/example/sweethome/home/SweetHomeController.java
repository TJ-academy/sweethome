package com.example.sweethome.home;

import com.example.sweethome.user.User;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/home/*")
@RequiredArgsConstructor
public class SweetHomeController {

    private final HomeService homeService;

    @GetMapping("/write")
    public String showWriteForm(HttpSession session, Model model) {
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("hostId", userProfile.getEmail());

        // HomeWriteDTO가 모델에 포함되지 않았다면 기본 값으로 생성해서 전달
        if (!model.containsAttribute("homeWriteDTO")) {
            model.addAttribute("homeWriteDTO", new HomeWriteDTO());
        }

        // 옵션 데이터 전달
        model.addAttribute("groupedOptions", homeService.getGroupedOptions());

        return "home/write";
    }

    @PostMapping("/write")
    public String registerHome(@ModelAttribute HomeWriteDTO homeWriteDTO,
                               RedirectAttributes redirectAttributes,
                               HttpSession session) {

        // 로그인된 사용자 확인
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }

        // 홈 등록 요청 시 로그인된 사용자 ID를 설정
        homeWriteDTO.setHostId(userProfile.getEmail());

        try {
            // 홈 등록 처리
            int newHomeIdx = homeService.registerHome(homeWriteDTO);
            redirectAttributes.addFlashAttribute("message", "숙소 등록 성공!");
            return "redirect:/home/detail/" + newHomeIdx;
        } catch (RuntimeException e) {
            log.error("숙소 등록 오류", e);
            redirectAttributes.addFlashAttribute("error", "숙소 등록 실패: " + e.getMessage());
            redirectAttributes.addFlashAttribute("homeWriteDTO", homeWriteDTO);
            return "redirect:/home/write";
        }
    }
}
