package com.example.demo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ThucDonForm
 *
 * Version 1.0
 *
 * Date: 16-01-2026
 *
 * Copyright
 *
 * Modification Logs:
 * DATE        AUTHOR      DESCRIPTION
 * -----------------------------------
 * 16-01-2026  Việt    Create
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThucDonForm {
    private Long id;

    @NotBlank(message = "Tên món bắt buộc")
    private String tenMon;

    @NotNull(message = "Giá tiền bắt buộc")
    @DecimalMin(value = "0", inclusive = true, message = "Giá tiền phải lớn hơn hoặc bằng 0")
    private BigDecimal giaTien;
}
