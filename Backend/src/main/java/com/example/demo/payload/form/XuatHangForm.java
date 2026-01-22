package com.example.demo.payload.form;
import java.time.LocalDateTime;

import org.springframework.format.annotation.DateTimeFormat;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form payload for Xuat Hang.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class XuatHangForm {
    @NotNull(message = "Hàng hóa bắt buộc")
    private Long hangHoaId;

    @NotNull(message = "Số lượng xuất bắt buộc")
    @Min(value = 1, message = "Số lượng xuất phải lớn hơn 0")
    private Integer soLuong;

    @NotNull(message = "Ngày xuất bắt buộc")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    private LocalDateTime ngayXuat;
}

