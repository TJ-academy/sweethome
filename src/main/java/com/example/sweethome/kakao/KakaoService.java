package com.example.sweethome.kakao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.sweethome.kakao.dto.KakaoProfile;

import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {
	private String clientId;
	private String redirectUri;
    private final String KAUTH_TOKEN_URL_HOST;
    private final String KAUTH_USER_URL_HOST;

    @Autowired
    public KakaoService(
    		@Value("${kakao.client_id}") String clientId) {
    	this.clientId = clientId;
    	//this.redirectUri = redirectUri;
    	KAUTH_TOKEN_URL_HOST ="https://kauth.kakao.com";
    	KAUTH_USER_URL_HOST = "https://kapi.kakao.com";
    }

    public String getAccessTokenFromKakao(String code) {
        
        KakaoTokenResponseDto kakaoTokenResponseDto = WebClient.create(KAUTH_TOKEN_URL_HOST).post()
        		.uri("/oauth/token")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .bodyValue("grant_type=authorization_code" +
                           "&client_id=d0c2283e342f9018d35eb38e5f51fc04" +
                           "&redirect_uri=" + "http://homesweethome.koyeb.app/kakao/callback" + 
                           "&code=" + code)
                .retrieve()
                //TODO : Custom Exception
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();


//        log.info("[KakaoService] Access Token: {}", kakaoTokenResponseDto.getAccessToken());
//        log.info("[KakaoService] Refresh Token: {}", kakaoTokenResponseDto.getRefreshToken());
//        //제공 조건: OpenID Connect가 활성화 된 앱의 토큰 발급 요청인 경우 또는 scope에 openid를 포함한 추가 항목 동의 받기 요청을 거친 토큰 발급 요청인 경우
//        log.info("[KakaoService] ID Token: {}", kakaoTokenResponseDto.getIdToken());
//        log.info("[KakaoService] Scope: {}", kakaoTokenResponseDto.getScope());

        return kakaoTokenResponseDto.getAccessToken();
    }
    
    
    public KakaoProfile getUserInfo(String accessToken) {

    	KakaoProfile userInfo = WebClient.create(KAUTH_USER_URL_HOST)
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                //TODO : Custom Exception
//                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
//                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoProfile.class)
                .block();

//    	log.info("[KakaoService] User Info: {}", userInfo);
//        log.info("[KakaoService] Kakao ID: {}", userInfo.getId());
//        log.info("[KakaoService] Nickname: {}", userInfo.getKakaoAccount().getProfile().getNickname());
//        log.info("[KakaoService] Profile Image: {}", userInfo.getKakaoAccount().getProfile().getThumbnailImageUrl());

        return userInfo;
    }
    
    public void unlinkKakao(String accessToken) {
        WebClient.create(KAUTH_USER_URL_HOST)
                .post()
                .uri("/v1/user/unlink")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("[KakaoService] Kakao account unlinked successfully");
    }
}