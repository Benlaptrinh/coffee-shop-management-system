package com.example.demo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * EditHangHoaForm
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
public class EditHangHoaForm {
    private Long id;

    @NotBlank(message = "Tên hàng hóa bắt buộc")
    private String tenHangHoa;

    @NotNull(message = "Số lượng bắt buộc")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;

    @NotNull(message = "Đơn vị bắt buộc")
    private Long donViTinhId;

    @NotNull(message = "Đơn giá bắt buộc")
    @DecimalMin(value = "0", inclusive = true, message = "Đơn giá phải lớn hơn hoặc bằng 0")
    private BigDecimal donGia;
}

