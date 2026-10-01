package com.example.jwt_demo.controller;

import com.example.jwt_demo.entity.User;
import com.example.jwt_demo.repository.UserRepository;
import com.example.jwt_demo.service.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtService jwtService;

    // API Đăng ký nhanh dữ liệu mẫu để test
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> request) {
        User user = new User();
        user.setUserName(request.get("userName"));
        user.setPassword(request.get("password"));
        userRepository.save(user);
        return ResponseEntity.ok("User registered successfully");
    }

    // API Đăng nhập[cite: 1]
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> request) {
        String userName = request.get("userName"); //[cite: 1]
        String password = request.get("password"); //[cite: 1]

        Optional<User> userOptional = userRepository.findByUserName(userName);
        
        if (userOptional.isPresent() && userOptional.get().getPassword().equals(password)) {
            String token = jwtService.generateToken(userName); //[cite: 1]
            User user = userOptional.get();
            user.setToken(token); //[cite: 1]
            userRepository.save(user);

            return ResponseEntity.ok(Map.of("token", token));
        }

        return ResponseEntity.status(401).body("Tài khoản hoặc mật khẩu không chính xác!");
    }

    // API in Hello World theo bài thực hành số 1 và được bảo vệ bởi Token[cite: 1]
    @GetMapping("/auth")
    public ResponseEntity<String> getAuthHello() {
        return ResponseEntity.ok("Hello World"); //[cite: 1]
    }
}