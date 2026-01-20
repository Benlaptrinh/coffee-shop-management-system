package com.example.demo.controller;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.dto.ChiTieuForm;
import com.example.demo.dto.ThuChiDTO;
import com.example.demo.service.NganSachService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/chitieu")
public class ChiTieuApiController {

    private final NganSachService nganSachService;

    public ChiTieuApiController(NganSachService nganSachService) {
        this.nganSachService = nganSachService;
    }

    @GetMapping("/report")
    public ResponseEntity<List<ThuChiDTO>> report(@RequestParam("from") String fromStr, @RequestParam("to") String toStr) {
        LocalDate from = LocalDate.parse(fromStr);
        LocalDate to = LocalDate.parse(toStr);
        return ResponseEntity.ok(nganSachService.xemThuChi(from, to));
    }

    @PostMapping
    public ResponseEntity<?> themChi(@Valid @RequestBody ChiTieuForm form, Authentication authentication) {
        String username = authentication == null ? null : authentication.getName();
        nganSachService.themChiTieu(form, username);
        return ResponseEntity.ok().build();
    }
}


