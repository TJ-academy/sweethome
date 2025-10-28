package com.example.sweethome.review;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReplyRepository extends JpaRepository<Reply, Integer> {
    
    // 특정 리뷰에 달린 답글을 조회합니다. 답글은 리뷰당 하나만 허용됩니다.
    Optional<Reply> findByReview(Review review);
}