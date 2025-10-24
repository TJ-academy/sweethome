package com.example.sweethome.review;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.sweethome.reservation.Reservation;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    Optional<Review> findByReservationAndDirection(Reservation reservation, ReviewDirection direction);
    boolean existsByReservationAndDirection(Reservation reservation, ReviewDirection direction);
}
