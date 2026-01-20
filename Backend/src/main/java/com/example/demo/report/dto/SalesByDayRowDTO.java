package com.example.demo.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * SalesByDayRowDTO
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
public class SalesByDayRowDTO {

    private LocalDate ngay;
    private Long soHoaDon;
    private BigDecimal doanhThu;

    /**
     * Creates SalesByDayRowDTO.
     *
     * @param ngay ngay
     * @param soHoaDon soHoaDon
     * @param doanhThu doanhThu
     */
    public SalesByDayRowDTO(LocalDate ngay, Long soHoaDon, BigDecimal doanhThu) {
        this.ngay = ngay;
        this.soHoaDon = soHoaDon == null ? 0L : soHoaDon;
        this.doanhThu = doanhThu == null ? BigDecimal.ZERO : doanhThu;
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public Long getSoHoaDon() {
        return soHoaDon;
    }

    public BigDecimal getDoanhThu() {
        return doanhThu;
    }
}


