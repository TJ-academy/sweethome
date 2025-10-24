package com.example.sweethome.reservation;

public enum ReservationStatus {
 REQUESTED, // 예약 요청됨
 CANCEL_REQUESTED, // 취소 요청됨
 CANCELLED, // 취소됨
 REJECTED, // 예약 거절됨
 CONFIRMED, // 예약 확정됨
 IN_USE, // 이용 중
 COMPLETED // 이용 완료
}
