package com.example.sweethome.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.sweethome.home.Home;
import com.example.sweethome.home.HomeRepository;
import com.example.sweethome.user.User;
import com.example.sweethome.user.UserRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/home")
@RequiredArgsConstructor
public class ReservationController {
	
	private final ReservationRepository reservationRepository; // Repository 주입 가정
    private final UserRepository userRepository; // User Repository 주입 가정
    private final HomeRepository homeRepository; // Home Repository 주입 가정

	@GetMapping("/reservationStart")
	public String reservationStart(
            // long 또는 Long으로 idx 값을 받습니다.
            @RequestParam("reservedHome") int reservedHomeId, 
            
            // int로 인원수를 받습니다.
            @RequestParam("adult") int adultCount, 
            @RequestParam("child") int childCount,
            
            // LocalDate로 날짜를 받습니다. (Spring이 자동으로 변환해 줍니다.)
            @RequestParam("startDate") LocalDate checkInDate,
            @RequestParam("endDate") LocalDate checkOutDate,
            
            // int 또는 Long으로 금액을 받습니다.
            @RequestParam("totalMoney") int finalMoney,
            
            // 필요한 경우 nights도 추가
            @RequestParam("nights") int nights,
            
            Model model) {

        // 1. 전달받은 데이터를 Model에 다시 담아 다음 페이지로 전달합니다.
        model.addAttribute("homeId", reservedHomeId);
        model.addAttribute("adults", adultCount);
        model.addAttribute("children", childCount);
        model.addAttribute("checkIn", checkInDate);
        model.addAttribute("checkOut", checkOutDate);
        model.addAttribute("totalPrice", finalMoney);
        model.addAttribute("nights", nights);

        // 2. reservationStart.html로 이동 (src/main/resources/templates/reservationStart.html 가정)
        return "home/reservationStart";
    }
	
	@PostMapping("/reservationFinish")
    public String reservationFinish(ReservationForm form, HttpSession session) { 
		// 1. 현재 로그인된 사용자 정보 (Booker) 가져오기: 세션에서 User 객체를 가져옵니다.
        User booker = (User) session.getAttribute("userProfile");

        // 2. 예약할 숙소(Home) 정보 가져오기
        Home reservedHome = homeRepository.findById(form.getReservedHomeId())
                .orElseThrow(() -> new RuntimeException("Home not found"));

        // 3. Reservation 엔티티 빌드 및 저장
        Reservation reservation = Reservation.builder()
                .booker(booker)
                .reservedHome(reservedHome)
                .adult(form.getAdult())
                .child(form.getChild())
                .pet(0) // 펫 정보는 폼에 없으므로 0으로 가정
                .reservedDate(LocalDateTime.now())
                .message(form.getMessage())
                
                // 초기 예약 상태는 요청됨(REQUESTED)으로 설정
                .reservationStatus(ReservationStatus.REQUESTED) 
                
                // 결제 정보 설정
                .payby(form.getPayby())
                .bank(form.getBank())
                .account(form.getAccount())
                
                .totalMoney(form.getTotalMoney())
                .startDate(form.getStartDate())
                .endDate(form.getEndDate())
                .build();

        reservationRepository.save(reservation);
        
        // 4. 예약 성공 페이지로 리다이렉트
        // 예약 완료 후에는 보통 PRG 패턴에 따라 리다이렉트합니다.
        return "redirect:/home/reservationSuccess";
    }
	
	@GetMapping("/reservationSuccess")
	public String reservationSuccess() {
		return "home/reservationFinish";
	}
}
