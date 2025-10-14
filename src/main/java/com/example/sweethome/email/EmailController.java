package com.example.sweethome.email;

import java.io.UnsupportedEncodingException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/user/mailConfirm")
    public String mailConfirm(@RequestParam("email") String email, 
    		HttpSession session) {
        try {
        	emailService.sendEmail(email, session);
            return "메일이 전송되었습니다.";
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace(); // 또는 로깅
            return "메일 전송을 실패했습니다.";
        }
    }
    
    @PostMapping("/user/verifyEmailCode")
    public String verifyEmailCode(@RequestParam("inputCode") String inputCode, 
    		HttpSession session) {
        String code = (String) session.getAttribute("emailCode");
        Long codeTime = (Long) session.getAttribute("emailCodeTime");
        
        if (code == null || codeTime == null) {
            return "인증 코드가 존재하지 않습니다. 다시 요청해주세요.";
        }
        
        long currentTime = System.currentTimeMillis();
        long timeElapsed = currentTime - codeTime;

        //10분안에 입력해야 함.
        if (timeElapsed > 10 * 60 * 1000L) {
        	//만료되었으면 세션에서 삭제
            session.removeAttribute("emailCode");
            session.removeAttribute("emailCodeTime");
            return "인증 코드가 만료되었습니다. 다시 요청해주세요.";
        }

        if (code.equals(inputCode)) {
        	//인증 성공
            session.setAttribute("emailVerified", true);
            session.removeAttribute("emailCode");
            session.removeAttribute("emailCodeTime");
            
            return "인증이 완료되었습니다.";
        } else {
            return "코드가 일치하지 않습니다.";
        }
    }
}