package com.example.sweethome.reservation;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.sweethome.user.User;

public interface ReservationRepository extends JpaRepository<Reservation, Integer> {
	
	// 사용자(email)를 기준으로 예약 목록 조회
    List<Reservation> findByBooker(User user);
}
