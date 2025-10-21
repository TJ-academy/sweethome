package com.example.sweethome.mypage;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
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
        // 세션에서 현재 로그인한 사용자 정보 가져오기
        User user = (User) session.getAttribute("userProfile");

        // 로그인되지 않았다면 로그인 페이지로 리다이렉트
        if (user == null) {
            return "redirect:/user/login";
        }

        // 사용자의 예약 정보를 조회
        List<Reservation> reservations = reservationRepository.findByBooker(user);

        // user 정보를 마이페이지 모델에 전달
        model.addAttribute("user", user);
        // 예약 목록을 모델에 추가
        model.addAttribute("reservations", reservations);

        return "mypage/myReservation"; // 예약 페이지로 이동
    }
}
