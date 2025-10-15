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
               .addPathPatterns("/**")  // 모든 경로에 대해 인터셉터 적용
               .excludePathPatterns("/", "/user/login", "/user/join", "/user/resetPassword", "/user/findPwd", "/user/checkEmailDuplicate", "/user/checkNicknameDuplicate",
            		   "/home/detail/**", "/img/home/**");  // 로그인하지 않아도 접근 가능한 경로
   }
}