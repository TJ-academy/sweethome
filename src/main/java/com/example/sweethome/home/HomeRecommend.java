package com.example.sweethome.home;

import com.example.sweethome.home.Home; 
import com.example.sweethome.user.User; 

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

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HomeRecommend {

    // recomId (int, PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int recomId;

    // homeId (int, FK) -> Home 엔티티와 ManyToOne 관계
    @ManyToOne 
    @JoinColumn(name = "homeId", referencedColumnName = "idx", nullable = false)
    private Home home;

    // recommender (varchar(50), FK, 추천인) -> User 엔티티와 ManyToOne 관계
    // User 엔티티의 PK인 email을 참조합니다.
    @ManyToOne
    @JoinColumn(name = "recommender", referencedColumnName = "email", nullable = false)
    private User recommender;

    // status (bit, 0: 비추천, 1: 추천)
    // boolean 타입으로 매핑하는 것이 일반적이며, DB에서는 tinyint(1) 또는 bit로 저장됩니다.
    private boolean status;
}