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
            // 현재 요청 URI와 쿼리스트링을 결합하여 prevPage에 저장
            String currentRequestUri = request.getRequestURI();
            String queryString = request.getQueryString();
            String fullUrl = currentRequestUri + (queryString != null ? "?" + queryString : "");
            
            request.getSession().setAttribute("prevPage", fullUrl); // 세션에 저장
            
            // 로그인 페이지로 리다이렉트
            response.sendRedirect("/user/login");
            return false;
        }
        return true; // 로그인된 경우 요청을 계속 처리
    }
}
