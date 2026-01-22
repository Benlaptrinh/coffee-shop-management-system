package com.example.demo.payload.dto;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
/**
 * DTO for Menu Item.
 */
@Getter
@Setter
public class MenuItemDto {
    private Long maThucDon;
    private String tenMon;
    private BigDecimal giaHienTai;
    private String loaiMon;
}
