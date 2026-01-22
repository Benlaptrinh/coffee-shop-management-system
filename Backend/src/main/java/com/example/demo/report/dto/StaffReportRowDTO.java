package com.example.demo.report.dto;

import lombok.Getter;

/**
 * StaffReportRowDto
 *
 * Version 1.0
 *
 * Date: 09-01-2026
 *
 * Copyright
 *
 * Modification Logs:
 * DATE        AUTHOR      DESCRIPTION
 * -----------------------------------
 * 09-01-2026  Việt    Create
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

