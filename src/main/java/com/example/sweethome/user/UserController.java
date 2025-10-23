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
    	return "login/login"; 
    }
    
    @PostMapping("/login")
    public String login(@RequestParam("email") String email, 
            @RequestParam("password") String password,
            HttpSession session,
            Model model) {
        Optional<User> userOpt = repo.findByEmail(email);
        if (userOpt.isEmpty() || !service.loginUser(email, password)) {
            model.addAttribute("error", "이메일 또는 비밀번호가 일치하지 않습니다.");
            return "login/login";
        }

        User user = userOpt.get();
        session.setAttribute("userProfile", user);

        // 로그인 후 prevPage가 존재하면 해당 페이지로 리다이렉트
        String prevPage = (String) session.getAttribute("prevPage");

        /*
         // prevPage가 없으면 기본 페이지(홈 페이지)로 리다이렉트
        
        if (prevPage == null || prevPage.isEmpty()) {
            prevPage = "/"; // 기본 URL 설정
        }

        // prevPage 사용 후 제거
        session.removeAttribute("prevPage");

        return "redirect:" + prevPage; // 예약 페이지로 리다이렉트
        */
        
       // prevPage가 있으면 사용하고, 세션에서 삭제
        if (prevPage != null && !prevPage.isEmpty()) {
            session.removeAttribute("prevPage"); // 사용 후 삭제
            System.out.println("로그인 성공! 이전 페이지로 리다이렉트: " + prevPage);
            return "redirect:" + prevPage;
        }
        
        // prevPage가 없으면 기본 경로('/')로 리다이렉트
        System.out.println("로그인 성공! 기본 경로('/')로 리다이렉트");
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
		session.setAttribute("emailVerified", false);
	    if(kakaouser != null) {
	    	session.setAttribute("emailVerified", true);
	    	model.addAttribute("kakaouser", kakaouser);
	    }
		return "login/join";
	}

	@PostMapping("/join")
	public String insertUser(@ModelAttribute User user, 
			HttpSession session,
			Model model) {
		Boolean emailVerified = (Boolean) session.getAttribute("emailVerified");
		Boolean nicknameVerified = (Boolean) session.getAttribute("nicknameVerified");
		String verifiedNickname = (String) session.getAttribute("verifiedNickname");
		model.addAttribute("user", user);

	    if (emailVerified == null || !emailVerified) {
	        model.addAttribute("message", "이메일 인증을 완료해주세요.");
	        return "login/join"; // 인증 안 된 경우, 회원가입 페이지로 돌아감
	    }
	    
	    if (nicknameVerified == null || !nicknameVerified || 
	            !user.getNickname().equals(verifiedNickname)) {
	        model.addAttribute("message", "닉네임 중복 확인을 완료해주세요.");
	        return "login/join"; // 인증 안 된 경우, 회원가입 페이지로 돌아감
	    }
	    
		service.insertUser(user);
		session.removeAttribute("emailVerified");
		session.removeAttribute("nicknameVerified");
		session.removeAttribute("verifiedNickname");
		model.addAttribute("message", "회원가입이 완료되었습니다.");
		return "login/login"; 
	}
	
	//이미 있는 이메일인지 아닌지 확인
	@ResponseBody   //json 리턴
	@PostMapping("/checkEmailDuplicate")
	public String checkEmailDuplicate(@RequestParam("email") String email) {
	    boolean exists = repo.existsByEmail(email);
	    return exists ? "duplicate" : "ok";
	}
	
	//이미 있는 닉네임인지 아닌지 확인
	@ResponseBody
	@PostMapping("/checkNicknameDuplicate")
	public String checkNicknameDuplicate(@RequestParam("nickname") String nickname,
			HttpSession session) {
	    boolean exists = repo.existsByNickname(nickname);

	    if (!exists) {
	        session.setAttribute("nicknameVerified", true);
	        session.setAttribute("verifiedNickname", nickname);  //닉네임 바뀌었는지 추적
	        return "ok";
	    } else {
	        session.setAttribute("nicknameVerified", false);
	        return "duplicate";
	    }
	}
	
	//비밀번호 찾기 페이지 로딩
	@GetMapping("/findPwd")
	public String findPwd() {
		return "login/findPwd"; 
	}
	
	//비밀번호 재설정 페이지 로딩
	@GetMapping("/resetPassword")
	public String resetPassword(@RequestParam("email") String email, 
			Model model) {
		model.addAttribute("email", email);
		return "login/resetPassword"; 
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
            return "login/resetPassword";
        }

        // 비밀번호 업데이트 처리
        try {
            service.updatePassword(email, password);
            model.addAttribute("success", "비밀번호가 성공적으로 변경되었습니다.");
            return "redirect:/user/login";
        } catch (Exception e) {
            model.addAttribute("error", "비밀번호 변경 중 오류가 발생했습니다.");
            model.addAttribute("email", email);
            return "login/resetPassword";
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