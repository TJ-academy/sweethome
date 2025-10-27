package com.example.sweethome.home;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class HomeWriteDTO {
	private int idx;
    private String hostId;
    private String title;
    private String description;
    private String location;
    private String address;
    private String detailAddress;
    private int costBasic;
    private int costExpen;
    private String homeType;
    private MultipartFile thumbnail;
    private int maxPeople;
    private int room;
    private int bath;
    private int bed;
    private String checkIn;
    private String checkOut;

    private List<MultipartFile> homeImages;

    private String currentThumbnailPath; // URL 대신 Path라는 용어를 사용하겠습니다.
    
    private List<String> currentHomeImagePaths;
    
    // 체크박스 값이 Integer든 Long이든 service에서 변환 가능
    private List<Long> optionIds;  
    
    //Hastag 필드
    private boolean wifi;
    private boolean tv;
    private boolean kitchen; //주방
    private boolean freePark; // 무료 주차
    private boolean selfCheckin; // 셀프 체크인
    private boolean coldWarm; // 냉난방
    private boolean petFriendly; // 애견 동반
    private boolean barrierFree; // 휠체어 (장애물 없는 시설)
    private boolean elevator; // 엘리베이터
}