package com.example.sweethome.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.example.sweethome.home.Home;
import com.example.sweethome.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
public class Reservation {

    // reservationIdx (int, PK)
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int reservationIdx;

    // booker (varchar(50), FK)
    @ManyToOne 
    @JoinColumn(name = "booker", referencedColumnName = "email", nullable = false)
    private User booker;

    // reservedHome (int, FK)
    @ManyToOne
    @JoinColumn(name = "reservedHome", referencedColumnName = "idx", nullable = false)
    private Home reservedHome;

    // adult (int)
    private int adult;

    // child (int)
    private int child;

    // pet (int) - 반려동물 수
    private int pet;
    
    // reservedDate (date, timestamp)
    @Column(columnDefinition = "timestamp")
    private LocalDateTime reservedDate;

    // message (varchar(200))
    @Column(length = 200)
    private String message;

    // **[수정]** condition (enum) -> 예약 상태 필드 이름을 reservationStatus로 변경
    @Enumerated(EnumType.STRING)
    private ReservationStatus reservationStatus;

    // payby (enum) -> 결제 수단
    @Enumerated(EnumType.STRING)
    private PaymentMethod payby;

    // bank (varchar(50)) -> 은행명
    @Column(length = 50)
    private String bank;

    // account (Long) -> 계좌
    private Long account;
    
   // totalMoney (int) -> 총 결제금액
    private int totalMoney;

    // startDate (date) -> 입실일자
    private LocalDate startDate;

    // endDate (date) -> 퇴실일자
    private LocalDate endDate;
    
    private String memoForHost;
    
    // 💡 추가: 취소 신청 사유 필드
    @Column(length = 500) // 넉넉하게 500자로 설정
    private String cancelMessage;
    
    private String memoForCheckIn;
    private String memoForCheckOut;
    
    /*------결제 관련 필드---------------------*/
	// 아임포트 주문 번호 (서버에서 생성, 고유값)
    @Column(unique = true, length = 40)
    private String merchantUid; 

    // 아임포트 결제 고유 번호 (결제 성공 시 아임포트에서 부여)
    @Column(length = 40)
    private String impUid;
}