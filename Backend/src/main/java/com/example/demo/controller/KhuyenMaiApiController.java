package com.example.demo.controller;

import java.util.List;

import com.example.demo.dto.KhuyenMaiForm;
import com.example.demo.entity.KhuyenMai;
import com.example.demo.service.KhuyenMaiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/khuyenmai")
public class KhuyenMaiApiController {

    private final KhuyenMaiService kmService;

    public KhuyenMaiApiController(KhuyenMaiService kmService) {
        this.kmService = kmService;
    }

    @GetMapping
    public ResponseEntity<List<KhuyenMai>> list() {
        return ResponseEntity.ok(kmService.getAllKhuyenMai());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody KhuyenMaiForm form) {
        kmService.createKhuyenMai(form);
        return ResponseEntity.status(201).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<KhuyenMaiForm> getForm(@PathVariable Long id) {
        KhuyenMaiForm form = kmService.getFormById(id);
        return ResponseEntity.ok(form);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody KhuyenMaiForm form) {
        kmService.updateKhuyenMai(id, form);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        kmService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}

