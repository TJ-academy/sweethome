package com.example.sweethome.home;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/**
 * 숙소 등록 폼(write.html)에서 전송되는 데이터를 받기 위한 DTO
 * Home 엔티티와 HomePhoto 엔티티에 필요한 데이터를 모두 포함
 */
@Data
public class HomeWriteDTO {
    
    // ======================================================================
    // 1. Home 엔티티 필드
    // ======================================================================
    
    // hostId (Hidden input) - 세션에서 받지만, DTO에서도 받아서 사용
    private String hostId;

    // title
    private String title;

    // description (Summernote 내용)
    private String description;

    // location (지역)
    private String location;

    // address (다음 주소 검색 결과)
    private String address;

    // 상세 주소 address에 병합예정
    private String detailAddress; 

    // costBasic (기본 요금)
    private int costBasic;

    // costExpen (주말 요금)
    private int costExpen;

    // homeType (Enum 타입의 String 값)
    private String homeType; 
    
    // maxPeople (최대 인원)
    private int maxPeople;

    // room (방 갯수)
    private int room;
    
	// bath (욕실 갯수)
    private Integer bath;

    // checkIn (체크인 시간, 폼에서 time으로 받으면 String으로 들어옴. 
    // Home 엔티티의 int checkIn으로 변환 필요. 예: "15:00" -> 15)
    private String checkIn; 

    // checkout (체크아웃 시간, Home 엔티티의 int checkOut으로 변환 필요)
    private String checkOut;
    
    // ======================================================================
    // 2. 파일 업로드 필드 (MultipartFile)
    // ======================================================================

    // thumbnail (썸네일 파일 1개)
    private MultipartFile thumbnail;

    // 숙소 사진 (최대 10개)
    private MultipartFile imgOne;
    private MultipartFile imgTwo;
    private MultipartFile imgThree;
    private MultipartFile imgFour;
    private MultipartFile imgFive;
    private MultipartFile imgSix;
    private MultipartFile imgSeven;
    private MultipartFile imgEight;
    private MultipartFile imgNine;
    private MultipartFile imgTen;
}
