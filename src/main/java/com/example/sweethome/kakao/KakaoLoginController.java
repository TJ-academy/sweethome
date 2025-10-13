package com.example.sweethome.kakao;

import java.io.IOException;
import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sweethome.kakao.dto.KakaoProfile;
import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
//@RestController
@RequiredArgsConstructor
@RequestMapping("/kakao")
public class KakaoLoginController {
	private final KakaoService kakaoService;
	private final UserRepository userRepo;
	
	@GetMapping("/callback")
	public String callback(@RequestParam("code") String code,
			Model model, HttpSession session) throws IOException {
		//카카오에서 엑세스 토큰 받기
		String accessToken = kakaoService.getAccessTokenFromKakao(code);
		
		//액세스 토큰으로 사용자 정보 받아오기
		KakaoProfile userInfo = kakaoService.getUserInfo(accessToken);
		
		//이메일로 회원 검색
		Optional<User> isUser = userRepo.findByEmail(userInfo.getKakaoAccount().getEmail());
		//System.out.println(userInfo);
		//회원이 아니라면 회원가입
		if (isUser.isEmpty()) {
			session.setAttribute("kakaouser", userInfo);
			return "redirect:/user/join";
		} else {
			//회원이면 home.html로 이동
			session.setAttribute("userProfile", isUser.get());
	        return "redirect:/";
		}
		
	}
}