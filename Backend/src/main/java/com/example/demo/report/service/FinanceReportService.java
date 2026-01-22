package com.example.demo.report.service;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.report.dto.ReportRowDto;

/**
 * Service contract for Finance Report.
 */
public interface FinanceReportService {
    /**
     * Get finance report.
     *
     * @param from from
     * @param to to
     * @return result
     */
    List<ReportRowDto> getFinanceReport(LocalDate from, LocalDate to);
}


