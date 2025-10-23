package com.example.sweethome.reservation;

import java.time.LocalDateTime;
import java.util.Map;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CalendarEventDTO {
    private String title;
    private LocalDateTime start;
    private LocalDateTime end;
    private String color;                // 선택
    private Map<String, Object> extendedProps; // 상세(게스트, 숙소명 등)
}
