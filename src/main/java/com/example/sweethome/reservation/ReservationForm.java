package com.example.sweethome.reservation;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

// 필요한 필드만 포함하는 DTO
// Lombok 사용을 가정
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ReservationForm {

    // 예약 상세 페이지에서 hidden으로 받은 필드
    private int reservedHomeId; // homeId (Reservation 엔티티의 reservedHome과 매핑)
    private int adult;
    private int child;
    private LocalDate startDate;
    private LocalDate endDate;
    private int totalMoney;
    
    // 사용자가 폼에서 입력한 필드
    private String message;
    private PaymentMethod payby; // Enum으로 받기
    private String bank;
    private Long account;
}