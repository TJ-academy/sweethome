package com.example.sweethome.home;

import java.io.Serializable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

// 복합키를 정의하는 클래스
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode // 동등성 및 해시 코드 구현 필수
public class AccommodationOptionId implements Serializable {

    private static final long serialVersionUID = 1L;

    // AccommodationOption 엔티티의 필드 이름과 일치해야 함
    private int home; // 매핑될 Home 엔티티 필드 이름
    private int option; // 매핑될 Option 엔티티 필드 이름
}