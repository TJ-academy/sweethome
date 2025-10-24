package com.example.sweethome.user;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.server.ResponseStatusException;

import com.example.sweethome.home.Home;
import com.example.sweethome.home.HomeRepository;
import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.reservation.ReservationStatus;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/host")
@RequiredArgsConstructor
public class HostController {

	private final HomeRepository homeRepository; // 🌟 HomeRepository 주입
	private final ReservationRepository reservationRepository;

    @GetMapping("/list")
    public String list(HttpSession session, Model model) {

        User user = (User) session.getAttribute("userProfile");
        if (user == null)
            return "redirect:/user/login";

        // 🌟 로그인된 호스트의 숙소 목록 조회
        List<Home> myHomes = homeRepository.findByHost(user);

        model.addAttribute("user", user);
        model.addAttribute("myHomes", myHomes); // 🌟 모델에 숙소 리스트 추가

        return "host/myHomeList";
    }

    /**
     * 숙소 상세 페이지
     */
    @GetMapping("/detail/{homeIdx}")
    public String detail(@PathVariable("homeIdx") int homeIdx, HttpSession session, Model model) {

        User user = (User) session.getAttribute("userProfile");
        if (user == null)
            return "redirect:/user/login";

        // 1. 숙소 정보 조회
        Optional<Home> homeOpt = homeRepository.findById(homeIdx);

        if (homeOpt.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "숙소 정보를 찾을 수 없습니다.");
        }

        Home home = homeOpt.get();

        // 2. 호스트가 현재 로그인된 사용자인지 확인 (보안 체크)
        if (!home.getHost().getEmail().equals(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }

        // 3. 모델에 추가
        model.addAttribute("user", user);
        model.addAttribute("home", home); // 🌟 숙소 상세 정보 추가

        return "host/myHomeDetail"; // myHomeDetail.html 템플릿으로 이동
    }

	@GetMapping("/calendar")
	public String calendar(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userProfile");
		if (user == null)
			return "redirect:/user/login";

		model.addAttribute("user", user);
		return "host/calendar";
	}

	@GetMapping("/today")
	public String today(HttpSession session, Model model) {

		User user = (User) session.getAttribute("userProfile");
		if (user == null)
			return "redirect:/user/login";

		model.addAttribute("user", user);

		return "host/today";
	}
	
	@org.springframework.web.bind.annotation.RequestMapping(
	        value = "/reservation/detail/{reservationId}",
	        method = {org.springframework.web.bind.annotation.RequestMethod.GET,
	                  org.springframework.web.bind.annotation.RequestMethod.POST}
	)
	public String reservationDetail(
	        @PathVariable("reservationId") int reservationId,
	        @org.springframework.web.bind.annotation.RequestParam(value = "action", required = false) String action,
	        jakarta.servlet.http.HttpSession session,
	        org.springframework.ui.Model model) {

	    User host = (User) session.getAttribute("userProfile");
	    if (host == null) return "redirect:/user/login";

	    Reservation r = reservationRepository.findById(reservationId)
	            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "예약을 찾을 수 없습니다."));

	    // 소유권 확인(로그인한 사용자가 해당 숙소의 호스트인지)
	    if (!r.getReservedHome().getHost().getEmail().equals(host.getEmail())) {
	        throw new ResponseStatusException(HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
	    }

	    // ---- POST 액션 처리 ----
	    if (action != null) {
	        ReservationStatus curr = r.getReservationStatus();

	        switch (action) {
	            case "confirm": // 예약 확인
	                if (curr == ReservationStatus.REQUESTED) {
	                    r.setReservationStatus(ReservationStatus.CONFIRMED);
	                }
	                break;

	            case "reject": // 예약 거절
	                if (curr == ReservationStatus.REQUESTED) {
	                    r.setReservationStatus(ReservationStatus.REJECTED);
	                }
	                break;

	            case "cancel_accept": // 취소 수락
	                if (curr == ReservationStatus.CANCEL_REQUESTED) {
	                    r.setReservationStatus(ReservationStatus.CANCELLED);
	                }
	                break;

	            case "cancel_reject": // 취소 거절 → 다시 예약 확정됨으로
	                if (curr == ReservationStatus.CANCEL_REQUESTED) {
	                    r.setReservationStatus(ReservationStatus.CONFIRMED);
	                }
	                break;

	            case "start_use": // 이용중으로 전환
	                if (curr == ReservationStatus.CONFIRMED) {
	                    r.setReservationStatus(ReservationStatus.IN_USE);
	                }
	                break;

	            case "complete": // 이용완료로 전환
	                if (curr == ReservationStatus.IN_USE) {
	                    r.setReservationStatus(ReservationStatus.COMPLETED);
	                }
	                break;
	        }

	        reservationRepository.save(r);
	        return "redirect:/host/reservation/detail/" + reservationId;
	    }

	    // ---- GET: 상세 화면 렌더 ----
	    model.addAttribute("user", host);
	    model.addAttribute("reservation", r);
	    model.addAttribute("home", r.getReservedHome());
	    return "host/reservationDetail";
	}
}
