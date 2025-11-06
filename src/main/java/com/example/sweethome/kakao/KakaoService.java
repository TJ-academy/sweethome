package com.example.sweethome.kakao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;

import com.example.sweethome.kakao.dto.KakaoProfile;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.netty.handler.codec.http.HttpHeaderValues;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class KakaoService {
	private final WebClient kakaoAuthWebClient;
    private final WebClient kakaoWebClient;
    
    @Value("${kakao.client_id}")
    private String client_id;
    
    @Value("${kakao.redirect_uri}")
    private String redirect_uri;
    
    @Value("${kakao.logout_redirect_uri}")
    private String logout_redirect_uri;
    
    private final String TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private final String USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";
    private final String UNLINK_URL = "https://kapi.kakao.com/v1/user/unlink";

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public KakaoService() {
    	this.kakaoAuthWebClient = WebClient.builder()
                .baseUrl("https://kauth.kakao.com")
                .build();

        this.kakaoWebClient = WebClient.builder()
                .baseUrl("https://kapi.kakao.com")
                .build();
    }

    public String getAccessTokenFromKakao(String code) {
        
        KakaoTokenResponseDto kakaoTokenResponseDto = kakaoAuthWebClient.post()
        		.uri("/oauth/token")
                .header(HttpHeaders.CONTENT_TYPE, "application/x-www-form-urlencoded")
                .bodyValue("grant_type=authorization_code" +
                           "&client_id=" + client_id + 
                           "&redirect_uri=" + redirect_uri + 
                           "&code=" + code)
                .retrieve()
                //TODO : Custom Exception
                .onStatus(HttpStatusCode::is4xxClientError, clientResponse -> Mono.error(new RuntimeException("Invalid Parameter")))
                .onStatus(HttpStatusCode::is5xxServerError, clientResponse -> Mono.error(new RuntimeException("Internal Server Error")))
                .bodyToMono(KakaoTokenResponseDto.class)
                .block();

        return kakaoTokenResponseDto.getAccessToken();
    }
    
    
    public KakaoProfile getUserInfo(String accessToken) {

    	KakaoProfile userInfo = kakaoWebClient
                .get()
                .uri(uriBuilder -> uriBuilder
                        .scheme("https")
                        .path("/v2/user/me")
                        .build(true))
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken) // access token 인가
                .header(HttpHeaders.CONTENT_TYPE, HttpHeaderValues.APPLICATION_X_WWW_FORM_URLENCODED.toString())
                .retrieve()
                .bodyToMono(KakaoProfile.class)
                .block();

        return userInfo;
    }
    
    public void unlinkKakao(String accessToken) {
    	kakaoWebClient
                .post()
                .uri("/v1/user/unlink")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        log.info("[KakaoService] Kakao account unlinked successfully");
    }
}