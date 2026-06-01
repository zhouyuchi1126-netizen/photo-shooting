package com.example.photoshoot.controller;

import com.example.photoshoot.dto.LoginRequest;
import com.example.photoshoot.dto.LoginResponse;
import com.example.photoshoot.dto.RegisterRequest;
import com.example.photoshoot.model.User;
import com.example.photoshoot.service.UserService;
import com.example.photoshoot.util.PasswordEncryptor;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:5173")
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody LoginRequest request) {
        LoginResponse response = new LoginResponse();
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            response.setSuccess(false);
            response.setMessage("用户不存在");
            return response;
        }

        if (!PasswordEncryptor.matches(request.getPassword(), user.getPassword())) {
            response.setSuccess(false);
            response.setMessage("用户名或密码错误");
            return response;
        }

        response.setSuccess(true);
        response.setDisplayName(user.getDisplayName());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        response.setToken("dummy-token");
        response.setMessage("登录成功");
        return response;
    }

    @PostMapping("/register")
    public LoginResponse register(@RequestBody RegisterRequest request) {
        LoginResponse response = new LoginResponse();
        String error = userService.registerUser(request);
        if (error != null) {
            response.setSuccess(false);
            response.setMessage(error);
            return response;
        }

        response.setSuccess(true);
        response.setUsername(request.getUsername());
        response.setDisplayName(request.getDisplayName() == null || request.getDisplayName().isBlank()
                ? request.getUsername()
                : request.getDisplayName());
        response.setRole("user");
        response.setToken("dummy-token");
        response.setMessage("注册成功");
        return response;
    }
}
