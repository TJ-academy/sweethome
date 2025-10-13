package com.example.sweethome.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KakaoProfile {
	//회원 번호
    @JsonProperty("id")
	private Long id;
    
  //서비스에 연결 완료된 시각. UTC
    @JsonProperty("connected_at")
	private String connectedAt;
    
  //카카오 계정 정보
    @JsonProperty("kakao_account")
	private KakaoAccount kakaoAccount;
}