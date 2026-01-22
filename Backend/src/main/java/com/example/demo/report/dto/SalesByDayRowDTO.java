package com.example.demo.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;

/**
 * SalesByDayRowDto
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
@Getter
public class SalesByDayRowDto {

    private LocalDate ngay;
    private Long soHoaDon;
    private BigDecimal doanhThu;

    /**
     * Creates SalesByDayRowDto.
     *
     * @param ngay ngay
     * @param soHoaDon soHoaDon
     * @param doanhThu doanhThu
     */
    public SalesByDayRowDto(LocalDate ngay, Long soHoaDon, BigDecimal doanhThu) {
        this.ngay = ngay;
        this.soHoaDon = soHoaDon == null ? 0L : soHoaDon;
        this.doanhThu = doanhThu == null ? BigDecimal.ZERO : doanhThu;
    }

}

