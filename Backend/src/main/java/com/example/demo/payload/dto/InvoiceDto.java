package com.example.demo.payload.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class InvoiceDto {
    private Long maHoaDon;
    private Long maBan;
    private String tinhTrang;
    private LocalDateTime ngayGioTao;
    private LocalDateTime ngayThanhToan;
    private BigDecimal tongTien;
    private String tenNhanVien;
    private String tenKhachDat;
    private List<InvoiceItemDto> items;
}
