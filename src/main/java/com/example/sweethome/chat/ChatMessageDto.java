package com.example.sweethome.chat;

import com.example.sweethome.user.User;

import lombok.Data;

@Data
public class ChatMessageDto {
	private Integer roomId;
    private User sender;
    private User receiver;
    private String content;
    private String img = "-";
}