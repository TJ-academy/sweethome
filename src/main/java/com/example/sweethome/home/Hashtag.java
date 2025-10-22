package com.example.sweethome.home;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
public class Hashtag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;

    // homeIdx (int, FK) -> Home 엔티티와 ManyToOne 관계
    // 하나의 Home은 여러 개의 Hashtag 레코드를 가질 수 있습니다.
   // @ManyToOne
    //@JoinColumn(name = "homeIdx", referencedColumnName = "idx")
    //private Home home;

    @ManyToOne
    @JoinColumn(name = "home_idx", referencedColumnName = "idx", nullable = false)
    private Home home;

    
    // 해당 1, 미해당 0
    private boolean wifi;

    private boolean tv;

    private boolean kitchen; //주방

    private boolean freePark; // 무료 주차

    private boolean selfCheckin; // 셀프 체크인

    private boolean coldWarm; // 냉난방

    private boolean petFriendly; // 애견 동반

    private boolean barrierFree; // 휠체어 (장애물 없는 시설)

    private boolean elevator; // 엘리베이터
}