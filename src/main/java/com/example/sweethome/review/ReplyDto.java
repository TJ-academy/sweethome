package com.example.sweethome.review;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReplyDto {
    private final String content;
    private final String hostUsername; // 호스트 닉네임
    
    // Reply 엔티티를 DTO로 변환하는 정적 메서드
    public static ReplyDto fromEntity(Reply reply) {
        String hostUsername = reply.getHost() != null ? reply.getHost().getUsername() : "호스트";
        
        return ReplyDto.builder()
                .content(reply.getContent())
                .hostUsername(hostUsername)
                .build();
    }
}