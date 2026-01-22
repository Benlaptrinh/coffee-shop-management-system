package com.example.demo.report.service;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.report.dto.ReportRowDto;
import com.example.demo.report.dto.SalesByDayRowDto;
import com.example.demo.report.dto.StaffReportRowDto;

/**
 * ReportService
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
public interface ReportService {
    /**
     * Thong ke thu chi.
     *
     * @param from from
     * @param to to
     * @return result
     */
    List<ReportRowDto> thongKeThuChi(LocalDate from, LocalDate to);
    /**
     * Thong ke nhan vien.
     *
     * @return result
     */
    List<StaffReportRowDto> thongKeNhanVien();
    /**
     * Report sales by day.
     *
     * @param from from
     * @param to to
     * @return result
     */
    List<SalesByDayRowDto> reportSalesByDay(LocalDate from, LocalDate to);
}


