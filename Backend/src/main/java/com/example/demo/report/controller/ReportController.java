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

    public ReportController(FinanceReportService financeReportService,
                            SalesReportService salesReportService,
                            StaffReportService staffReportService) {
        this.financeReportService = financeReportService;
        this.salesReportService = salesReportService;
        this.staffReportService = staffReportService;
    }

    @GetMapping("/finance")
    public ResponseEntity<?> finance(@RequestParam("from") String fromStr,
                                     @RequestParam("to") String toStr) {
        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(fromStr, DATE_FORMAT);
            to = LocalDate.parse(toStr, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body("Ngay khong hop le");
        }
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body("Tu ngay khong duoc sau den ngay");
        }
        List<ReportRowDto> data = financeReportService.getFinanceReport(from, to);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/sales")
    public ResponseEntity<?> sales(@RequestParam("from") String fromStr,
                                   @RequestParam("to") String toStr) {
        LocalDate from;
        LocalDate to;
        try {
            from = LocalDate.parse(fromStr, DATE_FORMAT);
            to = LocalDate.parse(toStr, DATE_FORMAT);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().body("Ngay khong hop le");
        }
        if (from.isAfter(to)) {
            return ResponseEntity.badRequest().body("Tu ngay khong duoc sau den ngay");
        }
        List<SalesByDayRowDto> data = salesReportService.getSalesByDay(from, to);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/staff")
    public ResponseEntity<List<StaffReportRowDto>> staff() {
        return ResponseEntity.ok(staffReportService.getStaffSummary());
    }
}
