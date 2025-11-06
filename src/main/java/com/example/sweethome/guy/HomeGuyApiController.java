package com.example.sweethome.guy;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.sweethome.home.HomeRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/homes")
@RequiredArgsConstructor
public class HomeGuyApiController {

    private final HomeRepository homeRepository;

    @GetMapping("/{id}")
    public ResponseEntity<?> getHome(@PathVariable("id") int id) {
        return homeRepository.findById(id)
                .<ResponseEntity<?>>map(h -> ResponseEntity.ok(HomeBriefDTO.from(h)))
                .orElseGet(() -> ResponseEntity.status(404)
                        .body(Map.of("ok", false, "message", "숙소를 찾을 수 없습니다.")));
    }
}
