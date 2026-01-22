package com.example.demo.report.service;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.report.dto.SalesByDayRowDto;

/**
 * Service contract for Sales Report.
 */
public interface SalesReportService {
    /**
     * Get sales by day.
     *
     * @param from from
     * @param to to
     * @return result
     */
    List<SalesByDayRowDto> getSalesByDay(LocalDate from, LocalDate to);
}


