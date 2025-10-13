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

        if (code != null && code.equals(inputCode)) {
            session.setAttribute("emailVerified", true); // 인증 성공
            return "인증이 완료되었습니다.";
        } else {
            return "코드가 일치하지 않습니다.";
        }
    }
}