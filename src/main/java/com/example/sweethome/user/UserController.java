package com.example.sweethome.user;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sweethome.email.EmailService;
import com.example.sweethome.kakao.dto.KakaoProfile;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
	private final UserRepository repo;
	private final UserService service;
	
	private final EmailService emailService;
	
	@Value("${kakao.client_id}")
    private String client_id;

    @Value("${kakao.redirect_uri}")
    private String redirect_uri;
    
    @Value("${kakao.logout_redirect_uri}")
    private String logout_redirect_uri;

    @GetMapping("/login")
    public String kakaoLogin(Model model){ 
    	String location = 
    			"https://kauth.kakao.com/oauth/authorize?response_type=code&client_id=" 
    	+ client_id 
    	+ "&redirect_uri="+redirect_uri;
    	//+ "&prompt=login"; 
    	model.addAttribute("location", location); 
    	model.addAttribute("message", " ");
    	return "login"; 
    }
    
    @PostMapping("/login")
    public String login(@RequestParam("email") String email, 
    		@RequestParam("password") String password,
            HttpSession session,
            Model model) {
    	if(service.loginUser(email, password)) {
            Optional<User> user = repo.findByEmail(email);
            session.setAttribute("userProfile", user.get());
            return "redirect:/";
        } else {
            model.addAttribute("error", "아이디 또는 비밀번호가 일치하지 않습니다.");
            return "login"; 
        }
    }
    
    @GetMapping("/logout")
    public String logout(HttpSession session) {
    	session.invalidate();
    	return "redirect:/";
    }

	@GetMapping("/join")
	public String createForm(HttpSession session, Model model) {
		KakaoProfile kakaouser = (KakaoProfile) session.getAttribute("kakaouser");
	    if(kakaouser != null) {
	    	model.addAttribute("kakaouser", kakaouser);
	    }
		return "join";
	}

	@PostMapping("/join")
	public String insertUser(@ModelAttribute User user, 
			HttpSession session,
			Model model) {
		Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");

	    if (emailVerified == null || !emailVerified) {
	        model.addAttribute("message", "이메일 인증을 완료해주세요.");
	        return "join"; // 인증 안 된 경우, 회원가입 페이지로 돌아감
	    }
	    
		// nickname 이 비어 있으면 username 으로 설정
	    if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
	        user.setNickname(user.getUsername());
	    }
	    
		service.insertUser(user);
		model.addAttribute("message", "회원가입이 완료되었습니다.");
		return "login"; 
	}
	
	//이미 있는 이메일인지 아닌지 확인
	@ResponseBody
	@PostMapping("/checkEmailDuplicate")
	public String checkEmailDuplicate(@RequestParam("email") String email) {
	    boolean exists = repo.existsByEmail(email);
	    return exists ? "duplicate" : "ok";
	}
	
	//비밀번호 찾기 페이지 로딩
	@GetMapping("/findPwd")
	public String findPwd() {
		return "findPwd"; 
	}
	
	//비밀번호 재설정 페이지 로딩
	@GetMapping("/resetPassword")
	public String resetPassword(@RequestParam("email") String email, 
			Model model) {
		model.addAttribute("email", email);
		return "resetPassword"; 
	}
	
	//비밀번호 재설정
	@PostMapping("/resetPassword")
	public String resetPassword(@RequestParam("email") String email,
            @RequestParam("password") String password,
            @RequestParam("confirmPassword") String confirmPassword,
            Model model) {
		
		if (!password.equals(confirmPassword)) {
            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
            model.addAttribute("email", email);  // 다시 form에 유지
            return "resetPassword";
        }

        // 비밀번호 업데이트 처리
        try {
            service.updatePassword(email, password);
            model.addAttribute("success", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/user/login";
        } catch (Exception e) {
            model.addAttribute("error", "비밀번호 변경 중 오류가 발생했습니다.");
            model.addAttribute("email", email);
            return "resetPassword";
        }
	}
	
	@GetMapping("/cancelJoin")
	public String cancelJoin(HttpSession session) {
	    if(session.getAttribute("kakaouser") != null) {
	    	session.removeAttribute("kakaouser");
	    }
	    return "redirect:/";
	}
}