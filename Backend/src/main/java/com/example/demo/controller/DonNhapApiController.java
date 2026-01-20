package com.example.demo.controller;

import java.util.List;

import com.example.demo.entity.DonNhap;
import com.example.demo.repository.DonNhapRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * DonNhapApiController
 *
 * Endpoints for purchase orders (don nhap).
 */
@RestController
@RequestMapping("/api/donnhap")
public class DonNhapApiController {

    private final DonNhapRepository repo;

    public DonNhapApiController(DonNhapRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<DonNhap>> listAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonNhap> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}


