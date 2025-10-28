package com.example.sweethome.review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.user.User;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
	
    Optional<Review> findByReservationAndDirection(Reservation reservation, ReviewDirection direction);
    
    boolean existsByReservationAndDirection(Reservation reservation, ReviewDirection direction);
    
    @Modifying
    void deleteByHome_Idx(int homeIdx);
    
    // 내가 쓴 리뷰
    List<Review> findByWriterOrderByCreatedAtDesc(User writer);

    // 나에 대한 리뷰 (내가 호스트일 때: 게스트가 쓴 → HOST 대상)
    List<Review> findByDirectionAndReservation_ReservedHome_HostOrderByCreatedAtDesc(
            ReviewDirection direction, User host);

    // 나에 대한 리뷰 (내가 게스트일 때: 호스트가 쓴 → GUEST 대상)
    List<Review> findByDirectionAndReservation_BookerOrderByCreatedAtDesc(
            ReviewDirection direction, User booker);
    
    /**
     * 특정 호스트의 이메일과 리뷰 방향을 기준으로 리뷰 목록을 조회합니다.
     * Review -> Home (필드명: home) -> Host (필드명: host) -> Email (필드명: email)
     */
    List<Review> findByHomeHostEmailAndDirection(String hostEmail, ReviewDirection direction);
    
    /**
     * 특정 예약 번호(ReservationIdx)와 리뷰 방향(Direction)에 해당하는 Review를 조회합니다.
     * * 가정된 엔티티 관계:
     * Review.reservation -> Reservation (엔티티 필드명: reservation)
     * Reservation.reservationIdx (필드명: reservationIdx)
     */
    Optional<Review> findByReservationReservationIdxAndDirection(int reservationIdx, ReviewDirection direction);
}
