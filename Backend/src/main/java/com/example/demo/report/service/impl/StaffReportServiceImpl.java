package com.example.demo.report.service.impl;

import java.util.ArrayList;
import java.util.List;

import com.example.demo.report.dto.StaffReportRowDto;
import com.example.demo.report.service.StaffReportService;
import com.example.demo.repository.NhanVienRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * StaffReportServiceImpl
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
@Service
public class StaffReportServiceImpl implements StaffReportService {

    private static final Logger log = LoggerFactory.getLogger(StaffReportServiceImpl.class);

    private final NhanVienRepository nhanVienRepository;

    /**
     * Creates StaffReportServiceImpl.
     *
     * @param nhanVienRepository nhanVienRepository
     */
    public StaffReportServiceImpl(NhanVienRepository nhanVienRepository) {
        this.nhanVienRepository = nhanVienRepository;
    }

    /**
     * Get staff summary.
     *
     * @return result
     */
    @Override
    public List<StaffReportRowDto> getStaffSummary() {
        List<Object[]> rows = nhanVienRepository.thongKeNhanVienRaw();
        List<StaffReportRowDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            Boolean enabled = (Boolean) r[0];
            Number cnt = (Number) r[1];
            String trangThai = enabled != null && enabled ? "Đang làm" : "Nghỉ việc";
            result.add(new StaffReportRowDto(trangThai, cnt == null ? 0L : cnt.longValue()));
        }
        log.info("Staff summary rows={}", result.size());
        return result;
    }
}

