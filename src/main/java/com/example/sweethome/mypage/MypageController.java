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
	private final MypageService service;
	private final UserService userService;
	private final UserRepository userRepo;
	
	//마이페이지
	@GetMapping("")
    public String mypage(HttpSession session) {
    	return "mypage/mypage";
    }
	
	//회원탈퇴하시겠습니까?
	@GetMapping("/delete")
    public String delete(HttpSession session) {
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