package com.example.demo.dto;

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
