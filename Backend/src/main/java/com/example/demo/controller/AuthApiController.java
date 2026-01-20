package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.constraints.NotBlank;

/**
 * AuthApiController
 */
@RestController
@RequestMapping("/api/auth")
public class AuthApiController {

    private static final Logger log = LoggerFactory.getLogger(AuthApiController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    public AuthApiController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }

    public static class LoginRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
    }

    public static class LoginResponse {
        public String token;
        public String tokenType = "Bearer";
        public long expiresIn;
        public String username;
        public List<String> roles;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.username, req.password)
        );
        List<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .collect(Collectors.toList());
        String token = jwtUtil.generateToken(req.username, roles);
        LoginResponse resp = new LoginResponse();
        resp.token = token;
        resp.expiresIn = Long.parseLong(System.getProperty("jwt.expiration.seconds", "3600"));
        resp.username = req.username;
        resp.roles = roles;
        log.info("User logged in: {}", req.username);
        return ResponseEntity.ok(resp);
    }
}


