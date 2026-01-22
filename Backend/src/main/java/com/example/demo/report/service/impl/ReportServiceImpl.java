package com.example.demo.report.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.report.dto.ReportRowDto;
import com.example.demo.report.dto.SalesByDayRowDto;
import com.example.demo.report.dto.StaffReportRowDto;
import com.example.demo.report.service.ReportService;
import com.example.demo.repository.HoaDonRepository;
import com.example.demo.repository.NhanVienRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * ReportServiceImpl
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
public class ReportServiceImpl implements ReportService {

    private static final Logger log = LoggerFactory.getLogger(ReportServiceImpl.class);

    private final HoaDonRepository hoaDonRepository;
    private final NhanVienRepository nhanVienRepository;

    /**
     * Creates ReportServiceImpl.
     *
     * @param hoaDonRepository hoaDonRepository
     * @param nhanVienRepository nhanVienRepository
     */
    public ReportServiceImpl(HoaDonRepository hoaDonRepository, NhanVienRepository nhanVienRepository) {
        this.hoaDonRepository = hoaDonRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    /**
     * Thong ke thu chi.
     *
     * @param from from
     * @param to to
     * @return result
     */
    @Override
    public List<ReportRowDto> thongKeThuChi(LocalDate from, LocalDate to) {
        LocalDateTime fromTime = from.atStartOfDay();
        LocalDateTime toTime = to.atTime(23, 59, 59);

        List<Object[]> rows = hoaDonRepository.thongKeThuRaw(fromTime, toTime);
        List<ReportRowDto> result = rows.stream()
                .map(r -> new ReportRowDto(
                        (java.sql.Date) r[0],
                        (BigDecimal) r[1],
                        (Number) r[2]
                ))
                .toList();

        log.info("Thu chi report from {} to {} rows={}", from, to, result.size());
        return result;
    }

    /**
     * Thong ke nhan vien.
     *
     * @return result
     */
    @Override
    public List<StaffReportRowDto> thongKeNhanVien() {
        List<Object[]> rows = nhanVienRepository.thongKeNhanVienRaw();
    
        List<StaffReportRowDto> result = new ArrayList<>();
        for (Object[] r : rows) {
            Boolean enabled = (Boolean) r[0];
            Long count = (Long) r[1];
    
            String trangThai = enabled ? "Đang làm" : "Nghỉ việc";
            result.add(new StaffReportRowDto(trangThai, count));
        }
        log.info("Nhan vien report rows={}", result.size());
        return result;
    }
    

    /**
     * Report sales by day.
     *
     * @param from from
     * @param to to
     * @return result
     */
    @Override
    public List<SalesByDayRowDto> reportSalesByDay(LocalDate from, LocalDate to) {
        LocalDateTime fromTime = from.atStartOfDay();
        LocalDateTime toTime = to.atTime(23, 59, 59);

        List<Object[]> rows = hoaDonRepository.thongKeBanHangTheoNgayRaw(fromTime, toTime);
        List<SalesByDayRowDto> result = rows.stream()
                .map(r -> new SalesByDayRowDto(
                        ((java.sql.Date) r[0]).toLocalDate(),
                        ((Number) r[1]).longValue(),
                        (BigDecimal) r[2]
                ))
                .toList();
        log.info("Sales by day report from {} to {} rows={}", from, to, result.size());
        return result;
    }
}

