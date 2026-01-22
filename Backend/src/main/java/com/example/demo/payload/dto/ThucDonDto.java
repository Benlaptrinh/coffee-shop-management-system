package com.example.demo.payload.dto;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ThucDonDto {
    private Long maThucDon;
    private String tenMon;
    private BigDecimal giaHienTai;
    private String loaiMon;
}
