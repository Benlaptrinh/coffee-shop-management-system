package com.example.demo.payload.request;
import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateThucDonRequest {
    @NotBlank
    private String tenMon;
    @NotNull
    private BigDecimal giaHienTai;
}
