package com.example.sweethome.payment;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class IamportAccessToken {
    private String access_token; //발급된 access 토큰 값
    private long now; //토큰 발급 시점 timestamp
    private long expired_at; //토큰 만료 시점 timestamp
}