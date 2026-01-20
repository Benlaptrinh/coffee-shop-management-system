package com.example.demo.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.ThucDon;
import com.example.demo.service.ThucDonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * ThucDonApiController
 *
 * CRUD endpoints for menu items (ThucDon).
 */
@RestController
@RequestMapping("/api/thucdon")
public class ThucDonApiController {

    private final ThucDonService thucDonService;

    public ThucDonApiController(ThucDonService thucDonService) {
        this.thucDonService = thucDonService;
    }

    public static class ThucDonDto {
        public Long maThucDon;
        public String tenMon;
        public BigDecimal giaHienTai;
        public String loaiMon;
    }

    public static class CreateReq {
        @NotBlank
        public String tenMon;
        @NotNull
        public BigDecimal giaHienTai;
    }

    public static class UpdateReq {
        @NotBlank
        public String tenMon;
        @NotNull
        public BigDecimal giaHienTai;
    }

    private static ThucDonDto toDto(ThucDon t) {
        if (t == null) return null;
        ThucDonDto d = new ThucDonDto();
        d.maThucDon = t.getMaThucDon();
        d.tenMon = t.getTenMon();
        d.giaHienTai = t.getGiaHienTai();
        d.loaiMon = t.getLoaiMon();
        return d;
    }

    @GetMapping
    public ResponseEntity<List<ThucDonDto>> list(@RequestParam(value = "q", required = false) String q) {
        List<ThucDon> list = (q == null || q.isBlank()) ? thucDonService.findAll() : thucDonService.searchByTenMon(q);
        return ResponseEntity.ok(list.stream().map(ThucDonApiController::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ThucDonDto> get(@PathVariable Long id) {
        return thucDonService.findById(id).map(ThucDonApiController::toDto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody CreateReq req) {
        thucDonService.create(req.tenMon, req.giaHienTai);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody UpdateReq req) {
        thucDonService.update(id, req.tenMon, req.giaHienTai);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        thucDonService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}


