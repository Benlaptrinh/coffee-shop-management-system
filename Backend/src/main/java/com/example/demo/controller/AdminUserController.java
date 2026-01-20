package com.example.demo.controller;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.example.demo.entity.ChucVu;
import com.example.demo.entity.NhanVien;
import com.example.demo.entity.TaiKhoan;
import com.example.demo.repository.ChucVuRepository;
import com.example.demo.service.NhanVienService;
import com.example.demo.service.TaiKhoanService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * AdminUserController
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
@RequestMapping("/admin/users")
public class AdminUserController {

    private final TaiKhoanService taiKhoanService;
    private final NhanVienService nhanVienService;
    private final ChucVuRepository chucVuRepository;

    /**
     * Creates AdminUserController.
     *
     * @param taiKhoanService taiKhoanService
     * @param nhanVienService nhanVienService
     * @param chucVuRepository chucVuRepository
     */
    public AdminUserController(TaiKhoanService taiKhoanService,
                               NhanVienService nhanVienService,
                               ChucVuRepository chucVuRepository) {
        this.taiKhoanService = taiKhoanService;
        this.nhanVienService = nhanVienService;
        this.chucVuRepository = chucVuRepository;
    }

    /**
     * List.
     *
     * @param model model
     * @return result
     */
    @GetMapping
    public String list(@RequestParam(defaultValue = "1") int page, Model model) {
        List<TaiKhoan> users = taiKhoanService.findAll();
        users.sort(Comparator.comparing(
                tk -> tk.getTenDangNhap() == null ? "" : tk.getTenDangNhap(),
                String.CASE_INSENSITIVE_ORDER
        ));
        int pageSize = 5;
        int totalItems = users.size();
        int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<TaiKhoan> pageItems = totalItems == 0 ? Collections.emptyList() : users.subList(fromIndex, toIndex);

        model.addAttribute("users", pageItems);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", pageSize);
        return "admin/users/list";
    }

    /**
     * Create form.
     *
     * @param model model
     * @return result
     */
    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("taiKhoan", new TaiKhoan());
        model.addAttribute("nhanVien", new NhanVien());
        List<ChucVu> chucVus = chucVuRepository.findAll();
        model.addAttribute("chucVus", chucVus);
        return "admin/users/create";
    }

    /**
     * Create.
     *
     * @param taiKhoan taiKhoan
     * @param nhanVien nhanVien
     * @param chucVuId chucVuId
     * @param redirectAttributes redirectAttributes
     * @return result
     */
    @PostMapping("/create")
    public String create(@ModelAttribute TaiKhoan taiKhoan,
                         @ModelAttribute NhanVien nhanVien,
                         @RequestParam(value = "chucVuId", required = false) Long chucVuId,
                         RedirectAttributes redirectAttributes) {
        try {
            TaiKhoan saved = taiKhoanService.save(taiKhoan);
            if (chucVuId != null) {
                chucVuRepository.findById(chucVuId).ifPresent(nhanVien::setChucVu);
            }
            nhanVien.setTaiKhoan(saved);
            nhanVienService.save(nhanVien);
            return "redirect:/admin/users";
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            redirectAttributes.addFlashAttribute("taiKhoan", taiKhoan);
            redirectAttributes.addFlashAttribute("nhanVien", nhanVien);
            return "redirect:/admin/users/create";
        }
    }

    /**
     * Edit form.
     *
     * @param id id
     * @param model model
     * @return result
     */
    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        TaiKhoan tk = taiKhoanService.findById(id).orElse(null);
        if (tk == null) return "redirect:/admin/users";
        model.addAttribute("taiKhoan", tk);
        
        nhanVienService.findAll().stream()
                .filter(n -> n.getTaiKhoan() != null && n.getTaiKhoan().getMaTaiKhoan().equals(id))
                .findFirst()
                .ifPresent(n -> model.addAttribute("nhanVien", n));
        model.addAttribute("chucVus", chucVuRepository.findAll());
        return "admin/users/edit";
    }

    /**
     * Edit.
     *
     * @param id id
     * @param taiKhoan taiKhoan
     * @param nhanVien nhanVien
     * @param chucVuId chucVuId
     * @param redirectAttributes redirectAttributes
     * @return result
     */
    @PostMapping("/{id}/edit")
    public String edit(@PathVariable Long id,
                       @ModelAttribute TaiKhoan taiKhoan,
                       @ModelAttribute NhanVien nhanVien,
                       @RequestParam(value = "chucVuId", required = false) Long chucVuId,
                       RedirectAttributes redirectAttributes) {
        TaiKhoan existing = taiKhoanService.findById(id).orElse(null);
        if (existing == null) return "redirect:/admin/users";
        existing.setQuyenHan(taiKhoan.getQuyenHan());
        if (taiKhoan.getMatKhau() != null && !taiKhoan.getMatKhau().isEmpty()) {
            existing.setMatKhau(taiKhoan.getMatKhau()); 
        }
        try {
            taiKhoanService.save(existing);
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/users/" + id + "/edit";
        }

        
        nhanVienService.findById(nhanVien.getMaNhanVien()).ifPresent(n -> {
            n.setHoTen(nhanVien.getHoTen());
            n.setSoDienThoai(nhanVien.getSoDienThoai());
            n.setDiaChi(nhanVien.getDiaChi());
            if (chucVuId != null) {
                chucVuRepository.findById(chucVuId).ifPresent(n::setChucVu);
            }
            nhanVienService.save(n);
        });

        return "redirect:/admin/users";
    }

    /**
     * Disable.
     *
     * @param id id
     * @return result
     */
    @PostMapping("/{id}/disable")
    public String disable(@PathVariable Long id) {
        taiKhoanService.disable(id);
        return "redirect:/admin/users";
    }

    /**
     * Delete.
     *
     * @param id id
     * @param redirectAttributes redirectAttributes
     * @return result
     */
    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        
        nhanVienService.deleteByTaiKhoanId(id);
        taiKhoanService.findById(id).ifPresent(t -> {
            
            try {
                
                
                taiKhoanService.disable(id);
            } catch (Exception ignored) {}
        });
        redirectAttributes.addFlashAttribute("message", "Deleted user if existed");
        return "redirect:/admin/users";
    }
}
