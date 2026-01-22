package com.example.demo.payload.dto;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
/**
 * DTO for Invoice Item.
 */
@Getter
@Setter
public class InvoiceItemDto {
    private Long maThucDon;
    private String tenMon;
    private Integer soLuong;
    private BigDecimal giaTaiThoiDiemBan;
    private BigDecimal thanhTien;
}
