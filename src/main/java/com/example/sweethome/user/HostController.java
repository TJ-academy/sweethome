package com.example.sweethome.user;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.sweethome.home.Home;
import com.example.sweethome.home.HomeRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/host")
@RequiredArgsConstructor
public class HostController {

	private final HomeRepository homeRepository; // 🌟 HomeRepository 주입

    @GetMapping("/list")
    public String list(HttpSession session, Model model) {

        User user = (User) session.getAttribute("userProfile");
        if (user == null)
            return "redirect:/user/login";

        // 🌟 로그인된 호스트의 숙소 목록 조회
        List<Home> myHomes = homeRepository.findByHost(user);

        model.addAttribute("user", user);
        model.addAttribute("myHomes", myHomes); // 🌟 모델에 숙소 리스트 추가

        return "host/myHomeList";
    }

    /**
     * 숙소 상세 페이지
     */
    @GetMapping("/detail/{homeIdx}")
    public String detail(@PathVariable("homeIdx") int homeIdx, HttpSession session, Model model) {

        User user = (User) session.getAttribute("userProfile");
        if (user == null)
            return "redirect:/user/login";

        // 1. 숙소 정보 조회
        Optional<Home> homeOpt = homeRepository.findById(homeIdx);

        if (homeOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "숙소 정보를 찾을 수 없습니다.");
        }

        Home home = homeOpt.get();

        // 2. 호스트가 현재 로그인된 사용자인지 확인 (보안 체크)
        if (!home.getHost().getEmail().equals(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        // 3. 모델에 추가
        model.addAttribute("user", user);
        model.addAttribute("home", home); // 🌟 숙소 상세 정보 추가

        return "host/myHomeDetail"; // myHomeDetail.html 템플릿으로 이동
    }

	@GetMapping("/calendar")
	public String calendar(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userProfile");
		if (user == null)
			return "redirect:/user/login";

		model.addAttribute("user", user);
		return "host/calendar";
	}

	@GetMapping("/today")
	public String today(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userProfile");
		if (user == null)
			return "redirect:/user/login";

		model.addAttribute("user", user);

		return "host/today";
	}
}
