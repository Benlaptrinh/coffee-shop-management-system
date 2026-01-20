package com.example.demo.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.demo.controller.base.BaseController;
import com.example.demo.dto.NhanVienForm;
import com.example.demo.entity.NhanVien;
import com.example.demo.entity.TaiKhoan;
import com.example.demo.enums.Role;
import com.example.demo.service.NhanVienService;
import com.example.demo.service.TaiKhoanService;
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
 * AdminEmployeesController
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
@RequestMapping("/admin/employees")
public class AdminEmployeesController extends BaseController {

    private final NhanVienService nhanVienService;
    private final TaiKhoanService taiKhoanService;

    /**
     * Creates AdminEmployeesController.
     *
     * @param nhanVienService nhanVienService
     * @param taiKhoanService taiKhoanService
     */
    public AdminEmployeesController(NhanVienService nhanVienService, TaiKhoanService taiKhoanService) {
        this.nhanVienService = nhanVienService;
        this.taiKhoanService = taiKhoanService;
    }

    /**
     * List.
     *
     * @param q q
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping
    public String list(@RequestParam(value = "q", required = false) String q,
                       @RequestParam(defaultValue = "1") int page,
                       Model model,
                       Authentication auth) {
        List<NhanVien> list;
        if (q != null && !q.isBlank()) {
            list = nhanVienService.findByHoTenContaining(q);
        } else {
            list = nhanVienService.findAll();
        }
        list.sort(Comparator.comparing(
                nv -> nv.getHoTen() == null ? "" : nv.getHoTen(),
                String.CASE_INSENSITIVE_ORDER
        ));
        int pageSize = 5;
        int totalItems = list.size();
        int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<NhanVien> pageItems = totalItems == 0 ? Collections.emptyList() : list.subList(fromIndex, toIndex);

        model.addAttribute("nhanViens", pageItems);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("q", q);
        setupAdminLayout(model, "admin/employees_list", auth);
        return "layout/base";
    }

    /**
     * Create form.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/create")
    public String createForm(Model model, Authentication auth) {
        NhanVienForm form = new NhanVienForm();
        form.setRole(Role.NHANVIEN.name());
        form.setEnabled(true);
        model.addAttribute("form", form);
        setupAdminLayout(model, "admin/employees_create", auth);
        return "layout/base";
    }

    /**
     * Create.
     *
     * @param form form
     * @param bindingResult bindingResult
     * @param model model
     * @param auth auth
     * @param redirectAttributes redirectAttributes
     * @return result
     */
    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("form") NhanVienForm form,
                         BindingResult bindingResult,
                         Model model,
                         Authentication auth,
                         RedirectAttributes redirectAttributes) {
        if (form.getTenDangNhap() != null && !form.getTenDangNhap().isBlank()) {
            taiKhoanService.findByUsername(form.getTenDangNhap())
                    .ifPresent(tk -> bindingResult.rejectValue("tenDangNhap", "duplicate", "Tên đăng nhập đã tồn tại"));
        }
        if (bindingResult.hasErrors()) {
            setupAdminLayout(model, "admin/employees_create", auth);
            return "layout/base";
        }
        NhanVien nhanVien = new NhanVien();
        nhanVien.setHoTen(form.getHoTen());
        nhanVien.setDiaChi(form.getDiaChi());
        nhanVien.setSoDienThoai(form.getSoDienThoai());
        nhanVien.setEnabled(form.getEnabled() != null ? form.getEnabled() : Boolean.TRUE);
        try {
            if (form.getTenDangNhap() != null && !form.getTenDangNhap().isBlank()) {
                if (form.getMatKhau() == null || form.getMatKhau().isBlank()) {
                    bindingResult.rejectValue("matKhau", "required", "Mật khẩu bắt buộc khi tạo tài khoản");
                    setupAdminLayout(model, "admin/employees_create", auth);
                    return "layout/base";
                }
                TaiKhoan tk = new TaiKhoan();
                tk.setTenDangNhap(form.getTenDangNhap());
                tk.setMatKhau(form.getMatKhau());
                try {
                    Role r = (form.getRole() == null || form.getRole().isBlank()) ? Role.NHANVIEN : Role.valueOf(form.getRole());
                    tk.setQuyenHan(r);
                } catch (IllegalArgumentException ex) {
                    tk.setQuyenHan(Role.NHANVIEN);
                }
                tk.setEnabled(nhanVien.getEnabled() != null ? nhanVien.getEnabled() : true);
                TaiKhoan savedTk = taiKhoanService.save(tk);
                nhanVien.setTaiKhoan(savedTk);
            }
            nhanVienService.save(nhanVien);
            redirectAttributes.addFlashAttribute("message", "Thêm nhân viên thành công");
            return "redirect:/admin/employees";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            setupAdminLayout(model, "admin/employees_create", auth);
            return "layout/base";
        }
    }

    /**
     * Edit form.
     *
     * @param id id
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication auth) {
        NhanVien nv = nhanVienService.findById(id).orElse(new NhanVien());
        model.addAttribute("form", toForm(nv));
        model.addAttribute("employeeId", id);
        setupAdminLayout(model, "admin/employees_edit", auth);
        return "layout/base";
    }

    /**
     * Edit.
     *
     * @param id id
     * @param form form
     * @param bindingResult bindingResult
     * @param model model
     * @param auth auth
     * @param redirectAttributes redirectAttributes
     * @return result
     */
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @Valid @ModelAttribute("form") NhanVienForm form,
                       BindingResult bindingResult,
                       Model model,
                       Authentication auth,
                       RedirectAttributes redirectAttributes) {
        NhanVien existing = nhanVienService.findById(id).orElse(null);
        if (existing == null) {
            redirectAttributes.addFlashAttribute("error", "Nhân viên không tồn tại");
            return "redirect:/admin/employees";
        }
        if (form.getTenDangNhap() != null && !form.getTenDangNhap().isBlank()) {
            taiKhoanService.findByUsername(form.getTenDangNhap())
                    .ifPresent(tk -> {
                        if (existing.getTaiKhoan() == null || !tk.getMaTaiKhoan().equals(existing.getTaiKhoan().getMaTaiKhoan())) {
                            bindingResult.rejectValue("tenDangNhap", "duplicate", "Tên đăng nhập đã tồn tại");
                        }
                    });
        }
        if (bindingResult.hasErrors()) {
            model.addAttribute("employeeId", id);
            setupAdminLayout(model, "admin/employees_edit", auth);
            return "layout/base";
        }
        existing.setHoTen(form.getHoTen());
        existing.setDiaChi(form.getDiaChi());
        existing.setSoDienThoai(form.getSoDienThoai());
        existing.setEnabled(form.getEnabled() != null ? form.getEnabled() : existing.getEnabled());
        
        try {
            if (form.getTenDangNhap() != null && !form.getTenDangNhap().isBlank()) {
                if (existing.getTaiKhoan() == null) {
                    if (form.getMatKhau() == null || form.getMatKhau().isBlank()) {
                        bindingResult.rejectValue("matKhau", "required", "Mật khẩu bắt buộc khi tạo tài khoản");
                        model.addAttribute("employeeId", id);
                        setupAdminLayout(model, "admin/employees_edit", auth);
                        return "layout/base";
                    }
                    TaiKhoan tk = new TaiKhoan();
                    tk.setTenDangNhap(form.getTenDangNhap());
                    tk.setMatKhau(form.getMatKhau());
                    try {
                        Role r = (form.getRole() == null || form.getRole().isBlank()) ? Role.NHANVIEN : Role.valueOf(form.getRole());
                        tk.setQuyenHan(r);
                    } catch (IllegalArgumentException ex) {
                        tk.setQuyenHan(Role.NHANVIEN);
                    }
                    tk.setEnabled(existing.getEnabled() != null ? existing.getEnabled() : true);
                    TaiKhoan saved = taiKhoanService.save(tk);
                    existing.setTaiKhoan(saved);
                } else {
                    TaiKhoan tk = existing.getTaiKhoan();
                    tk.setTenDangNhap(form.getTenDangNhap());
                    if (form.getMatKhau() != null && !form.getMatKhau().isBlank()) tk.setMatKhau(form.getMatKhau());
                    try {
                        Role r = (form.getRole() == null || form.getRole().isBlank()) ? tk.getQuyenHan() : Role.valueOf(form.getRole());
                        tk.setQuyenHan(r);
                    } catch (IllegalArgumentException ex) {
                    }
                    if (existing.getEnabled() != null) {
                        tk.setEnabled(existing.getEnabled());
                    }
                    taiKhoanService.save(tk);
                }
            }
            nhanVienService.save(existing);
            redirectAttributes.addFlashAttribute("message", "Cập nhật thành công");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("empl    oyeeId", id);
            setupAdminLayout(model, "admin/employees_edit", auth);
            return "layout/base";
        }
        return "redirect:/admin/employees";
    }

    private NhanVienForm toForm(NhanVien nv) {
        NhanVienForm form = new NhanVienForm();
        form.setHoTen(nv.getHoTen());
        form.setDiaChi(nv.getDiaChi());
        form.setSoDienThoai(nv.getSoDienThoai());
        form.setEnabled(nv.getEnabled() != null ? nv.getEnabled() : Boolean.TRUE);
        if (nv.getTaiKhoan() != null) {
            form.setTenDangNhap(nv.getTaiKhoan().getTenDangNhap());
            if (nv.getTaiKhoan().getQuyenHan() != null) {
                form.setRole(nv.getTaiKhoan().getQuyenHan().name());
            }
        } else {
            form.setRole(Role.NHANVIEN.name());
        }
        return form;
    }

    /**
     * Delete.
     *
     * @param id id
     * @param redirectAttributes redirectAttributes
     * @return result
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, Authentication auth, RedirectAttributes redirectAttributes) {
        if (auth != null) {
            var current = taiKhoanService.findByUsername(auth.getName())
                    .flatMap(tk -> nhanVienService.findByTaiKhoanId(tk.getMaTaiKhoan()));
            if (current.isPresent() && current.get().getMaNhanVien() != null
                    && current.get().getMaNhanVien().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa chính mình");
                return "redirect:/admin/employees";
            }
        }
        nhanVienService.deleteById(id);
        redirectAttributes.addFlashAttribute("message", "Đã xóa (nếu tồn tại)");
        return "redirect:/admin/employees";
    }
}
