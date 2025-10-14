package com.example.sweethome.review;

import com.example.sweethome.review.Review; // Review 엔티티 import

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
@IdClass(ReviewKeywordMapId.class) // 복합키 클래스 지정
public class ReviewKeywordMap {

    // reviewIdx (FK) -> Review 엔티티 참조 (복합키 1)
    @Id
    @ManyToOne 
    @JoinColumn(name = "reviewIdx", referencedColumnName = "reviewIdx", nullable = false)
    private Review review;

    // categoryIdx (FK) -> ReviewKeyword 엔티티 참조 (복합키 2)
    @Id
    @ManyToOne
    @JoinColumn(name = "categoryIdx", referencedColumnName = "categoryIdx", nullable = false)
    private ReviewKeyword keyword;    
}