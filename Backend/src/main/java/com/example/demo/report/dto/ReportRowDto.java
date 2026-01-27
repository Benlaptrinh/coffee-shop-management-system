package com.example.demo.report.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

import lombok.Getter;

/**
 * DTO for Report Row.
 */
@Getter
public class ReportRowDto {

    private LocalDate ngay;
    private Long thu;
    private Long chi;

    /**
     * Creates ReportRowDto.
     *
     * @param ngay ngay
     * @param thu thu
     * @param chi chi
     */
    public ReportRowDto(LocalDate ngay, Long thu, Long chi) {
        this.ngay = ngay;
        this.thu = thu == null ? 0L : thu;
        this.chi = chi == null ? 0L : chi;
    }

    /**
     * Creates ReportRowDto.
     *
     * @param sqlDate sqlDate
     * @param thu thu
     * @param chi chi
     */
    public ReportRowDto(Date sqlDate, BigDecimal thu, Number chi) {
        this(sqlDate != null ? sqlDate.toLocalDate() : null,
             thu != null ? thu.longValue() : 0L,
             chi != null ? chi.longValue() : 0L);
    }

}

