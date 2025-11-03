package com.example.sweethome.ssong;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;
import com.example.sweethome.user.UserService;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.groups.Default;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserApiController {
	private final UserRepository repo;
	private final UserService service;
	private final JwtUtil jwtUtil;

    @PostMapping(value = "/login", 
    		consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE)
    public ResponseEntity<?> login(
    		@Validated({UserFlutterDTO.Login.class, Default.class}) @ModelAttribute("form") UserFlutterDTO form, 
			BindingResult br, 
			HttpServletResponse res) {
    	if(br.hasErrors()) {
			return ResponseEntity.badRequest()
					.body(Map.of("ok", false, 
							"code", "INVALID_FORM", 
							"message", "입력값을 확인해 주세요."));
		}

        String email = form.getEmail();
        String password = form.getPassword();
        
        if(!service.loginUser(email, password)) {
        	return ResponseEntity.status(401)
					.body(Map.of("ok", false, 
							"code", "BAD_CREDENTIALS", 
							"message", "아이디 또는 비밀번호를 확인해 주세요."));
        }
        
        User user = repo.findByEmail(email).get();
        String token = jwtUtil.generateToken(user.getEmail());
        
        ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", token)
        		.httpOnly(true).path("/")
				.maxAge(Duration.ofHours(1)).sameSite("Lax").secure(false)
				.build();
		res.addHeader("Set-Cookie", cookie.toString());
		
		return ResponseEntity.ok(Map.of("ok", true, 
				"email", user.getEmail(), 
				"nickname", user.getNickname(),
				"profileImg", user.getProfileImg(),
				"token", token));
    }
    
    @GetMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse res) {
    	ResponseCookie cookie = ResponseCookie.from("ACCESS_TOKEN", "")
    			.httpOnly(true)
				.secure(false).sameSite("Lax").path("/").maxAge(0)
				.build();
		res.addHeader("Set-Cookie", cookie.toString());
		return ResponseEntity.ok(Map.of("ok", true));
    }
	
    //세션 유지 확인용
    @GetMapping("/session")
    public ResponseEntity<?> session(HttpServletRequest req) {
    	Optional<String> tokenOpt = getTokenFromCookie(req);  //사용자 토큰 정보
		if(tokenOpt.isEmpty())
			return ResponseEntity.status(401)
                    .body(Map.of("ok", false, "message", "토큰 없음"));
		
		try {
			Claims claims = jwtUtil.validateTokenAndGetEmail(tokenOpt.get())
					.getBody();  //토큰 내용 검사
			String email = claims.getSubject();
			
			return ResponseEntity.ok(Map.of(
		            "ok", true,
		            "email", email
		        ));
		} catch (Exception e) {
			// 토큰이 만료되거나 유효하지 않음
	        return ResponseEntity.status(401)
	        		.body(Map.of("ok", false, "message", "유효하지 않은 토큰"));
		}
    }
    
    private Optional<String> getTokenFromCookie(HttpServletRequest req) {
		Cookie[] cookies = req.getCookies();
		if(cookies == null)
			return Optional.empty();
		// 쿠키 배열을 순회하면서 access_token을 읽음(반복문 대체)
		return Arrays.stream(cookies).filter(c -> "ACCESS_TOKEN"
		.equals(c.getName()))
				.map(Cookie::getValue).findFirst();
	}
	
//	//이미 있는 닉네임인지 아닌지 확인
//	@ResponseBody
//	@PostMapping("/checkNicknameDuplicate")
//	public String checkNicknameDuplicate(@RequestParam("nickname") String nickname,
//			HttpSession session) {
//	    boolean exists = repo.existsByNickname(nickname);
//
//	    if (!exists) {
//	        session.setAttribute("nicknameVerified", true);
//	        session.setAttribute("verifiedNickname", nickname);  //닉네임 바뀌었는지 추적
//	        return "ok";
//	    } else {
//	        session.setAttribute("nicknameVerified", false);
//	        return "duplicate";
//	    }
//	}
//	
//	//비밀번호 찾기 페이지 로딩
//	@GetMapping("/findPwd")
//	public String findPwd() {
//		return "login/findPwd"; 
//	}
//	
//	//비밀번호 재설정 페이지 로딩
//	@GetMapping("/resetPassword")
//	public String resetPassword(@RequestParam("email") String email, 
//			Model model) {
//		model.addAttribute("email", email);
//		return "login/resetPassword"; 
//	}
//	
//	//비밀번호 재설정
//	@PostMapping("/resetPassword")
//	public String resetPassword(@RequestParam("email") String email,
//            @RequestParam("password") String password,
//            @RequestParam("confirmPassword") String confirmPassword,
//            Model model) {
//		
//		if (!password.equals(confirmPassword)) {
//            model.addAttribute("error", "비밀번호가 일치하지 않습니다.");
//            model.addAttribute("email", email);  // 다시 form에 유지
//            return "login/resetPassword";
//        }
//
//        // 비밀번호 업데이트 처리
//        try {
//            service.updatePassword(email, password);
//            model.addAttribute("success", "비밀번호가 성공적으로 변경되었습니다.");
//            return "redirect:/user/login";
//        } catch (Exception e) {
//            model.addAttribute("error", "비밀번호 변경 중 오류가 발생했습니다.");
//            model.addAttribute("email", email);
//            return "login/resetPassword";
//        }
//	}

}