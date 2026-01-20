package com.example.demo.controller;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Comparator;
import java.util.Optional;

import com.example.demo.dto.ThietBiForm;
import com.example.demo.entity.ThietBi;
import com.example.demo.service.ThietBiService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * EquipmentController
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
@RequestMapping("/admin/equipment")
public class EquipmentController {

    private final ThietBiService thietBiService;

    /**
     * Creates EquipmentController.
     *
     * @param thietBiService thietBiService
     */
    public EquipmentController(ThietBiService thietBiService) {
        this.thietBiService = thietBiService;
    }

    /**
     * Create.
     *
     * @param thietBi thietBi
     * @param ra ra
     * @return result
     */
    @PostMapping
    public String create(@Valid @ModelAttribute("thietBi") ThietBiForm form,
                         BindingResult bindingResult,
                         Model model,
                         Authentication auth,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(required = false) String keyword,
                         RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return renderEquipmentPage(model, auth, page, keyword);
        }
        thietBiService.save(toEntity(form));
        ra.addFlashAttribute("success", "Thêm thiết bị thành công");
        ra.addAttribute("page", page);
        if (keyword != null && !keyword.trim().isEmpty()) {
            ra.addAttribute("keyword", keyword.trim());
        }
        return "redirect:/admin/equipment";
    }

    /**
     * Edit form.
     *
     * @param id id
     * @param model model
     * @param ra ra
     * @return result
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id,
                           @RequestParam(defaultValue = "1") int page,
                           @RequestParam(required = false) String keyword,
                           RedirectAttributes ra) {
        Optional<ThietBi> opt = thietBiService.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Thiết bị không tồn tại");
            ra.addAttribute("page", page);
            if (keyword != null && !keyword.trim().isEmpty()) {
                ra.addAttribute("keyword", keyword.trim());
            }
            return "redirect:/admin/equipment";
        }
        ra.addFlashAttribute("thietBi", toForm(opt.get()));
        ra.addAttribute("page", page);
        if (keyword != null && !keyword.trim().isEmpty()) {
            ra.addAttribute("keyword", keyword.trim());
        }
        return "redirect:/admin/equipment";
    }

    /**
     * Update.
     *
     * @param id id
     * @param thietBi thietBi
     * @param ra ra
     * @return result
     */
    @PostMapping("/{id}")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("thietBi") ThietBiForm form,
                         BindingResult bindingResult,
                         Model model,
                         Authentication auth,
                         @RequestParam(defaultValue = "1") int page,
                         @RequestParam(required = false) String keyword,
                         RedirectAttributes ra) {
        Optional<ThietBi> opt = thietBiService.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Thiết bị không tồn tại");
            ra.addAttribute("page", page);
            if (keyword != null && !keyword.trim().isEmpty()) {
                ra.addAttribute("keyword", keyword.trim());
            }
            return "redirect:/admin/equipment";
        }
        if (bindingResult.hasErrors()) {
            return renderEquipmentPage(model, auth, page, keyword);
        }
        ThietBi existing = opt.get();
        existing.setTenThietBi(form.getTenThietBi());
        existing.setSoLuong(form.getSoLuong());
        existing.setDonGiaMua(form.getDonGiaMua());
        existing.setNgayMua(form.getNgayMua());
        existing.setGhiChu(form.getGhiChu());
        thietBiService.save(existing);
        ra.addFlashAttribute("success", "Cập nhật thiết bị thành công");
        ra.addAttribute("page", page);
        if (keyword != null && !keyword.trim().isEmpty()) {
            ra.addAttribute("keyword", keyword.trim());
        }
        return "redirect:/admin/equipment";
    }

    private ThietBi toEntity(ThietBiForm form) {
        ThietBi thietBi = new ThietBi();
        thietBi.setMaThietBi(form.getMaThietBi());
        thietBi.setTenThietBi(form.getTenThietBi());
        thietBi.setSoLuong(form.getSoLuong());
        thietBi.setDonGiaMua(form.getDonGiaMua());
        thietBi.setNgayMua(form.getNgayMua());
        thietBi.setGhiChu(form.getGhiChu());
        return thietBi;
    }

    private ThietBiForm toForm(ThietBi entity) {
        ThietBiForm form = new ThietBiForm();
        form.setMaThietBi(entity.getMaThietBi());
        form.setTenThietBi(entity.getTenThietBi());
        form.setSoLuong(entity.getSoLuong());
        form.setDonGiaMua(entity.getDonGiaMua());
        form.setNgayMua(entity.getNgayMua());
        form.setGhiChu(entity.getGhiChu());
        return form;
    }

    private String renderEquipmentPage(Model model, Authentication auth, int page, String keyword) {
        int pageSize = 5;
        String trimmedKeyword = keyword == null ? null : keyword.trim();
        java.util.List<ThietBi> allItems = thietBiService.findAll();
        if (trimmedKeyword != null && !trimmedKeyword.isEmpty()) {
            String lower = trimmedKeyword.toLowerCase();
            allItems.removeIf(item -> {
                String name = item.getTenThietBi();
                return name == null || !name.toLowerCase().contains(lower);
            });
        }
        allItems.sort(Comparator.comparing(
                tb -> tb.getTenThietBi() == null ? "" : tb.getTenThietBi(),
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
        java.util.List<ThietBi> pageItems = totalItems == 0 ? Collections.emptyList() : allItems.subList(fromIndex, toIndex);

        model.addAttribute("username", auth == null ? "anonymous" : auth.getName());
        model.addAttribute("sidebarFragment", "fragments/sidebar-admin");
        model.addAttribute("contentFragment", "admin/equipment");
        model.addAttribute("items", pageItems);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("keyword", trimmedKeyword);
        if (!model.containsAttribute("thietBi")) {
            model.addAttribute("thietBi", new ThietBiForm());
        }
        model.addAttribute("minDate", LocalDate.now().toString());
        return "layout/base";
    }

    /**
     * Delete.
     *
     * @param id id
     * @param ra ra
     * @return result
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        thietBiService.deleteById(id);
        ra.addFlashAttribute("success", "Xóa thiết bị thành công");
        return "redirect:/admin/equipment";
    }
}
