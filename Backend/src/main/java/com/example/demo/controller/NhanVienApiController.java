package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.NhanVien;
import com.example.demo.service.NhanVienService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/nhanvien")
public class NhanVienApiController {

    private final NhanVienService nhanVienService;

    public NhanVienApiController(NhanVienService nhanVienService) {
        this.nhanVienService = nhanVienService;
    }

    public static class NhanVienDto {
        public Long maNhanVien;
        public String hoTen;
        public String soDienThoai;
        public String diaChi;
        public String chucVu;
        public Long taiKhoanId;
        public boolean enabled;
    }

    private static NhanVienDto toDto(NhanVien nv) {
        if (nv == null) return null;
        NhanVienDto d = new NhanVienDto();
        d.maNhanVien = nv.getMaNhanVien();
        d.hoTen = nv.getHoTen();
        d.soDienThoai = nv.getSoDienThoai();
        d.diaChi = nv.getDiaChi();
        d.chucVu = nv.getChucVu() == null ? null : nv.getChucVu().getTenChucVu();
        d.taiKhoanId = nv.getTaiKhoan() == null ? null : nv.getTaiKhoan().getMaTaiKhoan();
        d.enabled = nv.getEnabled() == null ? true : nv.getEnabled();
        return d;
    }

    @GetMapping
    public ResponseEntity<List<NhanVienDto>> list(@RequestParam(value = "q", required = false) String q) {
        List<NhanVien> list = (q == null || q.isBlank()) ? nhanVienService.findAll() : nhanVienService.findByHoTenContaining(q);
        return ResponseEntity.ok(list.stream().map(NhanVienApiController::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NhanVienDto> get(@PathVariable Long id) {
        return nhanVienService.findById(id).map(NhanVienApiController::toDto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody NhanVien nv) {
        nhanVienService.save(nv);
        return ResponseEntity.status(201).build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @Valid @RequestBody NhanVien nv) {
        // ensure id
        nv.setMaNhanVien(id);
        nhanVienService.save(nv);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id) {
        nhanVienService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}


