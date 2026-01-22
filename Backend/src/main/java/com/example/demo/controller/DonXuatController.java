package com.example.demo.controller;

import java.util.List;

import com.example.demo.entity.DonXuat;
import com.example.demo.repository.DonXuatRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * DonXuatController
 *
 * Endpoints for export records (don xuat).
 */
@RestController
@RequestMapping("/api/donxuat")
public class DonXuatController {

    private final DonXuatRepository repo;
    /**
     * Creates a new Don Xuat Controller.
     * @param repo repo
     */
    public DonXuatController(DonXuatRepository repo) {
        this.repo = repo;
    }
    /**
     * Lists all.
     * @return response entity
     */
    @GetMapping
    public ResponseEntity<List<DonXuat>> listAll() {
        return ResponseEntity.ok(repo.findAll());
    }
    /**
     * Gets an item.
     * @param id id
     * @return response entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<DonXuat> get(@PathVariable long id) {
        return repo.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }
}
