package com.example.sweethome.review;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewKeywordMapRepository extends JpaRepository<ReviewKeywordMap, ReviewKeywordMapId> {

    // 특정 리뷰에 연결된 키워드 매핑 전부 조회
    List<ReviewKeywordMap> findByReview(Review review);

    // 특정 리뷰의 키워드 매핑 전부 삭제(수정/삭제 시 유용)
    void deleteByReview(Review review);
}
