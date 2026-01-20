package com.example.demo.report.controller;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.report.dto.ReportFilterDTO;
import com.example.demo.report.dto.ReportRowDTO;
import com.example.demo.report.dto.SalesByDayRowDTO;
import com.example.demo.report.dto.StaffReportRowDTO;
import com.example.demo.report.service.FinanceReportService;
import com.example.demo.report.service.SalesReportService;
import com.example.demo.report.service.StaffReportService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * ReportController
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
@Controller
@RequestMapping("/admin/report")
public class ReportController {

    private static final DateTimeFormatter DATE_LABEL_FORMAT = DateTimeFormatter.ofPattern("dd/MM");
    private String sidebar = "fragments/sidebar-admin";
    private final FinanceReportService financeReportService;
    private final SalesReportService salesReportService;
    private final StaffReportService staffReportService;

    /**
     * Creates ReportController.
     *
     * @param financeReportService financeReportService
     * @param salesReportService salesReportService
     * @param staffReportService staffReportService
     */
    public ReportController(FinanceReportService financeReportService,
                            SalesReportService salesReportService,
                            StaffReportService staffReportService) {
        this.financeReportService = financeReportService;
        this.salesReportService = salesReportService;
        this.staffReportService = staffReportService;
    }

    private String usernameFromAuth(Authentication auth) {
        return auth == null ? null : auth.getName();
    }

    private String formatDateLabel(LocalDate date) {
        return date == null ? "" : DATE_LABEL_FORMAT.format(date);
    }

    private void addChartData(Model model,
                              List<ReportRowDTO> reportData,
                              List<SalesByDayRowDTO> salesByDay,
                              List<StaffReportRowDTO> staffReport) {
        model.addAttribute("reportLabels", reportData.stream()
                .map(row -> formatDateLabel(row.getNgay()))
                .collect(Collectors.toList()));
        model.addAttribute("reportThuData", reportData.stream()
                .map(ReportRowDTO::getThu)
                .collect(Collectors.toList()));
        model.addAttribute("reportChiData", reportData.stream()
                .map(ReportRowDTO::getChi)
                .collect(Collectors.toList()));

        model.addAttribute("salesLabels", salesByDay.stream()
                .map(row -> formatDateLabel(row.getNgay()))
                .collect(Collectors.toList()));
        model.addAttribute("salesDoanhThuData", salesByDay.stream()
                .map(row -> row.getDoanhThu() == null ? 0L : row.getDoanhThu().longValue())
                .collect(Collectors.toList()));
        model.addAttribute("salesHoaDonData", salesByDay.stream()
                .map(SalesByDayRowDTO::getSoHoaDon)
                .collect(Collectors.toList()));

        model.addAttribute("staffLabels", staffReport.stream()
                .map(StaffReportRowDTO::getTrangThai)
                .collect(Collectors.toList()));
        model.addAttribute("staffCounts", staffReport.stream()
                .map(StaffReportRowDTO::getSoLuong)
                .collect(Collectors.toList()));
    }

    /**
     * Show report page.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping
    public String showReportPage(Model model, Authentication auth) {
        LocalDate now = LocalDate.now();

        ReportFilterDTO filter = new ReportFilterDTO();
        filter.setFromDate(now.withDayOfMonth(1));
        filter.setToDate(now);
        filter.setType("FINANCE");
        model.addAttribute("filter", filter);
        List<ReportRowDTO> reportData = financeReportService.getFinanceReport(filter.getFromDate(), filter.getToDate());
        List<SalesByDayRowDTO> salesByDay = salesReportService.getSalesByDay(filter.getFromDate(), filter.getToDate());
        List<StaffReportRowDTO> staffReport = staffReportService.getStaffSummary();
        model.addAttribute("reportData", reportData);
        model.addAttribute("salesByDay", salesByDay);
        model.addAttribute("staffReport", staffReport);
        addChartData(model, reportData, salesByDay, staffReport);
        model.addAttribute("username", usernameFromAuth(auth));
        model.addAttribute("sidebarFragment", sidebar);
        model.addAttribute("contentFragment", "admin/report/index");

        return "layout/base";
    }

    /**
     * View report.
     *
     * @param filter filter
     * @param model model
     * @param auth auth
     * @return result
     */
    @PostMapping
    public String viewReport(
            @ModelAttribute("filter") ReportFilterDTO filter,
            Model model,
            Authentication auth
    ) {
        String type = filter.getType();
        String error = null;
        List<ReportRowDTO> reportData = List.of();
        List<SalesByDayRowDTO> salesByDay = List.of();
        List<StaffReportRowDTO> staffReport = List.of();

        if ("STAFF".equals(type)) {
            if (filter.getFromDate() == null || filter.getToDate() == null) {
                LocalDate now = LocalDate.now();
                filter.setFromDate(now.withDayOfMonth(1));
                filter.setToDate(now);
            }
            staffReport = staffReportService.getStaffSummary();
        } else {
            if (filter.getFromDate() == null || filter.getToDate() == null) {
                error = "Vui lòng chọn đầy đủ ngày";
            } else if (filter.getFromDate().isAfter(filter.getToDate())) {
                error = "Từ ngày không được sau đến ngày";
            }

            if (error == null) {
                if ("SALES".equals(type)) {
                    salesByDay = salesReportService.getSalesByDay(filter.getFromDate(), filter.getToDate());
                } else {
                    reportData = financeReportService.getFinanceReport(filter.getFromDate(), filter.getToDate());
                }
            }
        }

        if (error != null) {
            model.addAttribute("error", error);
        }
        model.addAttribute("reportData", reportData);
        model.addAttribute("salesByDay", salesByDay);
        model.addAttribute("staffReport", staffReport);
        addChartData(model, reportData, salesByDay, staffReport);

        model.addAttribute("filter", filter);
        model.addAttribute("username", usernameFromAuth(auth));
        model.addAttribute("sidebarFragment", sidebar);
        model.addAttribute("contentFragment", "admin/report/index");

        return "layout/base";
    }
}
