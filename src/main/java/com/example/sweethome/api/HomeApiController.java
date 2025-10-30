package com.example.sweethome.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.sweethome.home.HomeResponseDto;
import com.example.sweethome.home.HomeService;

/**
 * Flutter 클라이언트에 JSON 데이터를 제공하는 API 컨트롤러
 */
@RestController
@RequestMapping("/api/homes")
public class HomeApiController {

    private final HomeService homeService;

    @Autowired
    public HomeApiController(HomeService homeService) {
        this.homeService = homeService;
    }

    //메인페이지 데이터
    @GetMapping("/main")
    public Map<String, List<HomeResponseDto>> getMainHomeData() {
        List<HomeResponseDto> homeList = homeService.getHomeListWithLikeCounts();
        List<HomeResponseDto> seoulHomeList = homeService.getSeoulPopularHomesWithLikeCounts();
        List<HomeResponseDto> longTermHomeList = homeService.getLongTermPopularHomesWithLikeCounts();
        List<HomeResponseDto> largeHomeList = homeService.getLargePopularHomesWithLikeCounts();

        Map<String, List<HomeResponseDto>> response = new HashMap<>();
        response.put("allHomes", homeList);
        response.put("seoulPopularHomes", seoulHomeList);
        response.put("longTermPopularHomes", longTermHomeList);
        response.put("largePopularHomes", largeHomeList);

        return response;
    }

    
}