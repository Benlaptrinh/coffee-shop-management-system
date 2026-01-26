package com.example.demo.report.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

import com.example.demo.report.dto.ReportRowDto;
import com.example.demo.report.dto.SalesByDayRowDto;
import com.example.demo.report.dto.StaffReportRowDto;
import com.example.demo.report.service.FinanceReportService;
import com.example.demo.report.service.SalesReportService;
import com.example.demo.report.service.StaffReportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * ReportController
 *
 * REST endpoints for report charts in React UI.
 */
@RestController
@RequestMapping("/api/report")
public class ReportController {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    private final FinanceReportService financeReportService;
    private final SalesReportService salesReportService;
    private final StaffReportService staffReportService;
    /**
     * Creates a new Report Controller.
     * @param financeReportService finance report service
     * @param salesReportService sales report service
     * @param staffReportService staff report service
     */
    public ReportController(FinanceReportService financeReportService,
                            SalesReportService salesReportService,
                            StaffReportService staffReportService) {
        this.financeReportService = financeReportService;
        this.salesReportService = salesReportService;
        this.staffReportService = staffReportService;
    }
    /**
     * Returns finance report data.
     * @param fromStr start date (ISO-8601)
     * @param toStr end date (ISO-8601)
     * @return response entity
     */
    @GetMapping("/finance")
    public ResponseEntity<?> finance(@RequestParam("from") String fromStr,
                                     @RequestParam("to") String toStr) {
        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(fromStr, DATE_FORMAT);
            to = LocalDate.parse(toStr, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày không hợp lệ");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Từ ngày không được sau đến ngày");
        }
        List<ReportRowDto> data = financeReportService.getFinanceReport(from, to);
        return ResponseEntity.ok(data);
    }
    /**
     * Returns sales report data.
     * @param fromStr start date (ISO-8601)
     * @param toStr end date (ISO-8601)
     * @return response entity
     */
    @GetMapping("/sales")
    public ResponseEntity<?> sales(@RequestParam("from") String fromStr,
                                   @RequestParam("to") String toStr) {
        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(fromStr, DATE_FORMAT);
            to = LocalDate.parse(toStr, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            throw new IllegalArgumentException("Ngày không hợp lệ");
        }
        if (from.isAfter(to)) {
            throw new IllegalArgumentException("Từ ngày không được sau đến ngày");
        }
        List<SalesByDayRowDto> data = salesReportService.getSalesByDay(from, to);
        return ResponseEntity.ok(data);
    }
    /**
     * Returns staff summary data.
     * @return response entity
     */
    @GetMapping("/staff")
    public ResponseEntity<List<StaffReportRowDto>> staff() {
        return ResponseEntity.ok(staffReportService.getStaffSummary());
    }
}
