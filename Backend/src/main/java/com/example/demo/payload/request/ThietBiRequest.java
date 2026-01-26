package com.example.demo.payload.request;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for Thiet Bi create/update.
 */
@Getter
@Setter
public class ThietBiRequest {
    @NotBlank(message = "Tên thiết bị bắt buộc")
    private String tenThietBi;

    @NotNull(message = "Số lượng bắt buộc")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;

    @NotNull(message = "Đơn giá mua bắt buộc")
    @DecimalMin(value = "0", inclusive = true, message = "Đơn giá mua phải lớn hơn hoặc bằng 0")
    private BigDecimal donGiaMua;

    @NotNull(message = "Ngày mua bắt buộc")
    private LocalDate ngayMua;

    private String ghiChu;
}
