package com.example.sweethome.review;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.sweethome.home.Home;
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
    
    // DetailController 에 쓰기 위한 추가 메서드
    
    /**
     * ✅ 홈에 대한 게스트의 리뷰 총 개수를 조회합니다. (GUEST_TO_HOST)
     * Review.home -> Home
     */
    int countByHomeAndDirection(Home home, ReviewDirection direction);

    /**
     * ✅ 홈에 대한 게스트 리뷰의 평균 별점을 조회합니다. (GUEST_TO_HOST)
     * Review.star 필드를 이용합니다.
     */
    @Query("SELECT AVG(r.star) FROM Review r WHERE r.home = :home AND r.direction = :direction")
    Double findAverageRatingByHomeAndDirection(@Param("home") Home home, @Param("direction") ReviewDirection direction);

    /**
     * ✅ 홈에 대한 게스트 리뷰 중 최신 4개를 조회합니다. (GUEST_TO_HOST)
     * PageRequest.of(0, 4)를 사용하여 LIMIT 4 효과를 냅니다.
     */
    List<Review> findByHomeAndDirectionOrderByCreatedAtDesc(Home home, ReviewDirection direction, PageRequest pageRequest);
    
    /**
     * ✅ 홈에 대한 게스트 리뷰 전체 목록을 최신순으로 조회합니다. (GUEST_TO_HOST)
     */
    List<Review> findByHomeAndDirectionOrderByCreatedAtDesc(Home home, ReviewDirection direction);
    
    /**
     * ✅ 홈에 대한 게스트 리뷰 전체 목록을 최신순으로 조회하며, 
     * 각 Review에 연결된 Reply를 즉시 로딩(Fetch Join)합니다.
     */
    @Query("SELECT r FROM Review r LEFT JOIN FETCH r.reply WHERE r.home = :home AND r.direction = :direction ORDER BY r.createdAt DESC")
    List<Review> findByHomeAndDirectionWithReply(@Param("home") Home home, @Param("direction") ReviewDirection direction);
    
    // detail 로딩 개선 중
    // ⭐️ ReviewRepository.java 파일에 추가
    @Query("SELECT r FROM Review r "
         + "JOIN FETCH r.writer w " // 작성자 정보(profileImg, username)를 한 번에 가져옴
         + "LEFT JOIN FETCH r.reply rp " // 답변 정보도 함께 가져옴 (답변이 없을 수 있으므로 LEFT JOIN)
         + "WHERE r.home = :home AND r.direction = :direction "
         + "ORDER BY r.createdAt DESC")
    List<Review> findRecentReviewsWithWriterAndReply(@Param("home") Home home, @Param("direction") ReviewDirection direction, Pageable pageable);
}
