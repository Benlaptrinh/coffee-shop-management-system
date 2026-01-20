package com.example.demo.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.FutureOrPresent;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ThietBiForm {

    private Long maThietBi;

    @NotBlank(message = "Tên thiết bị bắt buộc")
    private String tenThietBi;

    @NotNull(message = "Ngày mua bắt buộc")
    @FutureOrPresent(message = "Ngày mua không được trước hôm nay")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayMua;

    @NotNull(message = "Số lượng bắt buộc")
    @Min(value = 1, message = "Số lượng phải lớn hơn 0")
    @Max(value = 10, message = "Số lượng tối đa 10")
    private Integer soLuong;

    @NotNull(message = "Đơn giá mua bắt buộc")
    @DecimalMin(value = "0", inclusive = true, message = "Đơn giá mua phải lớn hơn hoặc bằng 0")
    private BigDecimal donGiaMua;

    private String ghiChu;
}
