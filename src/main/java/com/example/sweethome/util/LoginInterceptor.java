package com.example.sweethome.util;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class LoginInterceptor implements HandlerInterceptor {
    
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 세션에서 로그인 여부 확인
        Object userProfile = request.getSession().getAttribute("userProfile");

        // 로그인하지 않은 경우
        if (userProfile == null) {
            // 로그인 페이지로 리다이렉트
            if (!request.getRequestURI().equals("/") && !request.getRequestURI().equals("/search")) {
                response.sendRedirect("/user/login");
                return false;  // 요청을 처리하지 않고 리다이렉트
            }
        }
        return true; // 로그인된 경우 요청을 계속 처리
    }
}
