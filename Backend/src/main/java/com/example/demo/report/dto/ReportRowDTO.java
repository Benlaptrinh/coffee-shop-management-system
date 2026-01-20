package com.example.demo.report.dto;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

/**
 * ReportRowDTO
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
public class ReportRowDTO {

    private LocalDate ngay;
    private Long thu;
    private Long chi;

    /**
     * Creates ReportRowDTO.
     *
     * @param ngay ngay
     * @param thu thu
     * @param chi chi
     */
    public ReportRowDTO(LocalDate ngay, Long thu, Long chi) {
        this.ngay = ngay;
        this.thu = thu == null ? 0L : thu;
        this.chi = chi == null ? 0L : chi;
    }

    /**
     * Creates ReportRowDTO.
     *
     * @param sqlDate sqlDate
     * @param thu thu
     * @param chi chi
     */
    public ReportRowDTO(Date sqlDate, BigDecimal thu, Number chi) {
        this(sqlDate != null ? sqlDate.toLocalDate() : null,
             thu != null ? thu.longValue() : 0L,
             chi != null ? chi.longValue() : 0L);
    }

    public LocalDate getNgay() {
        return ngay;
    }

    public Long getThu() {
        return thu;
    }

    public Long getChi() {
        return chi;
    }
}


