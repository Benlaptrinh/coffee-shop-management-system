package com.example.demo.controller;

import java.util.List;

import com.example.demo.entity.KhuyenMai;
import com.example.demo.payload.form.KhuyenMaiForm;
import com.example.demo.service.KhuyenMaiService;
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
 * REST controller for Khuyen Mai.
 */
@RestController
@RequestMapping("/api/khuyenmai")
public class KhuyenMaiController {

    private final KhuyenMaiService kmService;
    /**
     * Creates a new Khuyen Mai Controller.
     * @param kmService km service
     */
    public KhuyenMaiController(KhuyenMaiService kmService) {
        this.kmService = kmService;
    }
    /**
     * Lists items.
     * @return response entity
     */
    @GetMapping
    public ResponseEntity<List<KhuyenMai>> list() {
        return ResponseEntity.ok(kmService.getAllKhuyenMai());
    }
    /**
     * Creates a new entry.
     * @param form form
     * @return response entity
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody KhuyenMaiForm form) {
        kmService.createKhuyenMai(form);
        return ResponseEntity.status(201).build();
    }
    /**
     * Returns form.
     * @param id id
     * @return response entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<KhuyenMaiForm> getForm(@PathVariable long id) {
        KhuyenMaiForm form = kmService.getFormById(id);
        return ResponseEntity.ok(form);
    }
    /**
     * Updates the entry.
     * @param id id
     * @param form form
     * @return response entity
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @Valid @RequestBody KhuyenMaiForm form) {
        kmService.updateKhuyenMai(id, form);
        return ResponseEntity.ok().build();
    }
    /**
     * Deletes the entry.
     * @param id id
     * @return response entity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        kmService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
