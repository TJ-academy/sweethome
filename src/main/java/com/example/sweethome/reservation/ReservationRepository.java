package com.example.sweethome.reservation;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
}
