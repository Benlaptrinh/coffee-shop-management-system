package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.payload.dto.ThuChiDto;
import com.example.demo.payload.form.ChiTieuForm;
import com.example.demo.service.NganSachService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
/**
 * REST controller for Chi Tieu.
 */
@RestController
@RequestMapping("/api/chitieu")
public class ChiTieuController {

    private final NganSachService nganSachService;
    /**
     * Creates a new Chi Tieu Controller.
     * @param nganSachService ngan sach service
     */
    public ChiTieuController(NganSachService nganSachService) {
        this.nganSachService = nganSachService;
    }
    /**
     * Returns income and expense report.
     * @param fromStr start date (ISO-8601)
     * @param toStr end date (ISO-8601)
     * @return response entity
     */
    @GetMapping("/report")
    public ResponseEntity<List<ThuChiDto>> report(@RequestParam("from") String fromStr, @RequestParam("to") String toStr) {
        LocalDate from = LocalDate.parse(fromStr);
        LocalDate to = LocalDate.parse(toStr);
        return ResponseEntity.ok(nganSachService.xemThuChi(from, to));
    }
    /**
     * Creates an expense entry.
     * @param form expense form
     * @param authentication authentication
     * @return response entity
     */
    @PostMapping
    public ResponseEntity<?> themChi(@Valid @RequestBody ChiTieuForm form, Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        nganSachService.themChiTieu(form, username);
        return ResponseEntity.ok().build();
    }
}
