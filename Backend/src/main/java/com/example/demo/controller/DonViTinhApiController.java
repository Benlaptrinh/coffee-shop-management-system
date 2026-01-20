package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.DonViTinh;
import com.example.demo.repository.DonViTinhRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

/**
 * DonViTinhApiController
 *
 * Simple CRUD for units.
 */
@RestController
@RequestMapping("/api/donvitinh")
public class DonViTinhApiController {

    private final DonViTinhRepository repo;

    public DonViTinhApiController(DonViTinhRepository repo) {
        this.repo = repo;
    }

    public static class DonViDto {
        public Long maDonViTinh;
        public String tenDonVi;
    }

    public static class CreateReq {
        @NotBlank
        public String tenDonVi;
    }

    @GetMapping
    public ResponseEntity<List<DonViDto>> list() {
        List<DonViDto> list = repo.findAll().stream().map(d -> {
            DonViDto dto = new DonViDto();
            dto.maDonViTinh = d.getMaDonViTinh();
            dto.tenDonVi = d.getTenDonVi();
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateReq req) {
        DonViTinh d = new DonViTinh();
        d.setTenDonVi(req.tenDonVi);
        repo.save(d);
        return ResponseEntity.status(201).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        repo.findById(id).ifPresent(repo::delete);
        return ResponseEntity.noContent().build();
    }
}


