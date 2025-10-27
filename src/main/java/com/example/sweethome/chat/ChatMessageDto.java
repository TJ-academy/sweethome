package com.example.sweethome.chat;

import java.time.Instant;

import lombok.Data;

@Data
public class ChatMessageDto {
	private Integer msgId;
	private Integer roomId;
    private String senderEmail;
    private String senderNickname;
    private String receiverEmail;
    private String receiverNickname;
    private String content;
    private String img = "-";
    private Instant sendedAt;
}