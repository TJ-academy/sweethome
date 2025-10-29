package com.example.sweethome.review;

import java.time.LocalDateTime;

import com.example.sweethome.user.User;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewDto {
    private final int idx;
    private final String content;
    private final int star;
    private final LocalDateTime createdAt;
    
    // 작성자 정보 (DTO 내부 DTO 또는 필요한 필드만)
    private final String writerUsername;
    private final String writerProfileImg;
    
    // Reply 정보 (순환 참조 방지)
    private final ReplyDto reply;

    // Review 엔티티를 DTO로 변환하는 정적 메서드
    public static ReviewDto fromEntity(Review review) {
        // null 체크를 통해 Lazy Loading 오류 및 NullPointerException 방지
        String username = review.getWriter() != null ? review.getWriter().getUsername() : "알 수 없는 사용자";
        String profileImg = review.getWriter() != null ? review.getWriter().getProfileImg() : null;
        
        // Reply도 DTO로 변환하여 포함 (Reply가 null일 수 있으므로 주의)
        ReplyDto replyDto = review.getReply() != null 
                            ? ReplyDto.fromEntity(review.getReply()) 
                            : null;
        
        return ReviewDto.builder()
                .idx(review.getReviewIdx())
                .content(review.getContent())
                .star(review.getStar())
                .createdAt(review.getCreatedAt())
                .writerUsername(username)
                .writerProfileImg(profileImg)
                .reply(replyDto)
                .build();
    }
}