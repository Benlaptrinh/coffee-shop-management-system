package com.example.demo.controller;

import java.util.List;

import com.example.demo.entity.ThietBi;
import com.example.demo.payload.request.ThietBiRequest;
import com.example.demo.service.ThietBiService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
/**
 * REST controller for Thiet Bi.
 */
@RestController
@RequestMapping("/api/thietbi")
public class ThietBiController {

    private final ThietBiService service;
    /**
     * Creates a new Thiet Bi Controller.
     * @param service service
     */
    public ThietBiController(ThietBiService service) {
        this.service = service;
    }
    /**
     * Lists items.
     * @return response entity
     */
    @GetMapping
    public ResponseEntity<List<ThietBi>> list() {
        return ResponseEntity.ok(service.findAll());
    }
    /**
     * Gets an item.
     * @param id id
     * @return response entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<ThietBi> get(@PathVariable long id) {
        return service.findById(id).map(ResponseEntity::ok)
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy thiết bị"));
    }
    /**
     * Creates a new entry.
     * @param thietBi thiet bi
     * @return response entity
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody ThietBiRequest req) {
        ThietBi thietBi = new ThietBi();
        applyRequest(thietBi, req);
        service.save(thietBi);
        return ResponseEntity.status(201).build();
    }
    /**
     * Updates the entry.
     * @param id id
     * @param thietBi thiet bi
     * @return response entity
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @Valid @RequestBody ThietBiRequest req) {
        ThietBi existing = service.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy thiết bị"));
        existing.setMaThietBi(id);
        applyRequest(existing, req);
        service.save(existing);
        return ResponseEntity.ok().build();
    }
    /**
     * Deletes the entry.
     * @param id id
     * @return response entity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        service.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyRequest(ThietBi target, ThietBiRequest req) {
        target.setTenThietBi(req.getTenThietBi());
        target.setSoLuong(req.getSoLuong());
        target.setDonGiaMua(req.getDonGiaMua());
        target.setNgayMua(req.getNgayMua());
        target.setGhiChu(req.getGhiChu());
    }
}
