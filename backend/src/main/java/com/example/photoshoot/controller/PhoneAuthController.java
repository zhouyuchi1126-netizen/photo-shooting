package com.example.photoshoot.controller;

import com.example.photoshoot.model.User;
import com.example.photoshoot.service.UserService;
import com.example.photoshoot.util.PasswordEncryptor;
import org.springframework.web.bind.annotation.*;

import java.security.SecureRandom;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 手机号验证码登录（开发模拟版）
 * 真实上线需接入阿里云短信等服务
 */
@RestController
@RequestMapping("/api/auth/phone")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class PhoneAuthController {

    private final UserService userService;

    // 内存模拟：phone → code
    private final ConcurrentHashMap<String, String> codeStore = new ConcurrentHashMap<>();

    public PhoneAuthController(UserService userService) {
        this.userService = userService;
    }

    /** 发送验证码（模拟：固定返回 123456，日志打印真实码） */
    @PostMapping("/send-code")
    public Map<String, Object> sendCode(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        if (phone == null || !phone.matches("\\d{11}")) {
            return Map.of("success", false, "message", "请输入正确的11位手机号");
        }

        // 生成6位随机码
        String code = String.format("%06d", new SecureRandom().nextInt(1000000));
        codeStore.put(phone, code);
        System.out.println("[PhoneAuth] 验证码 " + code + " 已发送到 " + phone);

        return Map.of("success", true, "message", "验证码已发送（模拟码: " + code + "）");
    }

    /** 验证码登录 / 注册 */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> body) {
        String phone = body.get("phone");
        String code = body.get("code");

        if (phone == null || code == null) {
            return Map.of("success", false, "message", "手机号和验证码不能为空");
        }

        // 验证
        String stored = codeStore.get(phone);
        if (stored == null) {
            return Map.of("success", false, "message", "请先获取验证码");
        }
        if (!stored.equals(code)) {
            return Map.of("success", false, "message", "验证码错误");
        }

        // 使用后清除
        codeStore.remove(phone);

        // 查找或创建用户（用手机号作为用户名）
        User user = userService.findOrCreatePhoneUser(phone);
        if (user == null) {
            return Map.of("success", false, "message", "创建用户失败");
        }

        return Map.of(
                "success", true,
                "message", "登录成功",
                "username", user.getUsername(),
                "displayName", user.getDisplayName() != null ? user.getDisplayName() : user.getUsername(),
                "role", user.getRole() != null ? user.getRole() : "user",
                "token", "dummy-token"
        );
    }
}
