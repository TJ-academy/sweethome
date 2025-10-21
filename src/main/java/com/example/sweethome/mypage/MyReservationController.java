package com.example.sweethome.mypage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.reservation.ReservationStatus;
import com.example.sweethome.user.User;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/mypage")
public class MyReservationController {

    private final ReservationRepository reservationRepository;

    public MyReservationController(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }
    
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
}
