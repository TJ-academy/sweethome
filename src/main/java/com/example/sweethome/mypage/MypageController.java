package com.example.sweethome.mypage;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;
import com.example.sweethome.user.UserService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/mypage")
@RequiredArgsConstructor
public class MypageController {
	
	private User getLoggedInUser(HttpSession session) {
		// 세션에서 userProfile (User 객체)을 가져와 형 변환
		Object userProfile = session.getAttribute("userProfile");
		if (userProfile instanceof User) {
			return (User) userProfile;
		}
		// 로그인 정보가 없으면 예외 처리 또는 로그인 페이지 리다이렉션
		throw new IllegalStateException("로그인이 필요합니다.");
	}
	
	private final MypageService service;
	private final UserService userService;
	private final UserRepository userRepo;
	
    // 마이페이지
    @GetMapping("")
    public String mypage(HttpSession session, Model model) {
        User user = (User) session.getAttribute("userProfile");

        // 세션에 저장된 userProfile이 없으면 로그인 페이지로 리다이렉트
        if (user == null) {
            return "redirect:/user/login"; // 로그인 페이지로 리다이렉트
        }

        // user 정보를 마이페이지 모델에 전달
        model.addAttribute("user", user);

        return "mypage/mypage"; // 마이페이지 뷰로 이동
    }
	
	//회원탈퇴하시겠습니까?
	@GetMapping("/delete")
    public String delete(HttpSession session, Model model) {
		
		User user = getLoggedInUser(session);
		model.addAttribute("user", user);
		
    	return "mypage/deleteReal";
    }
	
	//회원탈퇴
	@PostMapping("/delete")
    public String delete(@RequestParam("email") String email, 
    		@RequestParam("password") String password,
            HttpSession session,
            Model model) {
		User user = (User) session.getAttribute("userProfile");
		if(userService.loginUser(email, password) && 
				(user.getEmail().equals(email))) {
			service.deleteUserByEmail(email);
			session.invalidate();
			return "mypage/deleteFinish";
        } else {
        	model.addAttribute("email", email);
            model.addAttribute("error", "이메일 또는 비밀번호가 일치하지 않습니다.");
            return "mypage/deleteReal"; 
        }
    }
}