package com.example.sweethome.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sweethome.user.User;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {

	// 사용자(email)를 기준으로 예약 목록 조회
	List<Reservation> findByBooker(User user);

	// ★ 호스트의 숙소들에 대한 예약 중, 달력 보이는 기간(start~end)과 겹치는 예약만 조회
	@Query("""
			SELECT r
			FROM Reservation r
			WHERE r.reservedHome.host = :host
			  AND r.startDate < :rangeEnd
			  AND r.endDate > :rangeStart
			""")
	List<Reservation> findOverlappingByHostAndRange(@Param("host") User host, @Param("rangeStart") LocalDate rangeStart,
			@Param("rangeEnd") LocalDate rangeEnd);
	
	// 호스트 기준, 해당 '날짜'가 체크인인 예약
    @Query("""
        select r from Reservation r
        where r.reservedHome.host = :host
          and r.startDate = :date
    """)
    List<Reservation> findCheckInsByHostAndDate(@Param("host") User host,
                                                @Param("date") LocalDate date);

    // 호스트 기준, 해당 '날짜'가 체크아웃인 예약
    @Query("""
        select r from Reservation r
        where r.reservedHome.host = :host
          and r.endDate = :date
    """)
    List<Reservation> findCheckOutsByHostAndDate(@Param("host") User host,
                                                 @Param("date") LocalDate date);
    
 // ReservationRepository.java
    @Query("""
    	      select r.startDate from Reservation r
    	      where r.reservedHome.host = :host
    	        and r.startDate >= :start and r.startDate < :end
    	        and r.reservationStatus NOT IN ('CANCEL_REQUESTED', 'CANCELLED', 'REJECTED')
    	    """)
    List<LocalDate> findCheckInsBetween(@Param("host") User host,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);

    @Query("""
    	      select r.endDate from Reservation r
    	      where r.reservedHome.host = :host
    	        and r.endDate >= :start and r.endDate < :end
    	        and r.reservationStatus NOT IN ('CANCEL_REQUESTED', 'CANCELLED', 'REJECTED')
    	    """)
    List<LocalDate> findCheckOutsBetween(@Param("host") User host,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);

    @Modifying
    void deleteByReservedHome_Idx(int homeIdx);
    
    
    // ✅ 호스트(User)를 통해 해당 호스트의 모든 예약을 찾는 올바른 쿼리 메서드
    List<Reservation> findByReservedHome_Host(User host);
    
 // ------------------- HostController 탭 기능 구현을 위한 추가 메서드 -------------------

    /**
     * 1. 오늘 탭 데이터: 오늘 날짜 기준으로 숙박 중인 (체크인, 숙박 중, 체크아웃) 확정 예약 조회
     * 조건: (시작일 <= 오늘 < 종료일) & 상태가 CONFIRMED 또는 IN_USE
     * @param host 호스트 User 객체
     * @param today 오늘 날짜
     * @return 오늘의 예약 리스트 (체크인, 숙박 중, 체크아웃 포함)
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.reservedHome.host = :host
          AND r.startDate <= :today
          AND r.endDate > :today
          AND r.reservationStatus IN ('CONFIRMED', 'IN_USE')
    """)
    List<Reservation> findTodayBookingsForHost(@Param("host") User host, 
                                               @Param("today") LocalDate today);

    /**
     * 2. 예정 탭 데이터: 오늘 날짜 이후 체크인 예정인 확정된 예약 조회 (최대 30일 이내)
     * 조건: 시작일 > 오늘 & 상태가 CONFIRMED
     * (HostController에서 기간 필터링은 프론트엔드 또는 추가 비즈니스 로직으로 처리됨)
     * @param host 호스트 User 객체
     * @param today 오늘 날짜
     * @return 예정된 체크인 예약 리스트
     */
    @Query("""
        SELECT r
        FROM Reservation r
        WHERE r.reservedHome.host = :host
          AND r.startDate > :today
          AND r.reservationStatus = 'CONFIRMED'
        ORDER BY r.startDate ASC
    """)
    List<Reservation> findUpcomingCheckInsByHost(@Param("host") User host, 
                                                  @Param("today") LocalDate today);

    /**
     * 3. 예약 탭 데이터: 특정 예약 상태 목록에 해당하는 예약 조회 (예: 요청 대기, 취소 요청)
     * 조건: 상태가 주어진 목록(List<ReservationStatus>) 안에 포함
     * @param host 호스트 User 객체
     * @param statuses 조회할 예약 상태 목록
     * @return 요청/취소 요청 예약 리스트
     */
    List<Reservation> findReservationsByReservedHome_HostAndReservationStatusIn(
    		User host, List<ReservationStatus> statuses);
    
 // 호스트 기준, 해당 '날짜'가 체크인인 '활성' 예약 (취소/거절 제외)
    @Query("""
        select r from Reservation r
        where r.reservedHome.host = :host
          and r.startDate = :date
          and r.reservationStatus NOT IN ('CANCEL_REQUESTED', 'CANCELLED', 'REJECTED')
    """)
    List<Reservation> findActiveCheckInsByHostAndDate(@Param("host") User host,
                                                      @Param("date") LocalDate date);

    // 호스트 기준, 해당 '날짜'가 체크아웃인 '활성' 예약 (취소/거절 제외)
    @Query("""
        select r from Reservation r
        where r.reservedHome.host = :host
          and r.endDate = :date
          and r.reservationStatus NOT IN ('CANCEL_REQUESTED', 'CANCELLED', 'REJECTED')
    """)
    List<Reservation> findActiveCheckOutsByHostAndDate(@Param("host") User host,
                                                       @Param("date") LocalDate date);

    Optional<Reservation> findByMerchantUid(String merchantUid);
}
