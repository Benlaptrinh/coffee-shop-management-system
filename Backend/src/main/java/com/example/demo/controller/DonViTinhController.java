package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.DonViTinh;
import com.example.demo.payload.dto.DonViDto;
import com.example.demo.payload.request.CreateDonViRequest;
import com.example.demo.repository.DonViTinhRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * DonViTinhController
 *
 * Simple CRUD for units.
 */
@RestController
@RequestMapping("/api/donvitinh")
public class DonViTinhController {

    private final DonViTinhRepository repo;
    /**
     * Creates a new Don Vi Tinh Controller.
     * @param repo repo
     */
    public DonViTinhController(DonViTinhRepository repo) {
        this.repo = repo;
    }
    /**
     * Lists items.
     * @return response entity
     */
    @GetMapping
    public ResponseEntity<List<DonViDto>> list() {
        List<DonViDto> list = repo.findAll().stream().map(d -> {
            DonViDto dto = new DonViDto();
            dto.setMaDonViTinh(d.getMaDonViTinh());
            dto.setTenDonVi(d.getTenDonVi());
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
    /**
     * Creates a new entry.
     * @param req request payload
     * @return response entity
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateDonViRequest req) {
        DonViTinh d = new DonViTinh();
        d.setTenDonVi(req.getTenDonVi());
        repo.save(d);
        return ResponseEntity.status(201).build();
    }
    /**
     * Deletes the entry.
     * @param id id
     * @return response entity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        repo.findById(id).ifPresent(repo::delete);
        return ResponseEntity.noContent().build();
    }
}
