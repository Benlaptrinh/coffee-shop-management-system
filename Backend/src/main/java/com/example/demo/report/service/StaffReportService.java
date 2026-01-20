package com.example.demo.report.service;

import java.util.List;

import com.example.demo.report.dto.StaffReportRowDTO;

/**
 * StaffReportService
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
public interface StaffReportService {
    /**
     * Get staff summary.
     *
     * @return result
     */
    List<StaffReportRowDTO> getStaffSummary();
}


