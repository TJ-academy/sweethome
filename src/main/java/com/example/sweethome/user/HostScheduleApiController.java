package com.example.sweethome.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sweethome.reservation.CalendarEventDTO;
import com.example.sweethome.reservation.DayScheduleItemDTO;
import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HostScheduleApiController {

    private final ReservationRepository reservationRepository;

    /**
     * FullCalendar가 자동으로 start, end 쿼리스트링을 줍니다.
     * 예) /api/schedules?start=2025-10-01&end=2025-11-01
     */
    @GetMapping("/schedules")
    public ResponseEntity<?> getSchedules(
            HttpSession session,
            @RequestParam(value = "start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
            @RequestParam(value = "end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end
    ) {
        User host = (User) session.getAttribute("userProfile");
        if (host == null) return ResponseEntity.status(401).build();

        // 호스트의 숙소 예약 중 달력 표시 구간과 겹치는 것만
        List<Reservation> reservations = reservationRepository
                .findOverlappingByHostAndRange(host, start, end);

        List<CalendarEventDTO> events = new ArrayList<>();

        for (Reservation r : reservations) {
            // 체크인/아웃 시간을 보기 좋게 고정(원하면 DB/설정에서 가져오세요)
            LocalDateTime checkIn  = r.getStartDate().atTime(LocalTime.of(15, 0)); // 15:00
            LocalDateTime checkOut = r.getEndDate().atTime(LocalTime.of(11, 0));   // 11:00

            // ① 체류(숙박기간) 블록 이벤트 (파스텔 블루)
            events.add(new CalendarEventDTO(
                    String.format("🏠 %s (%s님)", r.getReservedHome().getTitle(), r.getBooker().getNickname()),
                    checkIn,
                    checkOut,
                    "#E7F1FF",
                    Map.of(
                        "reservationId", r.getReservationIdx(),
                        "homeTitle",     r.getReservedHome().getTitle(),
                        "guest",         r.getBooker().getNickname(),
                        "status",        String.valueOf(r.getReservationStatus()),
                        "memo",          Optional.ofNullable(r.getMemoForHost()).orElse("")
                    )
            ));

            // ② 체크인 포인트(파란색)
            events.add(new CalendarEventDTO(
                    String.format("체크인 - %s님", r.getBooker().getNickname()),
                    checkIn,
                    checkIn.plusHours(1),  // 1시간짜리 포인트표시
                    "#007bff",
                    Map.of(
                        "type", "checkin",
                        "reservationId", r.getReservationIdx(),
                        "homeTitle",     r.getReservedHome().getTitle()
                    )
            ));

            // ③ 체크아웃 포인트(빨간색)
            events.add(new CalendarEventDTO(
                    String.format("체크아웃 - %s님", r.getBooker().getNickname()),
                    checkOut,
                    checkOut.plusHours(1),
                    "#dc3545",
                    Map.of(
                        "type", "checkout",
                        "reservationId", r.getReservationIdx(),
                        "homeTitle",     r.getReservedHome().getTitle()
                    )
            ));
        }

        return ResponseEntity.ok(events);
    }

    /**
     * 우측 패널에 특정 날짜의 상세 일정을 띄우는 용도 (선택).
     * 예) /api/schedules/day?date=2025-10-02
     */
    @GetMapping("/schedules/day")
    public ResponseEntity<?> getDaySchedules(
            HttpSession session,
            @RequestParam(value = "date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ) {
        User host = (User) session.getAttribute("userProfile");
        if (host == null) return ResponseEntity.status(401).build();

        // 하루치 범위 계산
        LocalDate start = date;
        LocalDate end   = date.plusDays(1);

        List<Reservation> reservations = reservationRepository
                .findOverlappingByHostAndRange(host, start, end);

        List<DayScheduleItemDTO> items = new ArrayList<>();
        for (Reservation r : reservations) {
            LocalDateTime checkInTime  = r.getStartDate().atTime(15, 0);
            LocalDateTime checkOutTime = r.getEndDate().atTime(11, 0);

            items.add(new DayScheduleItemDTO(
                    r.getReservationIdx(),
                    r.getReservedHome().getTitle(),
                    r.getBooker().getNickname(),
                    r.getStartDate(),
                    r.getEndDate(),
                    checkInTime,
                    checkOutTime,
                    r.getMemoForHost(),
                    String.valueOf(r.getReservationStatus())
            ));
        }
        // 시간순 정렬(체크인/체크아웃 시간 기준)
        items.sort(Comparator.comparing(DayScheduleItemDTO::getCheckInTime));

        return ResponseEntity.ok(items);
    }
}
