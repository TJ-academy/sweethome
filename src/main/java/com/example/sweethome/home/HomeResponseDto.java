package com.example.sweethome.home;

import lombok.Data;

@Data
public class HomeResponseDto {
    private Home home;
    private Long likeCount;

    private Long reviewCount; //나래추가
    
    public HomeResponseDto(Home home, Long likeCount) {
        this.home = home;
        this.likeCount = likeCount;
        this.reviewCount = 0L; //나래추가
    }
    
    //나래추가
    public HomeResponseDto(Home home, Long likeCount, Long reviewCount) {
        this.home = home;
        this.likeCount = likeCount;
        this.reviewCount = reviewCount;
    }
}
