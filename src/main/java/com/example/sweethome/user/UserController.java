package com.example.sweethome.user;

import java.io.UnsupportedEncodingException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sweethome.email.EmailPostDto;
import com.example.sweethome.email.EmailService;
import com.example.sweethome.kakao.dto.KakaoProfile;

import jakarta.mail.MessagingException;
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
    
    @GetMapping("/kakao/logout")
    public String kakaoLogout(Model model) {
    	String location = 
    			"https://kauth.kakao.com/oauth/logout?client_id=" 
    + client_id 
    + "&logout_redirect_uri=" + logout_redirect_uri;
    	model.addAttribute("location", location);
    	return "redirect:" + location;
    }
    
    @GetMapping("/logout-complete")
    public String logoutComplete() {
    	System.out.println("로그아웃되었습니다.");
    	return "redirect:/";
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
			Model model) {
		// nickname 이 비어 있으면 username 으로 설정
	    if (user.getNickname() == null || user.getNickname().trim().isEmpty()) {
	        user.setNickname(user.getUsername());
	    }
	    
		service.insertUser(user);
		model.addAttribute("message", "회원가입이 완료되었습니다.");
		return "login"; 
	}
	
	@PostMapping("/mailConfirm")
	public String mailConfirm(@RequestBody EmailPostDto emailDto) {
        try {
            return emailService.sendEmail(emailDto.getEmail());
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace(); // 또는 로깅
            return "메일 전송 실패";
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