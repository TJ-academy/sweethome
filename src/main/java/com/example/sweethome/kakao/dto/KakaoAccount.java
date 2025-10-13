package com.example.sweethome.kakao.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Data;

@Data
public class KakaoAccount {
	//닉네임 제공 동의 여부
	@JsonProperty("profile_nickname_needs_agreement")
    private Boolean profileNicknameNeedsAgreement;

    //프로필 사진 제공 동의 여부
	@JsonProperty("profile_image_needs_agreement")
    private Boolean profileImageNeedsAgreement;
    
    //이메일 제공 동의 여부
	@JsonProperty("email_needs_agreement")
    private Boolean emailNeedsAgreement;
    
    //이메일
	@JsonProperty("email")
    private String email;
    
	//사용자 프로필 정보
    @JsonProperty("profile")
    private Profile profile;
}