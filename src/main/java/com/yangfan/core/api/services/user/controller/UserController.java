package com.yangfan.core.api.services.user.controller;

import com.yangfan.core.api.services.user.model.User;
import com.yangfan.core.api.services.user.model.UserResponse;
import com.yangfan.core.api.services.user.service.JwtUtil;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

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
    public ResponseEntity<UserResponse> signUp(@RequestBody User user) {
        if (users.containsKey(user.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "User already exists");
        }

        users.put(user.getUsername(), user.getPassword());
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.of("Created", null));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<UserResponse> signIn(@RequestBody User user) {
        final String storedPassword = users.get(user.getUsername());
        if (storedPassword == null || !storedPassword.equals(user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password.");
        }

        final String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok()
                .header("x-auth-token", token)
                .header("Access-Control-Expose-Headers", "x-auth-token")
                .body(UserResponse.of("Signed in", token));
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> profile(@RequestHeader("Authorization") String authorizationHeader) {
        final String token = authorizationHeader.substring(7);

        try {
            final String username = jwtUtil.extractUsername(token);
            return ResponseEntity.ok(UserResponse.of("Welcome " + username, null));
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(UserResponse.of("Not session available", null));
        }
    }
}