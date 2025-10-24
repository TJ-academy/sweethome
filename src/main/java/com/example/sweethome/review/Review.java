package com.example.sweethome.review;

import com.example.sweethome.home.Home;
import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.user.User;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor @Builder
@Table( // ✅ 동일 예약 + 동일 방향 중복 작성 방지
    uniqueConstraints = @UniqueConstraint(columnNames = {"reservationIdx", "direction"})
)
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reviewIdx;

    // ✅ 예약: many-to-one (한 예약에 리뷰 최대 2개)
    @ManyToOne
    @JoinColumn(name = "reservationIdx", referencedColumnName = "reservationIdx", nullable = false)
    private Reservation reservation;

    // 숙소
    @ManyToOne
    @JoinColumn(name = "homeIdx", referencedColumnName = "idx", nullable = false)
    private Home home;

    // 작성자
    @ManyToOne
    @JoinColumn(name = "writer", referencedColumnName = "email", nullable = false)
    private User writer;

    // ✅ 리뷰 방향 (게스트→호스트, 호스트→게스트)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReviewDirection direction;

    @Column(length = 500, nullable = false)
    private String content;

    @Column(length = 100) private String reviewThumb;
    @Column(length = 100) private String imgOne;
    @Column(length = 100) private String imgTwo;
    @Column(length = 100) private String imgThree;

    @Column(nullable = false) private int star;

    @Column(columnDefinition = "timestamp") private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}


/*package com.example.sweethome.review;

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
*/