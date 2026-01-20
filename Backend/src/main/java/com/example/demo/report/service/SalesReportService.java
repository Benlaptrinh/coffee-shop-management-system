package com.example.demo.report.service;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.report.dto.SalesByDayRowDTO;

/**
 * SalesReportService
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
public interface SalesReportService {
    /**
     * Get sales by day.
     *
     * @param from from
     * @param to to
     * @return result
     */
    List<SalesByDayRowDTO> getSalesByDay(LocalDate from, LocalDate to);
}


