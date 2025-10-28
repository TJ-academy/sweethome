package com.example.sweethome.user.noti;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import com.example.sweethome.user.User;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class NotificationService {
	private final NotificationRepository repo;
    private final SimpMessagingTemplate messagingTemplate;

    // DB 저장 + 실시간 전송
    public void sendNotification(User receiver, 
    		String title, 
    		String message) {
        Notification n = Notification.builder()
                .receiver(receiver)
                .title(title)
                .message(message)
                .status(NotificationStatus.NEW)
                .alertLevel(AlertLevel.INFO)
                .sendAt(LocalDateTime.now())
                .build();

        repo.save(n);

        messagingTemplate.convertAndSend(
                "/topic/notifications/" + receiver.getEmail(),
                n
        );
    }

    public List<Notification> getNotifications(String email) {
        return repo.findByReceiverEmailOrderBySendAtDesc(email);
    }

    public void markAsRead(int notificationId) {
        repo.findById(notificationId).ifPresent(n -> {
            n.setStatus(NotificationStatus.READ);
            repo.save(n);
        });
    }
}