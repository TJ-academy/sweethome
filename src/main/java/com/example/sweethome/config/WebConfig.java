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
      registry.addMapping("/api/**").allowedOrigins("*")
      .allowedMethods("GET", "POST", "PUT", "DELETE")
      .allowedHeaders("*");
   }
   
   //로그인 인터셉터 등록
   @Bean
   public LoginInterceptor loginInterceptor() {
       return new LoginInterceptor();
   }

   @Override
   public void addInterceptors(InterceptorRegistry registry) {
       // 로그인 여부를 체크할 경로를 지정
	   registry.addInterceptor(loginInterceptor())
       .addPathPatterns("/**")
       .excludePathPatterns(
           // 기존 경로
           "/", "/home", "/user/login", "/user/join", "/user/resetPassword", "/user/findPwd", 
           "/user/checkEmailDuplicate", "/user/checkNicknameDuplicate",
           "/home/detail/**", 
           
           // 💡 필수 추가: 정적 리소스 폴더 제외 💡
           "/css/**",      // /static/css/ 경로의 모든 파일
           "/js/**",       // /static/js/ 경로의 모든 파일
           "/img/**",      // /static/img/ 경로의 모든 파일
           "/images/**",
           "/favicon.ico",
           
           // 단일 파일도 명시적으로 제외하거나, 해당 파일이 있는 폴더를 위에 포함
           "/kakao_login_medium_narrow.png", 
           "/img/home/**" 
       );
   }
}