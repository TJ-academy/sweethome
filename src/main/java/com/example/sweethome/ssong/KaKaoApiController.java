package com.example.sweethome.ssong;

import java.time.Duration;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sweethome.kakao.KakaoService;
import com.example.sweethome.kakao.dto.KakaoProfile;
import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/kakao")
@RequiredArgsConstructor
public class KaKaoApiController {
	private final KakaoService kakaoService;
    private final UserRepository userRepo;
    private final JwtUtil jwtUtil;

    @PostMapping("/login")
    public ResponseEntity<?> kakaoLogin(
    		@RequestParam("accessToken") String accessToken,
            HttpServletResponse res) {
        try {
            // 카카오 토큰으로 유저 정보 가져오기
            KakaoProfile userInfo = kakaoService.getUserInfo(accessToken);
            String email = userInfo.getKakaoAccount().getEmail();

            Optional<User> userOpt = userRepo.findByEmail(email);

            if (userOpt.isEmpty()) {
            	return ResponseEntity.status(401)
    					.body(Map.of("ok", false, 
    							"message", "등록되지 않은 이메일입니다."));
            }
            
            User user = userOpt.get();
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

        } catch (Exception e) {
        	return ResponseEntity.status(401)
					.body(Map.of("ok", false, 
							"message", "카카오 로그인 실패"));
        }
    }
}