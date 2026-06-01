package com.example.photoshoot.service;

import com.example.photoshoot.dto.LoginRequest;
import com.example.photoshoot.dto.RegisterRequest;
import com.example.photoshoot.mapper.UserMapper;
import com.example.photoshoot.model.User;
import com.example.photoshoot.util.PasswordEncryptor;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.regex.Pattern;

import static com.example.photoshoot.util.PasswordEncryptor.encrypt;

@Service
public class UserService {

    private final UserMapper userMapper;
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^A-Za-z0-9]).{8,}$");

    public UserService(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public User findByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    public boolean validateUser(LoginRequest loginRequest) {
        User user = findByUsername(loginRequest.getUsername());
        return user != null && PasswordEncryptor.matches(loginRequest.getPassword(), user.getPassword());
    }

    public String registerUser(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()) {
            return "用户名不能为空";
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            return "密码不能为空";
        }
        if (findByUsername(request.getUsername()) != null) {
            return "用户名已存在";
        }
        if (!PASSWORD_PATTERN.matcher(request.getPassword()).matches()) {
            return "密码必须至少8位，并包含大小写字母和特殊字符";
        }

        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(request.getUsername());
        user.setPassword(PasswordEncryptor.encrypt(request.getPassword())); // 加密存储
        user.setDisplayName(request.getDisplayName() == null || request.getDisplayName().isBlank()
                ? request.getUsername()
                : request.getDisplayName());
        user.setRole("user");

        userMapper.insertUser(user);
        return null;
    }

    public User findOrCreateWechatUser(String openid, String wechatId, String nickname) {
        // 先按 openid 查找
        User existing = userMapper.selectByWechatOpenid(openid);
        if (existing != null) return existing;

        // 再按 wechatId（用作用户名）查找
        existing = userMapper.selectByUsername(wechatId);
        if (existing != null) {
            // 绑定 openid
            existing.setWechatOpenid(openid);
            userMapper.updateWechatOpenid(existing);
            return existing;
        }

        // 创建新用户
        User user = new User();
        user.setId(UUID.randomUUID().toString());
        user.setUsername(wechatId);
        user.setDisplayName(nickname);
        user.setPassword(encrypt(UUID.randomUUID().toString()));
        user.setRole("user");
        user.setWechatOpenid(openid);
        userMapper.insertUser(user);
        return user;
    }
}
