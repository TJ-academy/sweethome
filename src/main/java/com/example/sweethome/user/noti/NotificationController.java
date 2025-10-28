package com.example.sweethome.user.noti;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/notifications")
public class NotificationController {
	private final NotificationService service;

	//유저 알림 조회
    @GetMapping
    public List<Notification> getNotifications(HttpSession session) {
    	User user = (User) session.getAttribute("userProfile");
    	System.out.println("컨트롤러에서 알림 db: " + service.getNotifications(user.getEmail()));
        return service.getNotifications(user.getEmail());
    }

    //알림 읽음 처리
    @PostMapping("/{id}/read")
    public void markAsRead(@PathVariable("id") int id) {
    	service.markAsRead(id);
    }
}