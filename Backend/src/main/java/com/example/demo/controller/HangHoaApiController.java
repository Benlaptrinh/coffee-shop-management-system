package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.dto.EditHangHoaForm;
import com.example.demo.dto.HangHoaKhoDTO;
import com.example.demo.dto.HangHoaNhapForm;
import com.example.demo.service.HangHoaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

/**
 * HangHoaApiController
 *
 * Inventory endpoints.
 */
@RestController
@RequestMapping("/api/hanghoa")
public class HangHoaApiController {

    private final HangHoaService hangHoaService;

    public HangHoaApiController(HangHoaService hangHoaService) {
        this.hangHoaService = hangHoaService;
    }

    @GetMapping("/kho")
    public ResponseEntity<List<HangHoaKhoDTO>> danhSachKho(@RequestParam(value = "q", required = false) String q) {
        List<HangHoaKhoDTO> list = (q == null || q.isBlank()) ? hangHoaService.getDanhSachKho() : hangHoaService.searchHangHoa(q);
        return ResponseEntity.ok(list);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/nhap")
    public ResponseEntity<?> nhapHang(@Valid @RequestBody HangHoaNhapForm form) {
        // For now we don't attach a specific nhanVien; service handles null
        hangHoaService.nhapHang(form, null);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/xuat")
    public ResponseEntity<?> xuatHang(@RequestParam Long hangHoaId,
                                      @RequestParam Integer soLuong,
                                      @RequestParam String ngayXuat) {
        LocalDateTime when = LocalDateTime.parse(ngayXuat);
        hangHoaService.xuatHang(hangHoaId, soLuong, when, null);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping
    public ResponseEntity<?> updateHangHoa(@Valid @RequestBody EditHangHoaForm form) {
        hangHoaService.updateHangHoa(form);
        return ResponseEntity.ok().build();
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHangHoa(@PathVariable Long id) {
        hangHoaService.deleteHangHoa(id);
        return ResponseEntity.noContent().build();
    }
}


