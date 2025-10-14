package com.example.sweethome.review;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewKeyword {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int categoryIdx;

    // category (varchar) - 옵션 그룹 (예: '숙소 내부', '서비스/기타')
    @Column(length = 50, nullable = false) 
    private String category;

    // goodthing (varchar) - 키워드 내용 (예: '침대가 푹신해요', '답장이 빨라요')
    @Column(length = 100, nullable = false) 
    private String goodthing;
}