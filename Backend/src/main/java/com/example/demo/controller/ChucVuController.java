package com.example.demo.controller;

import java.util.List;

import com.example.demo.dto.CreateChucVuRequest;
import com.example.demo.entity.ChucVu;
import com.example.demo.repository.ChucVuRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chucvu")
public class ChucVuController {

    private final ChucVuRepository repo;

    public ChucVuController(ChucVuRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<ChucVu>> list() {
        return ResponseEntity.ok(repo.findAll());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateChucVuRequest req) {
        ChucVu cv = new ChucVu();
        cv.setTenChucVu(req.getTenChucVu());
        repo.save(cv);
        return ResponseEntity.status(201).build();
    }
}
