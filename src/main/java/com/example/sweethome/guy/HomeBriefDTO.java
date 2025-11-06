package com.example.sweethome.guy;

import com.example.sweethome.home.Home;

public record HomeBriefDTO(
        int id,
        String title,
        String address,
        String thumbnail,   // 서버는 '/img/...' 로 줄 것 (프런트가 절대 URL로 변환)
        Integer costBasic
) {
    public static HomeBriefDTO from(Home h) {
        return new HomeBriefDTO(
                h.getIdx(),
                h.getTitle(),
                h.getAddress(),
                h.getThumbnail(),
                h.getCostBasic()
        );
    }
}
