package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HangHoaKhoDTO
 *
 * Version 1.0
 *
 * Date: 09-01-2026
 *
 * Copyright
 *
 * Modification Logs:
 * DATE        AUTHOR      DESCRIPTION
 * -----------------------------------
 * 09-01-2026  Việt    Create
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class HangHoaKhoDTO {

    private Long maHangHoa;
    private String tenHangHoa;
    private Integer soLuong;
    private String donVi;
    private BigDecimal donGia;

    private LocalDateTime ngayNhapGanNhat;
    private LocalDateTime ngayXuatGanNhat;
}


