package com.example.sweethome.mypage;

import java.time.LocalDateTime;

import com.example.sweethome.review.ReviewDirection;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class ReviewCardVM {
    Integer id;             // reviewIdx
    Integer reservationId;  // detail 링크에 필요
    String  homeTitle;      
    Double  rating;         // 뱃지 표시에 double 선호하면 double, 아니면 int로
    String  content;
    LocalDateTime createdAt;
    String reviewerNickname; // "나에 대한 후기"에서 작성자(상대방) 닉네임 표시
    ReviewDirection direction;
}
