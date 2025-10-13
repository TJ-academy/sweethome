package com.example.sweethome;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;

@Controller
public class HomeController {
	@GetMapping("/")
	public String basic(HttpSession session, Model model) {
		User userProfile = (User) session.getAttribute("userProfile");
	    if (userProfile != null) {
	        model.addAttribute("userProfile", userProfile);
	    }
		return "home";
	}
	
	@GetMapping("/home")
	public String home(HttpSession session, Model model) {
		User userProfile = (User) session.getAttribute("userProfile");
	    if (userProfile != null) {
	        model.addAttribute("userProfile", userProfile);
	    }
		return "home";
	}
}