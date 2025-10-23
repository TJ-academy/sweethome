package com.example.sweethome.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DayScheduleItemDTO {
    private Integer reservationId;
    private String homeTitle;
    private String guestNickname;
    private LocalDate checkInDate;          // 날짜용
    private LocalDate checkOutDate;         // 날짜용
    private LocalDateTime checkInTime;      // 15:00 등
    private LocalDateTime checkOutTime;     // 11:00 등
    private String memoForHost;             // 호스트 메모
    private String status;                  // 예약상태
}
