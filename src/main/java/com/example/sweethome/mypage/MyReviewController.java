package com.example.sweethome.mypage;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/mypage")
public class MyReviewController {

	@GetMapping("/review")
	public String showReview(HttpSession session, Model model) {
		User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";
        
        model.addAttribute("user", user);
        
		return "mypage/myReviewMain";
	}
	
}
