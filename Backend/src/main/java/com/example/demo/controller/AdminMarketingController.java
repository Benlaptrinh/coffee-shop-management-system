package com.example.demo.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import com.example.demo.dto.KhuyenMaiForm;
import com.example.demo.entity.KhuyenMai;
import com.example.demo.service.KhuyenMaiService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * AdminMarketingController
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
@RequestMapping("/admin/marketing")
public class AdminMarketingController {

    private final KhuyenMaiService khuyenMaiService;

    /**
     * Creates AdminMarketingController.
     *
     * @param khuyenMaiService khuyenMaiService
     */
    public AdminMarketingController(KhuyenMaiService khuyenMaiService) {
        this.khuyenMaiService = khuyenMaiService;
    }

    /**
     * Show list + form.
     *
     * @param model model
     * @return result
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(required = false) String keyword,
                       Model model) {
        return renderMarketingPage(model, page, keyword, new KhuyenMaiForm(), false);
    }

    /**
     * Show add form.
     *
     * @param model model
     * @return result
     */
    @GetMapping("/add")
    public String showAddForm() {
        return "redirect:/admin/marketing";
    }

    /**
     * Add khuyen mai.
     *
     * @param khuyenMaiForm khuyenMaiForm
     * @param redirectAttributes redirectAttributes
     * @param model model
     * @return result
     */
    @PostMapping("/add")
    public String addKhuyenMai(@Valid KhuyenMaiForm khuyenMaiForm,
                               BindingResult bindingResult,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(required = false) String keyword,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        validateDates(khuyenMaiForm.getNgayBatDau(), khuyenMaiForm.getNgayKetThuc(), bindingResult);
        if (bindingResult.hasErrors()) {
            return renderMarketingPage(model, page, keyword, khuyenMaiForm, false);
        }
        try {
            khuyenMaiService.createKhuyenMai(khuyenMaiForm);
            redirectAttributes.addFlashAttribute("success", "Thêm khuyến mãi thành công");
            return "redirect:/admin/marketing";
        } catch (IllegalArgumentException ex) {
            applyMarketingError(bindingResult, ex.getMessage());
            return renderMarketingPage(model, page, keyword, khuyenMaiForm, false);
        }
    }

    /**
     * Show edit form.
     *
     * @param id id
     * @param model model
     * @param ra ra
     * @return result
     */
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id,
                               @RequestParam(defaultValue = "1") int page,
                               @RequestParam(required = false) String keyword,
                               Model model,
                               RedirectAttributes ra) {
        try {
            KhuyenMaiForm form = khuyenMaiService.getFormById(id);
            form.setId(id);
            return renderMarketingPage(model, page, keyword, form, true);
        } catch (IllegalArgumentException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/marketing";
        }
    }

    /**
     * Update khuyen mai.
     *
     * @param id id
     * @param khuyenMaiForm khuyenMaiForm
     * @param redirectAttributes redirectAttributes
     * @param model model
     * @return result
     */
    @PostMapping("/edit")
    public String updateKhuyenMai(@Valid KhuyenMaiForm khuyenMaiForm,
                                  BindingResult bindingResult,
                                  @RequestParam(defaultValue = "1") int page,
                                  @RequestParam(required = false) String keyword,
                                  RedirectAttributes redirectAttributes,
                                  Model model) {
        if (khuyenMaiForm.getId() == null) {
            bindingResult.reject("invalid", "Thiếu mã khuyến mãi");
        }
        validateDates(khuyenMaiForm.getNgayBatDau(), khuyenMaiForm.getNgayKetThuc(), bindingResult);
        if (bindingResult.hasErrors()) {
            return renderMarketingPage(model, page, keyword, khuyenMaiForm, true);
        }
        try {
            khuyenMaiService.updateKhuyenMai(khuyenMaiForm.getId(), khuyenMaiForm);
            redirectAttributes.addFlashAttribute("success", "Cập nhật khuyến mãi thành công");
            return "redirect:/admin/marketing";
        } catch (IllegalArgumentException ex) {
            applyMarketingError(bindingResult, ex.getMessage());
            return renderMarketingPage(model, page, keyword, khuyenMaiForm, true);
        }
    }

    /**
     * Delete khuyen mai.
     *
     * @param id id
     * @param redirectAttributes redirectAttributes
     * @return result
     */
    @GetMapping("/delete/{id}")
    public String deleteKhuyenMai(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            khuyenMaiService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Xóa khuyến mãi thành công");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/marketing";
    }

    private String renderMarketingPage(Model model,
                                       int page,
                                       String keyword,
                                       KhuyenMaiForm form,
                                       boolean editMode) {
        int pageSize = 5;
        String trimmedKeyword = keyword == null ? null : keyword.trim();
        List<KhuyenMai> allItems = khuyenMaiService.getAllKhuyenMai();
        if (trimmedKeyword != null && !trimmedKeyword.isEmpty()) {
            String lower = trimmedKeyword.toLowerCase();
            allItems.removeIf(km -> {
                String name = km.getTenKhuyenMai();
                return name == null || !name.toLowerCase().contains(lower);
            });
        }
        allItems.sort(java.util.Comparator.comparing(
                km -> km.getTenKhuyenMai() == null ? "" : km.getTenKhuyenMai(),
                String.CASE_INSENSITIVE_ORDER
        ));
        int totalItems = allItems.size();
        int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<KhuyenMai> pageItems = totalItems == 0 ? Collections.emptyList() : allItems.subList(fromIndex, toIndex);

        model.addAttribute("khuyenMais", pageItems);
        model.addAttribute("khuyenMaiForm", form);
        model.addAttribute("editMode", editMode);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("keyword", trimmedKeyword);
        model.addAttribute("sidebarFragment", "fragments/sidebar-admin");
        model.addAttribute("contentFragment", "admin/marketing/list");
        model.addAttribute("activeMenu", "marketing");
        return "layout/base";
    }

    private void validateDates(LocalDate start, LocalDate end, BindingResult bindingResult) {
        if (start != null && end != null && start.isAfter(end)) {
            bindingResult.rejectValue("ngayKetThuc", "invalid", "Ngày kết thúc phải sau hoặc bằng ngày bắt đầu");
        }
    }

    private void applyMarketingError(BindingResult bindingResult, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        String lower = message.toLowerCase();
        if (lower.contains("tên")) {
            bindingResult.rejectValue("tenKhuyenMai", "invalid", message);
            return;
        }
        if (lower.contains("tỷ lệ") || lower.contains("giảm")) {
            bindingResult.rejectValue("giaTriGiam", "invalid", message);
            return;
        }
        if (lower.contains("ngày")) {
            bindingResult.rejectValue("ngayKetThuc", "invalid", message);
            return;
        }
        bindingResult.reject("invalid", message);
    }
}
