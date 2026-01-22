package com.example.demo.payload.form;
import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Form payload for Thuc Don.
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
