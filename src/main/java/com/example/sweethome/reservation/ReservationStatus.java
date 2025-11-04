package com.example.sweethome.reservation;

public enum ReservationStatus {
 REQUESTED("예약 대기 중"), 
 CANCEL_REQUESTED("취소 요청 중"), 
 CANCELLED("취소 완료"), 
 REJECTED("예약 거절"), 
 CONFIRMED("예약 확정"), 
 IN_USE("이용 중"), 
 COMPLETED("이용 완료");
    
    // ✅ 1. 한글 이름을 저장할 필드 추가
    private final String displayName;

    // ✅ 2. 생성자 추가
    ReservationStatus(String displayName) {
        this.displayName = displayName;
    }
    
    // ✅ 3. 명시적인 public Getter 메서드 추가 (필수 수정)
    public String getDisplayName() {
        return displayName;
    }
}