package com.example.sweethome.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class Profile {
	//닉네임
    @JsonProperty("nickname")
	private String nickname;
    
  //프로필 미리보기 이미지 URL
    @JsonProperty("thumbnail_image_url")
	private String thumbnailImageUrl;
}