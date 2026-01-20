package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * ChiTieuForm
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
public class ChiTieuForm {
    @NotNull(message = "Ngày chi bắt buộc")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayChi;

    @NotBlank(message = "Khoản chi bắt buộc")
    private String tenKhoanChi;

    @NotNull(message = "Số tiền bắt buộc")
    @DecimalMin(value = "0", inclusive = false, message = "Số tiền phải lớn hơn 0")
    private BigDecimal soTien;
}

