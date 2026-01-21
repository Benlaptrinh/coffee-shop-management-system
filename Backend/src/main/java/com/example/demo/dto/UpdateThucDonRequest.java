package com.example.demo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateThucDonRequest {
    @NotBlank
    private String tenMon;
    @NotNull
    private BigDecimal giaHienTai;
}
