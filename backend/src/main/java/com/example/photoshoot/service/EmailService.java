package com.example.photoshoot.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String adminEmail;
    private String currentCode = "";
    private long codeExpireTime = 0;

    public EmailService(JavaMailSender mailSender,
                        @Value("${app.admin.email}") String adminEmail) {
        this.mailSender = mailSender;
        this.adminEmail = adminEmail;
    }

    public String generateAndSendCode() {
        SecureRandom random = new SecureRandom();
        int code = 100000 + random.nextInt(900000);
        currentCode = String.valueOf(code);
        codeExpireTime = System.currentTimeMillis() + 5 * 60 * 1000;
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(adminEmail);
            msg.setSubject("管理员登录验证码");
            msg.setText("您的验证码是: " + currentCode + "\n有效期5分钟。");
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("邮件发送失败: " + e.getMessage());
        }
        return "验证码已发送到管理员邮箱";
    }

    public boolean verifyCode(String code) {
        if (System.currentTimeMillis() > codeExpireTime) return false;
        return currentCode.equals(code);
    }
}
