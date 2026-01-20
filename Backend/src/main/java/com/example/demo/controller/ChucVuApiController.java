package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.ChucVu;
import com.example.demo.repository.ChucVuRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

@RestController
@RequestMapping("/api/chucvu")
public class ChucVuApiController {

    private final ChucVuRepository repo;

    public ChucVuApiController(ChucVuRepository repo) {
        this.repo = repo;
    }

    public static class ChucVuDto {
        public Long maChucVu;
        public String tenChucVu;
        public String luong;
    }

    public static class CreateReq {
        @NotBlank
        public String tenChucVu;
    }

    @GetMapping
    public ResponseEntity<List<ChucVu>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateReq req) {
        ChucVu cv = new ChucVu();
        cv.setTenChucVu(req.tenChucVu);
        repo.save(cv);
        return ResponseEntity.status(201).build();
    }
}


