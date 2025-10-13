package com.example.sweethome.email;

import java.io.UnsupportedEncodingException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/login/mailConfirm")
    public String mailConfirm(@RequestBody EmailPostDto emailDto) {
        try {
            return emailService.sendEmail(emailDto.getEmail());
        } catch (MessagingException | UnsupportedEncodingException e) {
            e.printStackTrace(); // 또는 로깅
            return "메일 전송 실패";
        }
    }
}