package com.example.demo.report.dto;

import lombok.Getter;

/**
 * DTO for Staff Report Row.
 */
@Getter
public class StaffReportRowDto {

    private String trangThai;
    private Long soLuong;

    /**
     * Creates StaffReportRowDto.
     *
     * @param trangThai trangThai
     * @param soLuong soLuong
     */
    public StaffReportRowDto(String trangThai, Long soLuong) {
        this.trangThai = trangThai;
        this.soLuong = soLuong == null ? 0L : soLuong;
    }

}

