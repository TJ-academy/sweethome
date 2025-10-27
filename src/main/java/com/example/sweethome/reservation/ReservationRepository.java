package com.example.sweethome.reservation;

import java.time.LocalDate;
import java.util.List;

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
    """)
    List<LocalDate> findCheckInsBetween(@Param("host") User host,
                                        @Param("start") LocalDate start,
                                        @Param("end") LocalDate end);

    @Query("""
      select r.endDate from Reservation r
      where r.reservedHome.host = :host
        and r.endDate >= :start and r.endDate < :end
    """)
    List<LocalDate> findCheckOutsBetween(@Param("host") User host,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);

    @Modifying
    void deleteByReservedHome_Idx(int homeIdx);

}
