package com.example.sweethome.review;

import java.util.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewKeywordRepository extends JpaRepository<ReviewKeyword, Integer> {

    // 카테고리로 조회
    List<ReviewKeyword> findByCategory(String category);

    // 여러 카테고리 한번에 조회
    List<ReviewKeyword> findAllByCategoryIn(Collection<String> categories);

    // 보기 좋게 정렬(카테고리, 키워드명)
    List<ReviewKeyword> findAllByOrderByCategoryAscGoodthingAsc();
}
