package com.example.sweethome.chat;

import java.time.Instant;

import lombok.Data;

@Data
public class ChatRoomPreviewDTO {
	private Integer roomId;
    private String roomName;
    private String lastMessage;   // 최근 메시지 10자 요약
    private Instant lastMessageTime;
    
    public ChatRoomPreviewDTO(Integer roomId, 
    		String roomName, 
    		String lastMessage, 
    		Instant lastMessageTime) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
    }
}