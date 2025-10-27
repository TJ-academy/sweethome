package com.example.sweethome.chat;

import com.example.sweethome.user.User;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
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
@IdClass(ChatUserId.class) //복합키 설정
public class ChatUser {
	@Id
	private Integer roomId;
	
	@Id
	@ManyToOne
    @JoinColumn(name = "user_email")
    private User user;
	
	private Integer lastRead;
}