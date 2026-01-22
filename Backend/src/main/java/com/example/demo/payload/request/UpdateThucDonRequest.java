package com.example.demo.payload.request;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
/**
 * Request payload for Update Thuc Don.
 */
@Getter
@Setter
public class UpdateThucDonRequest {
    @NotBlank
    private String tenMon;
    @NotNull
    private BigDecimal giaHienTai;
}
