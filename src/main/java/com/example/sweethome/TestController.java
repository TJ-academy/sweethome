package com.example.sweethome;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class TestController {

	public static class Hotel {
        public String name = "포항 영일대 오션뷰 숙소";
        public String title = "[포항] 영일대 오션뷰 온수풀/최대 10인/강아지 동반 가능";
        public int maxGuests = 10;
        public int numBedrooms = 3;
        public int numBeds = 3;
        public int numBathrooms = 2;
        public double rating = 4.9;
        public int reviewCount = 12;
        public String mainImageUrl = "https://cdn.eyesmag.com/wp-content/uploads/2016/02/14190913/airbnb-recommendation-list-01.jpg";
        public List<String> subImageUrls = Arrays.asList(
            "https://cdn.eyesmag.com/wp-content/uploads/2016/02/14190913/airbnb-recommendation-list-01.jpg", 
            "https://cdn.eyesmag.com/wp-content/uploads/2016/02/14190913/airbnb-recommendation-list-01.jpg", 
            "https://cdn.eyesmag.com/wp-content/uploads/2016/02/14190913/airbnb-recommendation-list-01.jpg", 
            "https://cdn.eyesmag.com/wp-content/uploads/2016/02/14190913/airbnb-recommendation-list-01.jpg"
        );
        public String description = "<p>※10월 11일 GRAND OPEN!! 공식적으로 등록된 펜션 숙소입니다.</p><p>객실은 영일대 해상 누각을 정면으로 바라보며...</p>";
        // 숨겨진 내용 추가 (더 길거나 전체 내용)
        public String fullDescription = 
            "<p>※10월 11일 GRAND OPEN!! 공식적으로 등록된 펜션 숙소입니다.</p>" + 
            "<p>※10월 한정 오픈 이벤트 50% 할인 (2박이상) 제공</p>" + 
            "<p>객실은 영일대 해상 누각을 정면으로 바라보며, 따뜻함이 느껴지도록 제작된 객실과 테라스가 있는 고급 숙소입니다.</p>" +
            "<p>답답하지 않은 세련된 실내 공간을 가지고 있으며, 어느 누구와 함께라도 힐링되는 만족감과 휴식을 경험하게 해줍니다. (이후 수많은 내용이 더 있습니다...)</p>";
        
        public long basePrice = 720000;
        public LocalDate checkInDate = LocalDate.of(2025, 10, 15);
        public LocalDate checkOutDate = LocalDate.of(2025, 10, 20);
        public int defaultGuestCount = 2;
        public String location = "대한민국, 경상북도 포항시";

        public Host host = new Host();
        public List<Room> rooms = Arrays.asList(new Room("퀸사이즈 침대", 1), new Room("더블 침대", 1));
        public List<ReviewStat> reviewStats = Arrays.asList(
                new ReviewStat("청결도", 4.5), new ReviewStat("정확도", 5.0), new ReviewStat("체크인", 4.5)
        );
        public List<Review> latestReviews = Arrays.asList(
                new Review("박*근", "호스트 활동 기간 3년", "모든 것이 완벽했고 오션뷰가 정말 좋았습니다..."),
                new Review("Anyeon", "호스트 활동 기간 3년", "깔끔하고 따뜻해서 만족스러웠습니다...")
        );
        public List<String> amenities = Arrays.asList("수영장", "와이파이", "주차장");
        public Rules rules = new Rules();
        public Safety safety = new Safety();
        public CancellationPolicy cancellationPolicy = new CancellationPolicy();
    }

    // Hotel 내부에서 사용하는 하위 더미 클래스들
    public static class Host {
        public String name = "Seonah Kim";
        public String profileImageUrl = "https://lh3.googleusercontent.com/proxy/DNVIwWacFoW3Za-pUNm8BiFDjLDOUAaq6y3dVk0TVXZSvlRvLGAqznzidRc1c7d-TqVhTxP8-h2D14HNgDEwfWvD0td6hQK1okNte93oCTs";
        public int memberSince = 3;
    }
    public static class Room {
        public String bedType;
        public int bedCount;
        public Room(String bedType, int bedCount) {
            this.bedType = bedType;
            this.bedCount = bedCount;
        }
    }
    public static class ReviewStat {
        public String label;
        public double score;
        public ReviewStat(String label, double score) {
            this.label = label;
            this.score = score;
        }
    }
    public static class Review {
        public String userName;
        public String userMemberSinceText;
        public String comment;
        public Review(String userName, String userMemberSinceText, String comment) {
            this.userName = userName;
            this.userMemberSinceText = userMemberSinceText;
            this.comment = comment;
        }
    }
    public static class Rules {
        public String checkInTime = "오후 3:00~오전 12:00";
        public String checkOutTime = "오전 11:00까지";
        public int guestLimit = 10;
    }
    public static class Safety {
        public String coDetector = "없음";
        public String indoorCamera = "있음";
        public String fireDetector = "있음";
    }
    public static class CancellationPolicy {
        public String summary = "24시간 동안 무료 취소가 가능합니다. 10월 24일 전에 취소 시 부분 환불 가능";
    }

    @GetMapping("/test")
    public String showHotelDetailWithDummyData(Model model) {
        
        // 1. 더미 Hotel 객체 생성 (모든 필드에 데이터를 채워넣습니다)
        Hotel dummyHotel = new Hotel();
        
        // 2. Model에 "hotel" 이라는 이름으로 객체를 추가합니다.
        //    이것이 Thymeleaf에서 ${hotel.name} 등으로 접근 가능하게 합니다.
        model.addAttribute("hotel", dummyHotel);
        
        // 3. 템플릿 이름 반환
        return "detail";
    }
}
