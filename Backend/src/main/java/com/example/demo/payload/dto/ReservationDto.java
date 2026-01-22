package com.example.demo.payload.dto;
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
