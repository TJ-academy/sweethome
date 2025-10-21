package com.example.sweethome.home;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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

        if (!model.containsAttribute("homeWriteDTO")) {
            model.addAttribute("homeWriteDTO", new HomeWriteDTO());
        }

        model.addAttribute("groupedOptions", homeService.getGroupedOptions());
        return "home/write";
    }

    @PostMapping("/write")
    public String registerHome(@ModelAttribute HomeWriteDTO homeWriteDTO,
                               RedirectAttributes redirectAttributes,
                               HttpSession session,BindingResult br) {
    	System.out.println("111111111111111111111");
    	System.out.println("binding:"+br.getAllErrors());
    	
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }
        
        homeWriteDTO.setHostId(userProfile.getEmail());

        try {
            int newHomeIdx = homeService.registerHome(homeWriteDTO);
            redirectAttributes.addFlashAttribute("message", "숙소 등록 성공!");
            return "redirect:/home/detail/" + newHomeIdx;
        } catch (MultipartException e) {
            log.error("파일 업로드 오류", e);
            redirectAttributes.addFlashAttribute("error", "파일 업로드 중 오류가 발생했습니다. 파일 크기나 형식을 확인하세요.");
        } catch (RuntimeException e) {
            log.error("숙소 등록 오류", e);
            redirectAttributes.addFlashAttribute("error", "숙소 등록 실패: " + e.getMessage());
        }

        redirectAttributes.addFlashAttribute("homeWriteDTO", homeWriteDTO);
        return "redirect:/home/write";
    }
}
