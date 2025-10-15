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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping("/home/*")
@RequiredArgsConstructor
public class SweetHomeController {

    private final HomeService homeService;
    private final OptionService optionService;

    // =======================================
    // GET: 숙소 등록 폼
    // =======================================
    @GetMapping("/write")
    public String showWriteForm(HttpSession session, Model model) {
        User userProfile = (User) session.getAttribute("userProfile");

        if (userProfile == null) {
            return "redirect:/user/login";
        }

        model.addAttribute("hostId", userProfile.getEmail());

        // 옵션 그룹별로 묶어서 전달
        List<Option> allOptions = optionService.getAllOptions();
        Map<String, List<Option>> groupedOptions = allOptions.stream()
                .collect(Collectors.groupingBy(Option::getOptionGroup));

        model.addAttribute("groupedOptions", groupedOptions);

        // 폼 바인딩용 DTO
        if (!model.containsAttribute("homeWriteDTO")) {
            model.addAttribute("homeWriteDTO", new HomeWriteDTO());
        }

        return "home/write";
    }

    // =======================================
    // POST: 숙소 등록 처리
    // =======================================
    @PostMapping("/write")
    public String registerHome(
            @ModelAttribute HomeWriteDTO homeWriteDTO,
            @RequestParam("thumbnail") MultipartFile thumbnailFile,
            @RequestParam(value = "imgOne", required = false) MultipartFile imgOne,
            @RequestParam(value = "imgTwo", required = false) MultipartFile imgTwo,
            @RequestParam(value = "imgThree", required = false) MultipartFile imgThree,
            @RequestParam(value = "imgFour", required = false) MultipartFile imgFour,
            @RequestParam(value = "imgFive", required = false) MultipartFile imgFive,
            @RequestParam(value = "imgSix", required = false) MultipartFile imgSix,
            @RequestParam(value = "imgSeven", required = false) MultipartFile imgSeven,
            @RequestParam(value = "imgEight", required = false) MultipartFile imgEight,
            @RequestParam(value = "imgNine", required = false) MultipartFile imgNine,
            @RequestParam(value = "imgTen", required = false) MultipartFile imgTen,
            RedirectAttributes redirectAttributes,
            HttpSession session) {

        log.info("숙소 등록 요청 DTO 수신: {}", homeWriteDTO.getTitle());

        // 세션 체크
        User userProfile = (User) session.getAttribute("userProfile");
        if (userProfile == null) {
            redirectAttributes.addFlashAttribute("error", "로그인이 필요합니다.");
            return "redirect:/user/login";
        }
        homeWriteDTO.setHostId(userProfile.getEmail());

        try {
            // 홈 등록
            int newHomeIdx = homeService.registerHome(homeWriteDTO);

            // 파일 처리(여기서는 서비스에서 따로 처리한다고 가정)
            // thumbnailFile, imgOne ~ imgTen 전달 가능

            redirectAttributes.addFlashAttribute("message", "숙소 등록이 성공적으로 완료되었습니다!");
            return "redirect:/home/detail/" + newHomeIdx;

        } catch (Exception e) {
            log.error("숙소 등록 중 오류 발생:", e);
            redirectAttributes.addFlashAttribute("error", "숙소 등록에 실패했습니다: " + e.getMessage());
            redirectAttributes.addFlashAttribute("homeWriteDTO", homeWriteDTO);
            return "redirect:/home/write";
        }
    }
}
