package com.example.sweethome.user;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/host")
@RequiredArgsConstructor
public class HostController {

	@GetMapping("/list")
	public String list(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userProfile");
		if (user == null)
			return "redirect:/user/login";

		model.addAttribute("user", user);

		return "host/myHomeList";
	}

	@GetMapping("/calendar")
	public String calendar(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userProfile");
		if (user == null)
			return "redirect:/user/login";

		model.addAttribute("user", user);
		return "host/calendar";
	}

	@GetMapping("/today")
	public String today(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userProfile");
		if (user == null)
			return "redirect:/user/login";

		model.addAttribute("user", user);

		return "host/today";
	}
}
