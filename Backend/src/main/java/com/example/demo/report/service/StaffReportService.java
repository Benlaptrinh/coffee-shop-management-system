package com.example.demo.report.service;

import java.util.List;

import com.example.demo.report.dto.StaffReportRowDto;

/**
 * Service contract for Staff Report.
 */
public interface StaffReportService {
    /**
     * Get staff summary.
     *
     * @return result
     */
    List<StaffReportRowDto> getStaffSummary();
}


