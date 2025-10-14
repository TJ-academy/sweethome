package com.example.sweethome.review;

import com.example.sweethome.home.Home; 
import com.example.sweethome.reservation.Reservation; 
import com.example.sweethome.user.User; 

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType; 
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reviewIdx; 
    
    // ReservationIdx (FK) - 단순 외래 키로 변경, unique 속성 추가
    // 하나의 예약에 하나의 리뷰만 가능하도록 unique 제약조건
    @OneToOne 
    @JoinColumn(name = "reservationIdx", referencedColumnName = "reservationIdx", nullable = false, unique = true)
    private Reservation reservation;

    // homeIdx (FK)
    @ManyToOne 
    @JoinColumn(name = "homeIdx", referencedColumnName = "idx", nullable = false)
    private Home home;

    // writer (FK)
    @ManyToOne
    @JoinColumn(name = "writer", referencedColumnName = "email", nullable = false)
    private User writer;

    // content (varchar(500))
    @Column(length = 500, nullable = false)
    private String content;

    // reviewThumb (varchar(100))
    @Column(length = 100)
    private String reviewThumb;

    // imgOne ~ imgThree (varchar(100)) 
    @Column(length = 100)
    private String imgOne;

    @Column(length = 100)
    private String imgTwo;

    @Column(length = 100)
    private String imgThree;

    // star (int) - 별점
    @Column(nullable = false)
    private int star;
    
    // createdAt, updatedAt
    @Column(columnDefinition = "timestamp")
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
}