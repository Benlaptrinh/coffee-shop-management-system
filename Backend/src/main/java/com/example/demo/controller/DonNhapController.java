package com.example.demo.controller;

import java.util.List;

import com.example.demo.entity.DonNhap;
import com.example.demo.repository.DonNhapRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DonNhapController
 *
 * Endpoints for purchase orders (don nhap).
 */
@RestController
@RequestMapping("/api/donnhap")
public class DonNhapController {

    private final DonNhapRepository repo;
    /**
     * Creates a new Don Nhap Controller.
     * @param repo repo
     */
    public DonNhapController(DonNhapRepository repo) {
        this.repo = repo;
    }
    /**
     * Lists all.
     * @return response entity
     */
    @GetMapping
    public ResponseEntity<List<DonNhap>> listAll() {
        return ResponseEntity.ok(repo.findAll());
    }
    /**
     * Gets an item.
     * @param id id
     * @return response entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<DonNhap> get(@PathVariable long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
