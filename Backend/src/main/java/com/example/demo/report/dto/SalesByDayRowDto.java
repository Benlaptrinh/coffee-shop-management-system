package com.example.demo.report.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.Getter;

/**
 * DTO for Sales By Day Row.
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

