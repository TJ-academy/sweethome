package com.example.sweethome.chat;

import java.io.Serializable;
import java.util.Objects;

import jakarta.persistence.Embeddable;

@Embeddable
public class ChatUserId implements Serializable {
	private Integer roomId;
    private String nickname;

    // 기본 생성자
    public ChatUserId() {}

    public ChatUserId(Integer roomId, String nickname) {
        this.roomId = roomId;
        this.nickname = nickname;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChatUserId)) return false;
        ChatUserId that = (ChatUserId) o;
        return Objects.equals(roomId, that.roomId) &&
               Objects.equals(nickname, that.nickname);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, nickname);
    }
}