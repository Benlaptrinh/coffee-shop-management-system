package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceItemDto {
    private Long maThucDon;
    private String tenMon;
    private Integer soLuong;
    private BigDecimal giaTaiThoiDiemBan;
    private BigDecimal thanhTien;
}
