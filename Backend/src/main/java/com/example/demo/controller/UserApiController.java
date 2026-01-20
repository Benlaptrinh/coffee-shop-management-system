package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.TaiKhoan;
import com.example.demo.service.TaiKhoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * UserApiController
 *
 * Provides REST endpoints for user management.
 */
@RestController
@RequestMapping("/api/users")
public class UserApiController {

    private static final Logger log = LoggerFactory.getLogger(UserApiController.class);

    private final TaiKhoanService taiKhoanService;
    private final PasswordEncoder passwordEncoder;

    public UserApiController(TaiKhoanService taiKhoanService, PasswordEncoder passwordEncoder) {
        this.taiKhoanService = taiKhoanService;
        this.passwordEncoder = passwordEncoder;
    }

    public static class UserDto {
        public Long id;
        public String username;
        public String role;
        public String avatar;
        public boolean enabled;
    }

    public static class CreateUserRequest {
        @NotBlank
        public String username;
        @NotBlank
        public String password;
        public String role;
        public String avatar;
        public Boolean enabled = true;
    }

    public static class UpdateUserRequest {
        public String password;
        public String role;
        public String avatar;
        public Boolean enabled;
    }

    public static class ChangePasswordRequest {
        @NotBlank
        public String oldPassword;
        @NotBlank
        public String newPassword;
    }

    private static UserDto toDto(TaiKhoan tk) {
        if (tk == null) return null;
        UserDto d = new UserDto();
        d.id = tk.getMaTaiKhoan();
        d.username = tk.getTenDangNhap();
        d.role = tk.getQuyenHan() == null ? null : tk.getQuyenHan().name();
        d.avatar = tk.getAnh();
        d.enabled = tk.isEnabled();
        return d;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String username = authentication.getName();
        return taiKhoanService.findByUsername(username)
                .map(UserApiController::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest req) {
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String username = authentication.getName();
        TaiKhoan tk = taiKhoanService.findByUsername(username).orElse(null);
        if (tk == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        if (!passwordEncoder.matches(req.oldPassword, tk.getMatKhau())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect");
        }
        tk.setMatKhau(req.newPassword);
        taiKhoanService.save(tk);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest req) {
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(req.username);
        tk.setMatKhau(req.password);
        try {
            if (req.role != null) tk.setQuyenHan(com.example.demo.enums.Role.valueOf(req.role));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
        tk.setAnh(req.avatar);
        tk.setEnabled(req.enabled == null ? true : req.enabled);
        TaiKhoan saved = taiKhoanService.save(tk);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> list = taiKhoanService.findAll().stream().map(UserApiController::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {
        return taiKhoanService.findById(id).map(UserApiController::toDto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest req) {
        TaiKhoan existing = taiKhoanService.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        if (req.password != null && !req.password.isBlank()) existing.setMatKhau(req.password);
        if (req.role != null) {
            try { existing.setQuyenHan(com.example.demo.enums.Role.valueOf(req.role)); } catch (IllegalArgumentException ex) {}
        }
        if (req.avatar != null) existing.setAnh(req.avatar);
        if (req.enabled != null) existing.setEnabled(req.enabled);
        TaiKhoan saved = taiKhoanService.save(existing);
        return ResponseEntity.ok(toDto(saved));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, Object> body) {
        // If body contains {"enabled": true/false} then set accordingly; otherwise set enabled=false
        if (body != null && body.containsKey("enabled")) {
            Object val = body.get("enabled");
            boolean enabled = Boolean.parseBoolean(String.valueOf(val));
            TaiKhoan tk = taiKhoanService.findById(id).orElse(null);
            if (tk == null) return ResponseEntity.notFound().build();
            tk.setEnabled(enabled);
            taiKhoanService.save(tk);
            return ResponseEntity.ok().build();
        } else {
            taiKhoanService.disable(id);
            return ResponseEntity.ok().build();
        }
    }
}


