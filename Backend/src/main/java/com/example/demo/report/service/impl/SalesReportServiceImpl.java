package com.example.demo.report.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.report.dto.SalesByDayRowDto;
import com.example.demo.report.service.SalesReportService;
import com.example.demo.repository.HoaDonRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * SalesReportServiceImpl
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
public class SalesReportServiceImpl implements SalesReportService {

    private static final Logger log = LoggerFactory.getLogger(SalesReportServiceImpl.class);

    private final HoaDonRepository hoaDonRepository;

    /**
     * Creates SalesReportServiceImpl.
     *
     * @param hoaDonRepository hoaDonRepository
     */
    public SalesReportServiceImpl(HoaDonRepository hoaDonRepository) {
        this.hoaDonRepository = hoaDonRepository;
    }

    /**
     * Get sales by day.
     *
     * @param from from
     * @param to to
     * @return result
     */
    @Override
    public List<SalesByDayRowDto> getSalesByDay(LocalDate from, LocalDate to) {
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
        log.info("Sales report from {} to {} rows={}", from, to, result.size());
        return result;
    }
}

