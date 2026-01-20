package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * ThuChiDTO
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
@Setter
@AllArgsConstructor
public class ThuChiDTO {
    private LocalDate ngay;
    private BigDecimal thu;
    private BigDecimal chi;
}


