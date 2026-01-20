package com.example.demo.report.service.impl;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.dto.ThuChiDTO;
import com.example.demo.report.dto.ReportRowDTO;
import com.example.demo.report.service.FinanceReportService;
import com.example.demo.service.NganSachService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * FinanceReportServiceImpl
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
public class FinanceReportServiceImpl implements FinanceReportService {

    private static final Logger log = LoggerFactory.getLogger(FinanceReportServiceImpl.class);

    private final NganSachService nganSachService;

    /**
     * Creates FinanceReportServiceImpl.
     *
     * @param nganSachService nganSachService
     */
    public FinanceReportServiceImpl(NganSachService nganSachService) {
        this.nganSachService = nganSachService;
    }

    /**
     * Get finance report.
     *
     * @param from from
     * @param to to
     * @return result
     */
    @Override
    public List<ReportRowDTO> getFinanceReport(LocalDate from, LocalDate to) {
        List<ThuChiDTO> rows = nganSachService.xemThuChi(from, to);
        List<ReportRowDTO> result = rows.stream()
                .map(r -> new ReportRowDTO(
                        r.getNgay(),
                        r.getThu() == null ? 0L : r.getThu().longValue(),
                        r.getChi() == null ? 0L : r.getChi().longValue()
                ))
                .toList();
        log.info("Finance report from {} to {} rows={}", from, to, result.size());
        return result;
    }
}
