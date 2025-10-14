package com.example.sweethome.home;

import com.example.sweethome.home.Home;

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
@IdClass(AccommodationOptionId.class) // 복합키 클래스 지정
public class AccommodationOption {

    // accId (int, 숙소 ID) -> Home 엔티티 참조
    // @Id와 @ManyToOne을 함께 사용하여 복합키이자 FK로 매핑합니다.
    @Id
    @ManyToOne 
    @JoinColumn(name = "accId", referencedColumnName = "idx", nullable = false)
    private Home home;

    // optionId (int, 옵션 ID) -> Option 엔티티 참조
    // @Id와 @ManyToOne을 함께 사용하여 복합키이자 FK로 매핑합니다.
    @Id
    @ManyToOne
    @JoinColumn(name = "optionId", referencedColumnName = "optionId", nullable = false)
    private Option option;

    // exist (boolean, 존재 여부) - default false
    // columnDefinition으로 DB 스키마에 기본값 설정
    @Builder.Default // Builder 사용 시 기본값 설정
    @jakarta.persistence.Column(columnDefinition = "boolean default false", nullable = false)
    private boolean exist = false; 
}