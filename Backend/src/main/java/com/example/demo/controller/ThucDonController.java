package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.ThucDon;
import com.example.demo.payload.dto.ThucDonDto;
import com.example.demo.payload.request.CreateThucDonRequest;
import com.example.demo.payload.request.UpdateThucDonRequest;
import com.example.demo.service.ThucDonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * ThucDonController
 *
 * CRUD endpoints for menu items (ThucDon).
 */
@RestController
@RequestMapping("/api/thucdon")
public class ThucDonController {

    private final ThucDonService thucDonService;
    /**
     * Creates a new Thuc Don Controller.
     * @param thucDonService thuc don service
     */
    public ThucDonController(ThucDonService thucDonService) {
        this.thucDonService = thucDonService;
    }

    private static ThucDonDto toDto(ThucDon t) {
        if (t == null) return null;
        ThucDonDto d = new ThucDonDto();
        d.setMaThucDon(t.getMaThucDon());
        d.setTenMon(t.getTenMon());
        d.setGiaHienTai(t.getGiaHienTai());
        d.setLoaiMon(t.getLoaiMon());
        return d;
    }
    /**
     * Lists items.
     * @param q q
     * @return response entity
     */
    @GetMapping
    public ResponseEntity<List<ThucDonDto>> list(@RequestParam(value = "q", required = false) String q) {
        List<ThucDon> list = (q == null || q.isBlank()) ? thucDonService.findAll() : thucDonService.searchByTenMon(q);
        return ResponseEntity.ok(list.stream().map(ThucDonController::toDto).collect(Collectors.toList()));
    }
    /**
     * Gets an item.
     * @param id id
     * @return response entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<ThucDonDto> get(@PathVariable long id) {
        return thucDonService.findById(id).map(ThucDonController::toDto).map(ResponseEntity::ok)
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy thực đơn"));
    }
    /**
     * Creates a new entry.
     * @param req request payload
     * @return response entity
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateThucDonRequest req) {
        thucDonService.create(req.getTenMon(), req.getGiaHienTai());
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
    /**
     * Updates the entry.
     * @param id id
     * @param req request payload
     * @return response entity
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @Valid @RequestBody UpdateThucDonRequest req) {
        thucDonService.update(id, req.getTenMon(), req.getGiaHienTai());
        return ResponseEntity.ok().build();
    }
    /**
     * Deletes the entry.
     * @param id id
     * @return response entity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        thucDonService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
