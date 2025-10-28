package com.example.sweethome.user.noti;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
	List<Notification> findByReceiverEmailOrderBySendAtDesc(String email);
}