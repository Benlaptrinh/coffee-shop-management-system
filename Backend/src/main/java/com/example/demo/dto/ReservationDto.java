package com.example.demo.dto;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReservationDto {
    private Long maBan;
    private String tenKhach;
    private String sdt;
    private LocalDateTime ngayGioDat;
}
