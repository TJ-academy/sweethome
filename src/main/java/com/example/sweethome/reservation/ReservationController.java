package com.example.sweethome.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.sweethome.home.Home;
import com.example.sweethome.home.HomeRepository;
import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;
import com.example.sweethome.user.noti.NotificationService;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationRepository reservationRepository;
    private final UserRepository userRepository;
    private final HomeRepository homeRepository;
    private final NotificationService notiservice;

    // 💡 Merchant UID 생성 유틸 메서드
    private String generateMerchantUid() {
        return "R" + System.currentTimeMillis() + "_" + (int)(Math.random() * 10000);
    }

    // 예약 시작 페이지 (세션 제거, Model만 전달)
    @GetMapping("/reservationStart")
    public String reservationStart(
            @RequestParam("reservedHome") int reservedHomeId,
            @RequestParam("adult") int adultCount,
            @RequestParam("child") int childCount,
            @RequestParam("startDate") LocalDate checkInDate,
            @RequestParam("endDate") LocalDate checkOutDate,
            @RequestParam("totalMoney") int finalMoney,
            @RequestParam("nights") int nights,
            Model model) {

        // 숙소 조회
        Home home = homeRepository.findById(reservedHomeId)
                .orElseThrow(() -> new IllegalArgumentException("숙소가 존재하지 않습니다. idx=" + reservedHomeId));

        // 숙소 정보 및 예약 데이터 전달
        model.addAttribute("homeTitle", home.getTitle());
        model.addAttribute("homeThumbnail", home.getThumbnail());
        model.addAttribute("homeId", reservedHomeId);
        model.addAttribute("adults", adultCount);
        model.addAttribute("children", childCount);
        model.addAttribute("checkIn", checkInDate);
        model.addAttribute("checkOut", checkOutDate);
        model.addAttribute("totalPrice", finalMoney);
        model.addAttribute("nights", nights);

        return "home/reservationStart";
    }

    // 💡 계좌이체(TRANSFER) 예약 완료 처리
    @PostMapping("/reservationFinish")
    public String reservationFinish(ReservationForm form, HttpSession session) {

        // 카카오페이 요청은 여기서 처리하지 않음
        if ("KAKAOPAY".equals(form.getPayby().toString())) {
            return "redirect:/error?msg=InvalidPaymentFlow. Please use KakaoPay endpoint.";
        }

        // 로그인 사용자 가져오기
        User booker = (User) session.getAttribute("userProfile");

        // 숙소 조회
        Home reservedHome = homeRepository.findById(form.getReservedHomeId())
                .orElseThrow(() -> new RuntimeException("Home not found"));

        // 예약 정보 저장
        Reservation reservation = Reservation.builder()
                .booker(booker)
                .reservedHome(reservedHome)
                .adult(form.getAdult())
                .child(form.getChild())
                .pet(0)
                .reservedDate(LocalDateTime.now())
                .message(form.getMessage())
                .reservationStatus(ReservationStatus.REQUESTED) // 예약 요청됨
                .payby(form.getPayby())
                .bank(form.getBank())
                .account(form.getAccount())
                .totalMoney(form.getTotalMoney())
                .startDate(form.getStartDate())
                .endDate(form.getEndDate())
                .build();

        reservationRepository.save(reservation);

        // 알림 전송
        String homeName = reservedHome.getTitle().length() > 10
                ? reservedHome.getTitle().substring(0, 10) + "..."
                : reservedHome.getTitle();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd");
        String formattedStartDate = reservation.getStartDate().format(formatter);
        String formattedEndDate = reservation.getEndDate().format(formatter);
        String resDate = formattedStartDate + " ~ " + formattedEndDate;

        notiservice.sendNotification(booker,
                "\"" + homeName + "\" 예약 신청이 완료됐습니다.",
                homeName + ", " + resDate,
                "RESERVATION");

        return "redirect:/home/reservationSuccess";
    }

    // 💡 카카오페이 결제 준비 엔드포인트 (AJAX 전용)
    @PostMapping("/startKakaoPayReservation")
    @ResponseBody
    public ResponseEntity<?> startKakaoPayReservation(@RequestBody ReservationForm form, HttpSession session) {

        // 로그인 확인
        User booker = (User) session.getAttribute("userProfile");
        if (booker == null) {
            return new ResponseEntity<>(Collections.singletonMap("error", "로그인이 필요합니다."), HttpStatus.UNAUTHORIZED);
        }

        // 숙소 조회
        Home reservedHome = homeRepository.findById(form.getReservedHomeId())
                .orElseThrow(() -> new RuntimeException("Home not found"));

        // Merchant UID 생성
        String merchantUid = generateMerchantUid();

        // 임시 예약 저장 (결제 전)
        Reservation reservation = Reservation.builder()
                .booker(booker)
                .reservedHome(reservedHome)
                .adult(form.getAdult())
                .child(form.getChild())
                .pet(0)
                .reservedDate(LocalDateTime.now())
                .message(form.getMessage())
                .reservationStatus(ReservationStatus.REQUESTED) // 예약 요청됨 (결제 완료 후 확정됨)
                .payby(form.getPayby())
                .totalMoney(form.getTotalMoney())
                .startDate(form.getStartDate())
                .endDate(form.getEndDate())
                .merchantUid(merchantUid)
                .build();

        reservationRepository.save(reservation);

        // 클라이언트에 결제 정보 반환
        Map<String, Object> response = new HashMap<>();
        response.put("status", "KAKAOPAY_READY");
        response.put("merchantUid", merchantUid);
        response.put("amount", form.getTotalMoney());
        response.put("buyerName", booker.getNickname());
        response.put("buyerEmail", booker.getEmail());
        response.put("homeName", reservedHome.getTitle());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    // 예약 완료 페이지
    @GetMapping("/reservationSuccess")
    public String reservationSuccess(Model model, HttpSession session) {

        Object userProfile = session.getAttribute("userProfile");
        model.addAttribute("userProfile", userProfile);

        return "home/reservationFinish";
    }
}
