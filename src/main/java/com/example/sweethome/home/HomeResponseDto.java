package com.example.sweethome.home;

import lombok.Data;

@Data
public class HomeResponseDto {
    private Home home;
    private Long likeCount;

    public HomeResponseDto(Home home, Long likeCount) {
        this.home = home;
        this.likeCount = likeCount;
    }
}
