package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.dto.NhanVienDto;
import com.example.demo.entity.NhanVien;
import com.example.demo.service.NhanVienService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/nhanvien")
public class NhanVienController {

    private final NhanVienService nhanVienService;

    public NhanVienController(NhanVienService nhanVienService) {
        this.nhanVienService = nhanVienService;
    }

    private static NhanVienDto toDto(NhanVien nv) {
        if (nv == null) return null;
        NhanVienDto d = new NhanVienDto();
        d.setMaNhanVien(nv.getMaNhanVien());
        d.setHoTen(nv.getHoTen());
        d.setSoDienThoai(nv.getSoDienThoai());
        d.setDiaChi(nv.getDiaChi());
        d.setChucVu(nv.getChucVu() == null ? null : nv.getChucVu().getTenChucVu());
        d.setTaiKhoanId(nv.getTaiKhoan() == null ? null : nv.getTaiKhoan().getMaTaiKhoan());
        d.setEnabled(nv.getEnabled() == null ? true : nv.getEnabled());
        return d;
    }

    @GetMapping
    public ResponseEntity<List<NhanVienDto>> list(@RequestParam(value = "q", required = false) String q) {
        List<NhanVien> list = (q == null || q.isBlank()) ? nhanVienService.findAll() : nhanVienService.findByHoTenContaining(q);
        return ResponseEntity.ok(list.stream().map(NhanVienController::toDto).collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<NhanVienDto> get(@PathVariable long id) {
        return nhanVienService.findById(id).map(NhanVienController::toDto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody NhanVien nv) {
        nhanVienService.save(nv);
        return ResponseEntity.status(201).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @Valid @RequestBody NhanVien nv) {
        nv.setMaNhanVien(id);
        nhanVienService.save(nv);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        nhanVienService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
