package com.example.sweethome.chat;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer idx; // 자동 증가 PK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chatRoomId", nullable = false)
    private ChatRoom chatRoom;

    private String sender;
    private String content;
    
    @Column(length = 100, columnDefinition = "varchar(100) default '-'")
	private String img = "-";
    
    private Instant sendedAt;
}