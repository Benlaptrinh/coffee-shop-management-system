package com.example.demo.payload.dto;
import java.math.BigDecimal;
import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * DTO for Thu Chi.
 */
@Getter
@Setter
@AllArgsConstructor
public class ThuChiDto {
    private LocalDate ngay;
    private BigDecimal thu;
    private BigDecimal chi;
}


