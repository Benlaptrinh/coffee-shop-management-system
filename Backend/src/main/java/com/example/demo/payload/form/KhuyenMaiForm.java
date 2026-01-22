package com.example.demo.payload.form;
import java.time.LocalDate;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Form payload for Khuyen Mai.
 */
@Getter
@Setter
public class KhuyenMaiForm {
    private Long id;

    @NotBlank(message = "Tên khuyến mãi bắt buộc")
    private String tenKhuyenMai;

    @NotNull(message = "Ngày bắt đầu bắt buộc")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayBatDau;

    @NotNull(message = "Ngày kết thúc bắt buộc")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate ngayKetThuc;

    @NotNull(message = "Phần trăm giảm giá bắt buộc")
    @Min(value = 1, message = "Phần trăm giảm giá phải từ 1 đến 100")
    @Max(value = 100, message = "Phần trăm giảm giá phải từ 1 đến 100")
    private Integer giaTriGiam;
}

