package com.example.sweethome.guy;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.ssong.JwtUtil;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationRepository reservationRepository;
    private final JwtUtil jwtUtil;

    @GetMapping("/my")
    public ResponseEntity<?> myReservations(HttpServletRequest req) {
        Optional<String> tokenOpt = getTokenFromCookie(req);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("ok", false, "message", "토큰 없음"));
        }

        try {
            Claims claims = jwtUtil.validateTokenAndGetEmail(tokenOpt.get()).getBody();
            String email = claims.getSubject();

            List<Reservation> list = reservationRepository
                    .findByBookerEmailOrderByReservedDateDesc(email);

            List<ReservationSummaryDTO> dto = list.stream()
                    .map(ReservationSummaryDTO::from)
                    .toList();

            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("ok", false, "message", "유효하지 않은 토큰"));
        }
    }

    @GetMapping("/{reservationIdx}")
    public ResponseEntity<?> myReservationDetail(@PathVariable("reservationIdx") int reservationIdx,
                                                 HttpServletRequest req) {
        Optional<String> tokenOpt = getTokenFromCookie(req);
        if (tokenOpt.isEmpty()) {
            return ResponseEntity.status(401)
                    .body(Map.of("ok", false, "message", "토큰 없음"));
        }

        try {
            Claims claims = jwtUtil.validateTokenAndGetEmail(tokenOpt.get()).getBody();
            String email = claims.getSubject();

            Optional<Reservation> opt = reservationRepository.findById(reservationIdx);
            if (opt.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("ok", false, "message", "예약을 찾을 수 없습니다."));
            }

            Reservation r = opt.get();
            if (!r.getBooker().getEmail().equals(email)) {
                return ResponseEntity.status(403)
                        .body(Map.of("ok", false, "message", "접근 권한이 없습니다."));
            }

            return ResponseEntity.ok(ReservationSummaryDTO.from(r));
        } catch (Exception e) {
            return ResponseEntity.status(401)
                    .body(Map.of("ok", false, "message", "유효하지 않은 토큰"));
        }
    }

    private Optional<String> getTokenFromCookie(HttpServletRequest req) {
        Cookie[] cookies = req.getCookies();
        if (cookies == null) return Optional.empty();
        return Arrays.stream(cookies)
                .filter(c -> "ACCESS_TOKEN".equals(c.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }
}
