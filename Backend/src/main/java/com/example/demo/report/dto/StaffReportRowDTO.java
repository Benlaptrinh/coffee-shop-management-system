package com.example.demo.report.dto;

/**
 * StaffReportRowDTO
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
public class StaffReportRowDTO {

    private String trangThai;
    private Long soLuong;

    /**
     * Creates StaffReportRowDTO.
     *
     * @param trangThai trangThai
     * @param soLuong soLuong
     */
    public StaffReportRowDTO(String trangThai, Long soLuong) {
        this.trangThai = trangThai;
        this.soLuong = soLuong == null ? 0L : soLuong;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public Long getSoLuong() {
        return soLuong;
    }
}


