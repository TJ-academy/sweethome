package com.example.sweethome.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.example.sweethome.util.LoginInterceptor;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowedHeaders("*");
    }

    // 로그인 인터셉터 등록
    @Bean
    public LoginInterceptor loginInterceptor() {
        return new LoginInterceptor();
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor())
                .addPathPatterns("/**")  // 모든 요청에 대해 인터셉터 적용
                .excludePathPatterns(
                        // 로그인 하지 않은 사용자가 접근 가능한 경로
                        "/", "/home", "/user/login", "/user/join", "/user/resetPassword", "/user/findPwd",
                        "/user/checkEmailDuplicate", "/user/checkNicknameDuplicate",
                        "/css/**", "/js/**", "/img/**", "/images/**", "/favicon.ico",
                        "/kakao_login_medium_narrow.png"
                );
    }
}
