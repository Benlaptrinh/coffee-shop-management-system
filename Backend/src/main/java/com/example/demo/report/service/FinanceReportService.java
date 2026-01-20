package com.example.demo.report.service;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.report.dto.ReportRowDTO;

/**
 * FinanceReportService
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
public interface FinanceReportService {
    /**
     * Get finance report.
     *
     * @param from from
     * @param to to
     * @return result
     */
    List<ReportRowDTO> getFinanceReport(LocalDate from, LocalDate to);
}


