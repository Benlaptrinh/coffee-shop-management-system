package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.payload.request.LoginRequest;
import com.example.demo.payload.response.LoginResponse;
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

import jakarta.validation.Valid;

/**
 * REST controller for authentication.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final Logger log = LoggerFactory.getLogger(AuthController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    /**
     * Creates a new Auth Controller.
     * @param authenticationManager authentication manager
     * @param jwtUtil JWT utility
     */
    public AuthController(AuthenticationManager authenticationManager, JwtUtil jwtUtil) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
    }
    /**
     * Authenticates the user.
     * @param req request payload
     * @return response entity
     */
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest req) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getUsername(), req.getPassword())
        );
        List<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority)
                .map(a -> a.replace("ROLE_", ""))
                .collect(Collectors.toList());
        String token = jwtUtil.generateToken(req.getUsername(), roles);
        LoginResponse resp = new LoginResponse();
        resp.setToken(token);
        resp.setExpiresIn(Long.parseLong(System.getProperty("jwt.expiration.seconds", "3600")));
        resp.setUsername(req.getUsername());
        resp.setRoles(roles);
        log.info("User logged in: {}", req.getUsername());
        return ResponseEntity.ok(resp);
    }
}
