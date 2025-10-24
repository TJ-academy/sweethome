package com.example.sweethome.mypage;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;
import com.example.sweethome.user.UserService;
import com.example.sweethome.util.FileHandlerService;

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
	private final FileHandlerService fileHandlerService;
	
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
	
	// MypageController.java

	@GetMapping("/updateForm")
	public String updateForm(HttpSession session, Model model) {
	    User user = (User) session.getAttribute("userProfile");
	    if (user == null) {
	        return "redirect:/user/login";
	    }
	    // 최신값 보장을 원하면 DB에서 다시 조회해도 OK
	    // user = userRepo.findById(user.getEmail()).orElse(user);

	    model.addAttribute("user", user);
	    return "mypage/mypageUpdateForm"; // 템플릿 파일명과 일치
	}
	
	 // ✅ 수정 처리 (닉네임, 전화번호, 비밀번호(선택), 프로필 이미지 업로드)
    @PostMapping("/update")
    public String updateProfile(
            HttpSession session,
            Model model,
            @RequestParam("email") String email, // hidden
            @RequestParam(value = "newPassword", required = false) String newPassword,
            @RequestParam(value = "confirmPassword", required = false) String confirmPassword,
            @RequestParam("nickname") String nickname,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage
    ) {

        // 1) 로그인 사용자 검증
        User loginUser = getLoggedInUser(session);
        if (!loginUser.getEmail().equals(email)) {
            model.addAttribute("error", "잘못된 요청입니다.");
            model.addAttribute("user", loginUser);
            return "mypage/updateForm";
        }

        // 2) 최신 사용자 엔티티 조회
        Optional<User> opt = userRepo.findByEmail(email);
        if (opt.isEmpty()) {
            model.addAttribute("error", "사용자 정보를 찾을 수 없습니다.");
            return "redirect:/user/login";
        }
        User user = opt.get();

        // 3) 비밀번호 변경 (선택)
        if (newPassword != null && !newPassword.isBlank()) {
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "새 비밀번호와 비밀번호 확인이 일치하지 않습니다.");
                model.addAttribute("user", user);
                return "mypage/updateForm";
            }
            // 서비스에 위임(해시 포함)
            userService.updatePassword(email, newPassword);
        }

        // 4) 닉네임 변경 시 중복 검사 (자기 자신 제외)
        if (!nickname.equals(user.getNickname())) {
            boolean exists = userRepo.existsByNickname(nickname);
            if (exists) {
                model.addAttribute("error", "이미 사용 중인 닉네임입니다.");
                model.addAttribute("user", user);
                return "mypage/updateForm";
            }
            user.setNickname(nickname);
        }

        // 5) 전화번호 변경
        if (phone != null) {
            user.setPhone(phone.trim());
        }

        // 6) 프로필 이미지 업로드 (선택)
        try {
            if (profileImage != null && !profileImage.isEmpty()) {
                String savedPath = fileHandlerService.saveFile(profileImage, "userProfile");
                user.setProfileImg(savedPath);
            }
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", "프로필 이미지 업로드 중 오류가 발생했습니다.");
            return "mypage/updateForm";
        }

        // 7) 저장
        userRepo.save(user);

        // 8) 세션 갱신 (마이페이지/헤더 등에 최신 정보 반영)
        session.setAttribute("userProfile", user);

        // 9) 성공 후 마이페이지로
        model.addAttribute("user", user);
        model.addAttribute("success", "회원정보가 수정되었습니다.");
        return "redirect:/mypage";
    }

}