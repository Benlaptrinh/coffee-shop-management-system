package com.example.demo.controller;

import java.util.List;

import com.example.demo.entity.DonXuat;
import com.example.demo.repository.DonXuatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * DonXuatApiController
 *
 * Endpoints for export records (don xuat).
 */
@RestController
@RequestMapping("/api/donxuat")
public class DonXuatApiController {

    private final DonXuatRepository repo;

    public DonXuatApiController(DonXuatRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public ResponseEntity<List<DonXuat>> listAll() {
        return ResponseEntity.ok(repo.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<DonXuat> get(@PathVariable Long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}


