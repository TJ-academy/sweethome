package com.example.sweethome.home;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne; // 하나의 숙소에 하나의 사진 목록 레코드가 매칭된다고 가정
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
public class HomePhoto {

    // idx (int, PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;

    @OneToOne 
    @JoinColumn(name = "homeIdx", referencedColumnName = "idx", nullable = false)
    private Home home;

    // imgOne ~ imgTen (varchar(100))
    @Column(length = 100)
    private String imgOne;

    @Column(length = 100)
    private String imgTwo;

    @Column(length = 100)
    private String imgThree;

    @Column(length = 100)
    private String imgFour;

    @Column(length = 100)
    private String imgFive;

    @Column(length = 100)
    private String imgSix;

    @Column(length = 100)
    private String imgSeven;

    @Column(length = 100)
    private String imgEight;

    @Column(length = 100)
    private String imgNine;

    @Column(length = 100)
    private String imgTen;
}