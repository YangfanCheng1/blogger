package com.yangfan.core.api.services.user.controller;

import com.yangfan.core.api.services.user.model.User;
import com.yangfan.core.api.services.user.model.UserResponse;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.SignatureException;
import io.jsonwebtoken.security.Keys;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
public class UserController {
    private final Map<String, String> users = new HashMap<>(); // In-memory storage for simplicity
    private final Key jwtSecretKey = Keys.hmacShaKeyFor("f9ff8169f6e1bad93a0f30b79332cb08".getBytes(StandardCharsets.UTF_8));
    private final long tokenExpirationInMillis = 14 * 24 * 60 * 60 * 1000; // 14 days

    @PostMapping("/sign-up")
    public ResponseEntity<UserResponse> signUp(@RequestBody User user) {
        if (users.containsKey(user.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(UserResponse.of("User already exists"));
        }

        users.put(user.getUsername(), user.getPassword());
        return ResponseEntity.ok(UserResponse.of("created"));
    }

    @PostMapping("/sign-in")
    public ResponseEntity<String> signIn(@RequestBody User user, HttpServletResponse response) {
        if (!users.containsKey(user.getUsername()) || !users.get(user.getUsername()).equals(user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid username or password");
        }

        // Generate the authentication/session token
        String authToken = generateAuthToken(user.getUsername());

        // Set the token as an HTTP-only cookie
        Cookie cookie = new Cookie("authToken", authToken);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        // cookie.setSecure(true); // Set to 'true' when using HTTPS
        cookie.setMaxAge((int) (tokenExpirationInMillis / 1000)); // Set cookie expiration in seconds
        response.addCookie(cookie);

        return ResponseEntity.ok("User signed in successfully");
    }

    @GetMapping("/profile")
    public ResponseEntity<String> getProfile(HttpServletRequest request) {
        // Extract the JWT token from the request's cookies
        Cookie[] cookies = request.getCookies();
        String authToken = null;
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("authToken")) {
                    authToken = cookie.getValue();
                    break;
                }
            }
        }

        if (authToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("No authentication token provided");
        }

        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(jwtSecretKey)
                    .build()
                    .parseClaimsJws(authToken)
                    .getBody();

            String username = claims.getSubject();
            // Use the extracted username to fetch the user profile from the database or perform other operations
            return ResponseEntity.ok("User profile for " + username);
        } catch (SignatureException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Invalid authentication token");
        }
    }

    private String generateAuthToken(String username) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + tokenExpirationInMillis);

        return Jwts.builder()
                .setSubject(username)
                .setIssuedAt(now)
                .setExpiration(expiration)
                .signWith(jwtSecretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private String extractAuthToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7); // Remove the "Bearer " prefix
        }
        return null;
    }
}