package com.example.photoshoot.controller;

import com.example.photoshoot.model.User;
import com.example.photoshoot.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 微信扫码登录（开发模拟版）
 * 真实上线需接入微信开放平台
 */
@RestController
@RequestMapping("/api/auth/wechat")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175"})
public class WechatController {

    private final UserService userService;

    // 内存模拟：ticket → 状态
    private final ConcurrentHashMap<String, WechatSession> sessions = new ConcurrentHashMap<>();

    public WechatController(UserService userService) {
        this.userService = userService;
    }

    /** 获取二维码 ticket */
    @PostMapping("/qrcode")
    public Map<String, Object> getQrcode() {
        String ticket = UUID.randomUUID().toString().replace("-", "").substring(0, 16);
        sessions.put(ticket, new WechatSession("pending"));
        // 真实环境：调微信接口获取二维码 URL；模拟环境用占位图 + ticket
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=200x200&data=wechat_login:" + ticket;
        return Map.of("ticket", ticket, "qrcode", qrUrl, "expire", 300);
    }

    /** 轮询扫码状态 */
    @GetMapping("/status/{ticket}")
    public Map<String, Object> getStatus(@PathVariable String ticket) {
        WechatSession session = sessions.get(ticket);
        if (session == null) {
            return Map.of("status", "expired", "message", "二维码已过期");
        }
        if ("scanned".equals(session.status)) {
            return Map.of("status", "scanned", "user", session.userResponse);
        }
        return Map.of("status", "pending");
    }

    /** 模拟扫码（测试用） */
    @PostMapping("/simulate/{ticket}")
    public Map<String, Object> simulateScan(@PathVariable String ticket,
                                            @RequestBody(required = false) Map<String, String> body) {
        WechatSession session = sessions.get(ticket);
        if (session == null) {
            return Map.of("success", false, "message", "二维码已过期");
        }

        // 模拟微信用户信息
        String nickname = (body != null && body.containsKey("nickname")) ? body.get("nickname") : "微信用户_" + UUID.randomUUID().toString().substring(0, 6);
        String wechatId = (body != null && body.containsKey("wechatId")) ? body.get("wechatId") : "wx_" + UUID.randomUUID().toString().substring(0, 8);
        String openid = "openid_" + UUID.randomUUID().toString().replace("-", "");

        // 查找或创建用户
        User user = userService.findOrCreateWechatUser(openid, wechatId, nickname);
        if (user == null) {
            return Map.of("success", false, "message", "创建用户失败");
        }

        Map<String, Object> userResp = Map.of(
                "success", true,
                "username", user.getUsername(),
                "displayName", user.getDisplayName(),
                "role", user.getRole(),
                "token", "dummy-token"
        );

        session.status = "scanned";
        session.userResponse = userResp;
        return Map.of("success", true, "message", "扫码成功", "user", userResp);
    }

    // ---- 内部类 ----
    private static class WechatSession {
        String status; // pending | scanned | expired
        Map<String, Object> userResponse;

        WechatSession(String status) {
            this.status = status;
        }
    }
}
