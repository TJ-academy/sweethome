package com.example.sweethome.mypage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.reservation.ReservationStatus;
import com.example.sweethome.review.ReviewDirection;
import com.example.sweethome.review.ReviewRepository;
import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequiredArgsConstructor
@RequestMapping("/mypage")
public class MyReservationController {

    private final ReservationRepository reservationRepository;
    private final ReviewRepository reviewRepository;
    
    @GetMapping("/reservation")
    public String myReservation(HttpSession session, Model model) {
    	User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";

        // 사용자의 예약 정보를 조회
        List<Reservation> reservations = reservationRepository.findByBooker(user);
        
        // 오늘 날짜 (로컬 기준)
        LocalDate today = LocalDate.now();

        // 결과 컨테이너
        List<Reservation> upcoming = new ArrayList<>();
        List<Reservation> completed = new ArrayList<>();
        List<Reservation> cancelled = new ArrayList<>();

        for (Reservation r : reservations) {
            // 1) 취소 관련 상태 먼저 분류
            ReservationStatus s = r.getReservationStatus();
            if (s == ReservationStatus.CANCEL_REQUESTED
             || s == ReservationStatus.CANCELLED
             || s == ReservationStatus.REJECTED) {
                cancelled.add(r);
                continue;
            }

            // 2) 그 외는 endDate 기준으로 완료/다가오는
            // endDate가 오늘 이전이면 완료, 그 외(오늘 포함 이후)는 다가오는
            if (r.getEndDate() != null && r.getEndDate().isBefore(today)) {
                completed.add(r);
            } else {
                upcoming.add(r);
            }
        }

        // 보기 좋게 최근순 정렬(옵션) — 다가오는: 시작일 빠른순, 완료·취소: 종료일 최신순
        upcoming.sort((a,b) -> a.getStartDate().compareTo(b.getStartDate()));
        completed.sort((a,b) -> b.getEndDate().compareTo(a.getEndDate()));
        cancelled.sort((a,b) -> b.getEndDate().compareTo(a.getEndDate()));

        model.addAttribute("user", user);
        model.addAttribute("upcoming", upcoming);
        model.addAttribute("completed", completed);
        model.addAttribute("cancelled", cancelled);

        return "mypage/myReservation"; // 예약 페이지로 이동
    }
    
    @GetMapping("/reservation/detail")
    public String reservationDetail(@RequestParam("reservationIdx") int reservationIdx, HttpSession session, Model model) {
    	
    	User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";
        
        // 1. 예약 정보 조회
        Optional<Reservation> reservationOpt = reservationRepository.findById(reservationIdx);
        
        if (reservationOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다.");
        }
        
        Reservation reservation = reservationOpt.get();
        
        // 2. 예약자가 현재 로그인된 사용자인지 확인 (보안 체크)
        // user Entity에서 email이 PK이므로, booker.getEmail()로 비교
        if (!reservation.getBooker().getEmail().equals(user.getEmail())) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
        
        // 3. 모델에 추가
        model.addAttribute("user", user);
        model.addAttribute("reservation", reservation);
        
     // 예약 상세 컨트롤러 예시
        boolean guestReviewWritten =
                reviewRepository.existsByReservationAndDirection(reservation, ReviewDirection.GUEST_TO_HOST);

        // ★ 반드시 모델에 Boolean 값으로 내려주기
        model.addAttribute("guestReviewWritten", guestReviewWritten);
        
        return "mypage/myReservationDetail";
    }
    
    /**
     * 예약 취소 신청 처리
     */
    @PostMapping("/reservation/cancel/{reservationIdx}")
    @Transactional
    public String cancelReservation(@PathVariable("reservationIdx") int reservationIdx, HttpSession session) {
        
    	User user = (User) session.getAttribute("userProfile");
        if (user == null) return "redirect:/user/login";
        
        Reservation reservation = reservationRepository.findById(reservationIdx)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약 정보를 찾을 수 없습니다."));

        // 예약자가 현재 로그인된 사용자인지 확인 (보안 체크)
        if (!reservation.getBooker().getEmail().equals(user.getEmail())) {
             throw new ResponseStatusException(HttpStatus.FORBIDDEN, "취소 권한이 없습니다.");
        }
        
        // 취소 신청이 가능한 상태인지 확인 (예: CONFIRMED 또는 REQUESTED 상태만 가능)
        ReservationStatus status = reservation.getReservationStatus();
        if (status == ReservationStatus.CONFIRMED || status == ReservationStatus.REQUESTED) {
            reservation.setReservationStatus(ReservationStatus.CANCEL_REQUESTED);
            reservationRepository.save(reservation);
        } else {
             // 이미 취소되었거나, 완료된 예약 등 취소 불가능한 상태
             throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "현재 상태에서는 취소 신청할 수 없습니다.");
        }

        // 예약 상세 페이지로 리다이렉트 (상태가 업데이트된 것을 보여주기 위해)
        return "redirect:/mypage/reservation/detail?reservationIdx=" + reservationIdx;
    }
}
