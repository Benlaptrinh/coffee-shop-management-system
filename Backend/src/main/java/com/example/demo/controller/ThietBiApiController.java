package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.ThietBi;
import com.example.demo.service.ThietBiService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/thietbi")
public class ThietBiApiController {

    private final ThietBiService service;

    public ThietBiApiController(ThietBiService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<ThietBi>> list() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThietBi> get(@PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ThietBi thietBi) {
        service.save(thietBi);
        return ResponseEntity.status(201).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody ThietBi thietBi) {
        thietBi.setMaThietBi(id);
        service.save(thietBi);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}


