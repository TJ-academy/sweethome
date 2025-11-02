package com.example.sweethome.home;

import java.util.ArrayList;
import java.util.List;

import com.example.sweethome.user.User;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Home {

    // idx (int, PK, auto increment)
	@Transient
	private long recommendCount;


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int idx;

    // hostId (FK) -> User 엔티티와 관계 매핑
    @ManyToOne 
    @JoinColumn(name = "hostId", referencedColumnName = "email")
    private User host;

    // title (varchar(200))
    @Column(length = 200, nullable = false)
    private String title;

    // description (text)
    @Column(columnDefinition = "text")
    private String description;

    // location (varchar(100))
    @Column(length = 100)
    private String location;

    // address (varchar(200))
    @Column(length = 200)
    private String address;

    // costBasic (int)
    private int costBasic;

    // costExpen (int) - 주말 요금
    private int costExpen;

    // homeType (enum)
    @Enumerated(EnumType.STRING)
    private HomeType homeType;

    // thumbnail (varchar(100))
    @Column(length = 100)
    private String thumbnail;

    // maxPeople (int)
    private int maxPeople;

    // room (int) - 방 개수
    private int room;
    
	// bath (int) - 욕실 개수
    private Integer bath;
    
    // bet (int) - 침대 개수
    private Integer bed;

    // checkIn (int) - 체크인 시간 (시간 정수 값)
    private int checkIn;

    // checkout (int) - 체크아웃 시간 (시간 정수 값)
    private int checkOut;
    
 // ⭐️ HomePhoto와의 1:1 관계 추가
    // mappedBy = "home": HomePhoto 엔티티의 'home' 필드가 관계의 주인이므로 매핑을 위임합니다.
    @OneToOne(mappedBy = "home") 
    private HomePhoto homePhoto; // <--- 이 필드가 있어야 home.getHomePhoto() 사용 가능
    
    // ⭐️ Hashtag와의 1:1 관계 추가 (Hashtag 엔티티 수정 필요)
    @OneToOne(mappedBy = "home") 
    private Hashtag hashtag; // <--- 이 필드가 있어야 home.getHashtag() 사용 가능

    @OneToMany(mappedBy = "home")
    private List<HomeRecommend> homeRecommends = new ArrayList<>();

}

// HomeType의 ENUM 타입 정의 (숙소 타입 반영)
/*
enum HomeType {
    PENSION, // 펜션
    DETACHED_HOUSE, // 단독 주택
    APARTMENT, // 아파트
    HOTEL, // 호텔
    MOTEL, // 모텔
    VILLA // 빌라
}*/