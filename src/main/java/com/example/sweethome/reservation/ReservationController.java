package com.example.sweethome.reservation;

import java.time.LocalDate;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/home/reservationStart")
@RequiredArgsConstructor
public class ReservationController {

	@GetMapping
	public String reservationStart(
            // long 또는 Long으로 idx 값을 받습니다.
            @RequestParam("reservedHome") Long reservedHomeId, 
            
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
}
