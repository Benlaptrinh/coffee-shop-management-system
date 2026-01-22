package com.example.demo.payload.dto;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO for Hang Hoa Kho.
 */
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class HangHoaKhoDto {

    private Long maHangHoa;
    private String tenHangHoa;
    private Integer soLuong;
    private String donVi;
    private BigDecimal donGia;

    private LocalDateTime ngayNhapGanNhat;
    private LocalDateTime ngayXuatGanNhat;
}


