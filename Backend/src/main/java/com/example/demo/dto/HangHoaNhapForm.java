package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * HangHoaNhapForm
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
public class HangHoaNhapForm {
    @NotBlank(message = "Tên hàng hóa bắt buộc")
    private String tenHangHoa;

    @NotNull(message = "Số lượng bắt buộc")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;

    private Long donViTinhId;

    @Size(max = 30, message = "Đơn vị mới tối đa 30 ký tự")
    private String donViMoi;

    @NotNull(message = "Đơn giá bắt buộc")
    @DecimalMin(value = "0", inclusive = true, message = "Đơn giá phải lớn hơn hoặc bằng 0")
    private BigDecimal donGia;

    @NotNull(message = "Ngày nhập bắt buộc")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayNhap;
}
