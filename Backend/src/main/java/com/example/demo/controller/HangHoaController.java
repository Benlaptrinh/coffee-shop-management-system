package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.payload.dto.HangHoaKhoDto;
import com.example.demo.payload.form.EditHangHoaForm;
import com.example.demo.payload.form.HangHoaNhapForm;
import com.example.demo.service.HangHoaService;
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
 * HangHoaController
 *
 * Inventory endpoints.
 */
@RestController
@RequestMapping("/api/hanghoa")
public class HangHoaController {

    private final HangHoaService hangHoaService;
    /**
     * Creates a new Hang Hoa Controller.
     * @param hangHoaService hang hoa service
     */
    public HangHoaController(HangHoaService hangHoaService) {
        this.hangHoaService = hangHoaService;
    }
    /**
     * Returns inventory list.
     * @param q search keyword
     * @return response entity
     */
    @GetMapping("/kho")
    public ResponseEntity<List<HangHoaKhoDto>> danhSachKho(@RequestParam(value = "q", required = false) String q) {
        List<HangHoaKhoDto> list = (q == null || q.isBlank()) ? hangHoaService.getDanhSachKho() : hangHoaService.searchHangHoa(q);
        return ResponseEntity.ok(list);
    }
    /**
     * Records inventory receipt.
     * @param form receipt form
     * @return response entity
     */
    @PostMapping("/nhap")
    public ResponseEntity<?> nhapHang(@Valid @RequestBody HangHoaNhapForm form) {
        hangHoaService.nhapHang(form, null);
        return ResponseEntity.ok().build();
    }
    /**
     * Records inventory issue.
     * @param hangHoaId inventory item id
     * @param soLuong quantity
     * @param ngayXuat issue time (ISO-8601)
     * @return response entity
     */
    @PostMapping("/xuat")
    public ResponseEntity<?> xuatHang(@RequestParam long hangHoaId,
                                      @RequestParam BigDecimal soLuong,
                                      @RequestParam String ngayXuat) {
        LocalDateTime when = LocalDateTime.parse(ngayXuat);
        hangHoaService.xuatHang(hangHoaId, soLuong, when, null);
        return ResponseEntity.ok().build();
    }
    /**
     * Updates inventory item.
     * @param form update form
     * @return response entity
     */
    @PutMapping
    public ResponseEntity<?> updateHangHoa(@Valid @RequestBody EditHangHoaForm form) {
        hangHoaService.updateHangHoa(form);
        return ResponseEntity.ok().build();
    }
    /**
     * Deletes inventory item.
     * @param id inventory item id
     * @return response entity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteHangHoa(@PathVariable long id) {
        hangHoaService.deleteHangHoa(id);
        return ResponseEntity.noContent().build();
    }
}
