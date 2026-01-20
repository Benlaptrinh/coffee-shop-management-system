package com.example.demo.controller;

import com.example.demo.dto.ProfileForm;
import com.example.demo.entity.NhanVien;
import com.example.demo.entity.TaiKhoan;
import com.example.demo.service.NhanVienService;
import com.example.demo.service.TaiKhoanService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * ProfileController
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
public class ProfileController {

    private final TaiKhoanService taiKhoanService;
    private final NhanVienService nhanVienService;

    /**
     * Creates ProfileController.
     *
     * @param taiKhoanService taiKhoanService
     * @param nhanVienService nhanVienService
     */
    public ProfileController(TaiKhoanService taiKhoanService, NhanVienService nhanVienService) {
        this.taiKhoanService = taiKhoanService;
        this.nhanVienService = nhanVienService;
    }

    /**
     * View profile.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/profile")
    public String viewProfile(Model model, Authentication auth) {
        String username = auth.getName();
        TaiKhoan tk = taiKhoanService.findByUsername(username).orElse(null);
        if (tk == null) return "redirect:/login";
        NhanVien nv = nhanVienService.findByTaiKhoanId(tk.getMaTaiKhoan()).orElse(null);
        model.addAttribute("taiKhoan", tk);
        model.addAttribute("nhanVien", nv == null ? new NhanVien() : nv);
        
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        model.addAttribute("sidebarFragment", isAdmin ? "fragments/sidebar-admin" : "fragments/sidebar-staff");
        model.addAttribute("contentFragment", "profile/view");
        model.addAttribute("username", username);
        return "layout/base";
    }

    /**
     * Edit profile.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/profile/edit")
    public String editProfile(Model model, Authentication auth) {
        String username = auth.getName();
        TaiKhoan tk = taiKhoanService.findByUsername(username).orElse(null);
        if (tk == null) return "redirect:/login";
        NhanVien nv = nhanVienService.findByTaiKhoanId(tk.getMaTaiKhoan()).orElse(new NhanVien());
        ProfileForm form = new ProfileForm();
        form.setHoTen(nv.getHoTen());
        form.setDiaChi(nv.getDiaChi());
        form.setSoDienThoai(nv.getSoDienThoai());
        model.addAttribute("form", form);
        boolean isAdmin = auth.getAuthorities().stream()
                .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        model.addAttribute("sidebarFragment", isAdmin ? "fragments/sidebar-admin" : "fragments/sidebar-staff");
        model.addAttribute("contentFragment", "profile/edit");
        model.addAttribute("username", username);
        return "layout/base";
    }

    /**
     * Update profile.
     *
     * @param auth auth
     * @param form form
     * @param bindingResult bindingResult
     * @param model model
     * @return result
     */
    @PostMapping("/profile/edit")
    public String updateProfile(Authentication auth,
                                @Valid @ModelAttribute("form") ProfileForm form,
                                BindingResult bindingResult,
                                Model model) {
        String username = auth.getName();
        TaiKhoan tk = taiKhoanService.findByUsername(username).orElse(null);
        if (tk == null) return "redirect:/login";
        if (bindingResult.hasErrors()) {
            boolean isAdmin = auth.getAuthorities().stream()
                    .anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
            model.addAttribute("sidebarFragment", isAdmin ? "fragments/sidebar-admin" : "fragments/sidebar-staff");
            model.addAttribute("contentFragment", "profile/edit");
            model.addAttribute("username", username);
            return "layout/base";
        }
        NhanVien nv = nhanVienService.findByTaiKhoanId(tk.getMaTaiKhoan()).orElse(new NhanVien());
        nv.setHoTen(form.getHoTen());
        nv.setDiaChi(form.getDiaChi());
        nv.setSoDienThoai(form.getSoDienThoai());
        nv.setTaiKhoan(tk);
        nhanVienService.save(nv);
        return "redirect:/profile";
    }
}
