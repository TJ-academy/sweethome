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

@Slf4j //로깅 사용
@Controller
@RequestMapping("/home/*")
@RequiredArgsConstructor
public class SweetHomeController {

    private final HomeService homeService;

    @GetMapping("/write")
    public String showWriteForm(HttpSession session, Model model) {
        //세션에서 유저객체 가져옴
        User userProfile = (User) session.getAttribute("userProfile"); 
        
        if (userProfile == null) {
            return "redirect:/user/login"; 
        }
        
        // Thymeleaf 템플릿에서 hostId를 사용할 수 있도록 모델에 추가
        model.addAttribute("hostId", userProfile.getEmail());
        
        // --- [핵심 수정 부분] ---
        // 폼 렌더링을 위해 비어있는 DTO 객체를 모델에 추가합니다.
        // 이 객체가 있어야 템플릿의 th:object="*{homeWriteDTO}" 바인딩 오류가 해결됩니다.
        if (!model.containsAttribute("homeWriteDTO")) {
            model.addAttribute("homeWriteDTO", new HomeWriteDTO());
        }
        // -------------------------
        
        return "home/write";
    }

    //숙소 등록 폼데이터 처리, HomeService 호출하여 숙소 등록
    @PostMapping("/write")
    public String registerHome(
            @ModelAttribute HomeWriteDTO homeWriteDTO,
            RedirectAttributes redirectAttributes) {

        log.info("숙소 등록 요청 DTO 수신: {}", homeWriteDTO.getTitle());

        try {
            // 서비스 호출하여 숙소 등록 처리
            int newHomeIdx = homeService.registerHome(homeWriteDTO);
            
            redirectAttributes.addFlashAttribute("message", "숙소 등록이 성공적으로 완료되었습니다!");
            
            return "redirect:/";
            //return "redirect:/home/detail/" + newHomeIdx; 
            
        } catch (RuntimeException e) {
            log.error("숙소 등록 중 오류 발생:", e);
            
            redirectAttributes.addFlashAttribute("error", "숙소 등록에 실패했습니다: " + e.getMessage());
            // 오류 발생 시 DTO를 리다이렉트 시에 Flash Attribute로 담아서 
            // GET /home/write 메서드에서 다시 모델로 읽어들여 폼에 이전 데이터를 유지합니다.
            redirectAttributes.addFlashAttribute("homeWriteDTO", homeWriteDTO);
            
            return "redirect:/home/write";
        }
    }
}