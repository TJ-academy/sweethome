package com.example.sweethome.review;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 복합키를 정의하는 클래스 (PK: reviewIdx + categoryIdx)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // 동등성 및 해시 코드 구현 필수
public class ReviewKeywordMapId implements Serializable {

    private static final long serialVersionUID = 1L;

    // 매핑될 Review 엔티티의 필드 이름과 일치해야 함
    private int review; 
    
    // 매핑될 ReviewKeyword 엔티티의 필드 이름과 일치해야 함
    private int keyword; 
}