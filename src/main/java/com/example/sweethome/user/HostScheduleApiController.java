package com.example.sweethome.user;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import com.example.sweethome.reservation.CalendarEventDTO;
import com.example.sweethome.reservation.DayScheduleItemDTO;
import com.example.sweethome.reservation.Reservation;
import com.example.sweethome.reservation.ReservationRepository;
import com.example.sweethome.reservation.ReservationStatus;

import jakarta.servlet.http.HttpSession;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class HostScheduleApiController {
	
	@Data @AllArgsConstructor
	static class DayMarkDTO {
	    private LocalDate date;
	    private boolean checkIn;
	    private boolean checkOut;
	}

	@GetMapping("/schedules/marks")
	public ResponseEntity<?> getDayMarks(
	        HttpSession session,
	        @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
	        @RequestParam("end")   @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end) {

	    User host = (User) session.getAttribute("userProfile");
	    if (host == null) return ResponseEntity.status(401).build();

	    // 날짜 → 마커 집계
	    Map<LocalDate, DayMarkDTO> map = new HashMap<>();

	    reservationRepository.findCheckInsBetween(host, start, end).forEach(d ->
	        map.merge(d, new DayMarkDTO(d, true, false),
	                  (a,b) -> new DayMarkDTO(d, a.isCheckIn()  || b.isCheckIn(),
	                                              a.isCheckOut() || b.isCheckOut()))
	    );

	    reservationRepository.findCheckOutsBetween(host, start, end).forEach(d ->
	        map.merge(d, new DayMarkDTO(d, false, true),
	                  (a,b) -> new DayMarkDTO(d, a.isCheckIn()  || b.isCheckIn(),
	                                              a.isCheckOut() || b.isCheckOut()))
	    );

	    return ResponseEntity.ok(new ArrayList<>(map.values()));
	}

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

        //List<CalendarEventDTO> events = new ArrayList<>();
        
        // 예약 상태가 'CANCEL_REQUESTED', 'CANCELLED', 'REJECTED'인 예약은 제외
        reservations = reservations.stream()
                .filter(r -> !r.getReservationStatus().equals(ReservationStatus.CANCEL_REQUESTED)
                        && !r.getReservationStatus().equals(ReservationStatus.CANCELLED)
                        && !r.getReservationStatus().equals(ReservationStatus.REJECTED))
                .collect(Collectors.toList());

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
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {

        User host = (User) session.getAttribute("userProfile");
        if (host == null) return ResponseEntity.status(401).build();

        // ✅ 정확히 일치로 분리 조회
        //List<Reservation> checkIns  = reservationRepository.findCheckInsByHostAndDate(host, date);
        //List<Reservation> checkOuts = reservationRepository.findCheckOutsByHostAndDate(host, date);
        
     // ✅ 정확히 일치로 분리 조회 (ReservationRepository에서 새로운 메서드 사용)
        List<Reservation> checkIns  = reservationRepository.findActiveCheckInsByHostAndDate(host, date);
        List<Reservation> checkOuts = reservationRepository.findActiveCheckOutsByHostAndDate(host, date);

        List<DayScheduleItemDTO> items = new ArrayList<>();

        for (Reservation r : checkIns) {
            items.add(new DayScheduleItemDTO(
                r.getReservationIdx(), r.getReservedHome().getTitle(), r.getBooker().getNickname(),
                r.getStartDate(), r.getEndDate(),
                r.getStartDate().atTime(15, 0),   // checkInTime
                null,                             // checkOutTime 없음
                null,                             // memoForHost 안씀
                r.getMemoForCheckIn(),            // ✅ 체크인 메모만
                null,                             // 체크아웃 메모 없음
                String.valueOf(r.getReservationStatus())
            ));
        }

        for (Reservation r : checkOuts) {
            items.add(new DayScheduleItemDTO(
                r.getReservationIdx(), r.getReservedHome().getTitle(), r.getBooker().getNickname(),
                r.getStartDate(), r.getEndDate(),
                null,
                r.getEndDate().atTime(11, 0),     // checkOutTime
                null,
                null,
                r.getMemoForCheckOut(),           // ✅ 체크아웃 메모만
                String.valueOf(r.getReservationStatus())
            ));
        }

        items.sort(Comparator.comparing(i -> {
            var t = i.getCheckInTime() != null ? i.getCheckInTime() : i.getCheckOutTime();
            return t != null ? t : LocalDateTime.MIN;
        }));

        return ResponseEntity.ok(items);
    }


    
    
    /** 호스트 권한 확인 유틸 */
    private void assertHostOwnership(User host, Reservation r) {
        if (host == null) throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED);
        String hostEmail = r.getReservedHome().getHost().getEmail();
        if (!hostEmail.equals(host.getEmail())) {
            throw new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "접근 권한이 없습니다.");
        }
    }

    /** 메모 조회 (선택적) */
    @GetMapping("/reservations/{id}/memo")
    public ResponseEntity<?> getMemo(HttpSession session, @PathVariable("id") Integer id) {
        User host = (User) session.getAttribute("userProfile");
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.NOT_FOUND));
        assertHostOwnership(host, r);
        return ResponseEntity.ok(Map.of("memo", Optional.ofNullable(r.getMemoForHost()).orElse("")));
    }

    @PutMapping("/reservations/{id}/memo")
    public ResponseEntity<?> updateMemo(HttpSession session,
            @PathVariable("id") Integer id,
            @RequestParam("type") String type,
            @RequestBody Map<String, String> body) {

        User host = (User) session.getAttribute("userProfile");
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        assertHostOwnership(host, r); // ✅ 여기!

        String memo = body.get("memo");
        if ("CHECKIN".equalsIgnoreCase(type)) {
            r.setMemoForCheckIn(memo);
        } else if ("CHECKOUT".equalsIgnoreCase(type)) {
            r.setMemoForCheckOut(memo);
        } else {
            return ResponseEntity.badRequest().body("Invalid type");
        }

        reservationRepository.save(r);
        return ResponseEntity.ok(Map.of("result", "ok"));
    }

    @DeleteMapping("/reservations/{id}/memo")
    public ResponseEntity<?> deleteMemo(HttpSession session,
            @PathVariable("id") Integer id,
            @RequestParam("type") String type) {

        User host = (User) session.getAttribute("userProfile");
        Reservation r = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        assertHostOwnership(host, r); // ✅ 여기!

        if ("CHECKIN".equalsIgnoreCase(type)) {
            r.setMemoForCheckIn(null);
        } else if ("CHECKOUT".equalsIgnoreCase(type)) {
            r.setMemoForCheckOut(null);
        } else {
            return ResponseEntity.badRequest().body("Invalid type");
        }

        reservationRepository.save(r);
        return ResponseEntity.ok(Map.of("result", "deleted"));
    }


    
    
    
    
    
    
    
    
    
}
