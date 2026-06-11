package com.example.photoshoot.controller;

import com.example.photoshoot.dto.AdminCodeRequest;
import com.example.photoshoot.dto.AdminCodeResponse;
import com.example.photoshoot.model.User;
import com.example.photoshoot.service.EmailService;
import com.example.photoshoot.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/auth")
public class AdminAuthController {

    private final EmailService emailService;
    private final UserService userService;
    private final String adminEmail;

    public AdminAuthController(EmailService emailService, UserService userService,
                                @Value("${app.admin.email}") String adminEmail) {
        this.emailService = emailService;
        this.userService = userService;
        this.adminEmail = adminEmail;
    }

    @PostMapping("/send-code")
    public AdminCodeResponse sendCode() {
        AdminCodeResponse resp = new AdminCodeResponse();
        try {
            String msg = emailService.generateAndSendCode();
            resp.setSuccess(true);
            resp.setMessage(msg);
        } catch (Exception e) {
            resp.setSuccess(false);
            resp.setMessage("发送失败: " + e.getMessage());
        }
        return resp;
    }

    @PostMapping("/verify-code")
    public AdminCodeResponse verifyCode(@RequestBody AdminCodeRequest request) {
        AdminCodeResponse resp = new AdminCodeResponse();
        if (request.getCode() == null || request.getCode().isBlank()) {
            resp.setSuccess(false); resp.setMessage("请输入验证码"); return resp;
        }
        if (!emailService.verifyCode(request.getCode().trim())) {
            resp.setSuccess(false); resp.setMessage("验证码错误或已过期"); return resp;
        }
        User admin = userService.findByUsername("admin");
        if (admin == null) {
            resp.setSuccess(false); resp.setMessage("管理员账号不存在"); return resp;
        }
        resp.setSuccess(true);
        resp.setUsername("admin");
        resp.setDisplayName("管理员");
        resp.setRole("admin");
        resp.setToken("admin-token");
        resp.setMessage("验证成功");
        return resp;
    }
}
