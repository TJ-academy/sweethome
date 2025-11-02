package com.example.sweethome.home;

import lombok.Data;
import lombok.NoArgsConstructor; // DTO 사용을 위해 추가

@Data
@NoArgsConstructor // 기본 생성자 추가
public class HomeResponseDto {
    
    // 1. Home 엔티티의 주요 필드
    private int idx;
    private String title;
    private String description;
    private String location;
    private String address;
    private int costBasic;
    private int costExpen;
    private String homeType; // HomeType.name()으로 저장
    private String thumbnail;
    private int maxPeople;
    private int room;
    private Integer bath;
    private Integer bed;
    private int checkIn;
    private int checkOut;
    private long recommendCount; // @Transient 필드도 DTO에 포함

    // 2. Lazy Loading 되는 Host(User) 엔티티 필드 (예: 이메일)
    private String hostEmail;

    // 3. 쿼리 결과로 추가되는 필드
    private Long likeCount;
    private Long reviewCount;
    
    // 4. 모든 정보를 받아 DTO 필드를 초기화하는 생성자 (Host 정보 포함)
    public HomeResponseDto(Home home, Long likeCount) {
        // Home 엔티티에서 값 복사
        this.idx = home.getIdx();
        this.title = home.getTitle();
        this.description = home.getDescription();
        this.location = home.getLocation();
        this.address = home.getAddress();
        this.costBasic = home.getCostBasic();
        this.costExpen = home.getCostExpen();
        this.homeType = home.getHomeType() != null ? home.getHomeType().name() : null;
        this.thumbnail = home.getThumbnail();
        this.maxPeople = home.getMaxPeople();
        this.room = home.getRoom();
        this.bath = home.getBath();
        this.bed = home.getBed();
        this.checkIn = home.getCheckIn();
        this.checkOut = home.getCheckOut();
        // this.recommendCount = home.getRecommendCount(); // @Transient 필드는 Service에서 설정해야 함
        
        // Host(User) 엔티티에서 필요한 필드 복사
        // 🚨 이 호출은 N+1 또는 LazyException을 유발하므로, HomeService에서 Fetch Join을 사용하도록 수정해야 합니다.
        // 현재는 에러를 피하기 위해 주석 처리하거나, Service 로직 수정 후 사용해야 합니다.
        // this.hostEmail = home.getHost().getEmail(); 
        
        // 쿼리 결과 값 설정
        this.likeCount = likeCount;
        this.reviewCount = 0L; // 기본값
    }
    
    // 리뷰 카운트까지 포함하는 생성자
    public HomeResponseDto(Home home, Long likeCount, Long reviewCount) {
        this.idx = home.getIdx();
        this.title = home.getTitle();
        // ... (나머지 home 필드 복사 로직) ...
        this.address = home.getAddress();
        this.thumbnail = home.getThumbnail();
        this.maxPeople = home.getMaxPeople();
        this.costBasic = home.getCostBasic();
        this.room = home.getRoom();
        this.bath = home.getBath();
        this.bed = home.getBed();
        this.checkIn = home.getCheckIn();
        this.checkOut = home.getCheckOut();
        this.hostEmail = home.getHost() != null ? home.getHost().getEmail() : null; // Host 필드 복사
        
        this.likeCount = likeCount;
        this.reviewCount = reviewCount;
    }
}

/*
 * package com.example.sweethome.home;
 * 
 * import lombok.Data;
 * 
 * @Data public class HomeResponseDto { //private Home home; 가희 수정... home 로딩문제
 * 개선
 * 
 * private int idx; private String title; private String address; private String
 * thumbnail; private int costBasic; private int maxPeople; private String
 * hostEmail; // 만약 호스트 정보가 필요하다면 (host.getEmail()) // ... Home에서 필요한 나머지 필드들을
 * 추가하세요 ... private int room; private Integer bath; private Integer bed;
 * 
 * private Long likeCount; private Long reviewCount; //나래추가
 * 
 * public HomeResponseDto(Home home, Long likeCount) { this.home = home;
 * this.likeCount = likeCount; this.reviewCount = 0L; //나래추가 }
 * 
 * //나래추가 public HomeResponseDto(Home home, Long likeCount, Long reviewCount) {
 * this.home = home; this.likeCount = likeCount; this.reviewCount = reviewCount;
 * } }
 */