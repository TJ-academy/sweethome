package com.example.sweethome.chat;

import lombok.Data;

@Data
public class ChatMessageDto {
	private Integer roomId;
    private String sender;
    private String receiver;
    private String content;
    private String img = "-";
}