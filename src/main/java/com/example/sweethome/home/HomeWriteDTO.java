package com.example.sweethome.home;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

@Data
public class HomeWriteDTO {

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

    // 체크박스 값이 Integer든 Long이든 service에서 변환 가능
    private List<Long> optionIds;  
}
