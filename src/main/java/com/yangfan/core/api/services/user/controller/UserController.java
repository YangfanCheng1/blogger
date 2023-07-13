package com.yangfan.core.api.services.user.controller;

import com.yangfan.core.api.services.user.model.User;
import com.yangfan.core.api.services.user.service.JwtUtil;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final Key jwtSecretKey = Keys.hmacShaKeyFor("f9ff8169f6e1bad93a0f30b79332cb08".getBytes(StandardCharsets.UTF_8));
    private final long tokenExpirationInMillis = 14 * 24 * 60 * 60 * 1000; // 14 days

    private final Map<String, String> users = new HashMap<>();
    private final JwtUtil jwtUtil;

    @PostMapping("/sign-up")
    public ResponseEntity<String> signUp(@RequestBody User user) {
        if (users.containsKey(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username already exists.");
        }

        users.put(user.getUsername(), user.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully.");
    }

    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(@RequestBody User user) {
        final String storedPassword = users.get(user.getUsername());
        if (storedPassword == null || !storedPassword.equals(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username or password.");
        }

        final String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok()
                .header("authToken", token)
                .body("User logged in successfully.");
    }

    @GetMapping("/profile")
    public ResponseEntity<String> profile(@RequestHeader("Authorization") String authorizationHeader) {
        final String token = authorizationHeader.substring(7);

        try {
            final String username = jwtUtil.extractUsername(token);
            return ResponseEntity.ok("Authorized. User: " + username);
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized access.");
        }
    }
}