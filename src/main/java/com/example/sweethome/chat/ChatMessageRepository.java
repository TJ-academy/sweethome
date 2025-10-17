package com.example.sweethome.chat;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer> {
	//특정 채팅방 메시지 시간 순으로 조회
	List<ChatMessage> findByChatRoom_IdOrderBySendedAtAsc(Integer chatRoomId);
	//특정 채팅방의 마지막 메시지
	Optional<ChatMessage> findFirstByChatRoom_IdOrderBySendedAtDesc(Integer chatRoomId);
}