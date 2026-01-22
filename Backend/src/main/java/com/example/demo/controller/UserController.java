package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.TaiKhoan;
import com.example.demo.payload.dto.UserDto;
import com.example.demo.payload.request.ChangePasswordRequest;
import com.example.demo.payload.request.CreateUserRequest;
import com.example.demo.payload.request.UpdateUserRequest;
import com.example.demo.service.TaiKhoanService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * UserController
 *
 * Provides REST endpoints for user management.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final TaiKhoanService taiKhoanService;
    private final PasswordEncoder passwordEncoder;

    public UserController(TaiKhoanService taiKhoanService, PasswordEncoder passwordEncoder) {
        this.taiKhoanService = taiKhoanService;
        this.passwordEncoder = passwordEncoder;
    }

    private static UserDto toDto(TaiKhoan tk) {
        if (tk == null) return null;
        UserDto d = new UserDto();
        d.setId(tk.getMaTaiKhoan());
        d.setUsername(tk.getTenDangNhap());
        d.setRole(tk.getQuyenHan() == null ? null : tk.getQuyenHan().name());
        d.setAvatar(tk.getAnh());
        d.setEnabled(tk.isEnabled());
        return d;
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(Authentication authentication) {
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String username = authentication.getName();
        return taiKhoanService.findByUsername(username)
                .map(UserController::toDto)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @PostMapping("/me/change-password")
    public ResponseEntity<?> changePassword(Authentication authentication, @Valid @RequestBody ChangePasswordRequest req) {
        if (authentication == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        String username = authentication.getName();
        TaiKhoan tk = taiKhoanService.findByUsername(username).orElse(null);
        if (tk == null) return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        if (!passwordEncoder.matches(req.getOldPassword(), tk.getMatKhau())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Old password is incorrect");
        }
        tk.setMatKhau(req.getNewPassword());
        taiKhoanService.save(tk);
        return ResponseEntity.ok().build();
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@Valid @RequestBody CreateUserRequest req) {
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap(req.getUsername());
        tk.setMatKhau(req.getPassword());
        try {
            if (req.getRole() != null) tk.setQuyenHan(com.example.demo.enums.Role.valueOf(req.getRole()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        }
        tk.setAnh(req.getAvatar());
        tk.setEnabled(req.getEnabled() == null ? true : req.getEnabled());
        TaiKhoan saved = taiKhoanService.save(tk);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));
    }

    @GetMapping
    public ResponseEntity<List<UserDto>> listUsers() {
        List<UserDto> list = taiKhoanService.findAll().stream().map(UserController::toDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable long id) {
        return taiKhoanService.findById(id).map(UserController::toDto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserDto> updateUser(@PathVariable long id, @Valid @RequestBody UpdateUserRequest req) {
        TaiKhoan existing = taiKhoanService.findById(id).orElse(null);
        if (existing == null) return ResponseEntity.notFound().build();
        if (req.getPassword() != null && !req.getPassword().isBlank()) existing.setMatKhau(req.getPassword());
        if (req.getRole() != null) {
            try { existing.setQuyenHan(com.example.demo.enums.Role.valueOf(req.getRole())); } catch (IllegalArgumentException ex) {}
        }
        if (req.getAvatar() != null) existing.setAnh(req.getAvatar());
        if (req.getEnabled() != null) existing.setEnabled(req.getEnabled());
        TaiKhoan saved = taiKhoanService.save(existing);
        return ResponseEntity.ok(toDto(saved));
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<?> disableUser(@PathVariable long id,
                                         @RequestBody(required = false) java.util.Map<String, Object> body) {
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
