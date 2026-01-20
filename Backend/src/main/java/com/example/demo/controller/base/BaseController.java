package com.example.demo.controller.base;

import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;

/**
 * BaseController
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
public abstract class BaseController {

    protected void setupAdminLayout(Model model, String contentFragment) {
        setupLayout(model, "fragments/sidebar-admin", contentFragment, null);
    }

    protected void setupAdminLayout(Model model, String contentFragment, Authentication auth) {
        setupLayout(model, "fragments/sidebar-admin", contentFragment, resolveUsername(auth));
    }

    protected void setupStaffLayout(Model model, String contentFragment) {
        setupLayout(model, "fragments/sidebar-staff", contentFragment, null);
    }

    protected void setupStaffLayout(Model model, String contentFragment, Authentication auth) {
        setupLayout(model, "fragments/sidebar-staff", contentFragment, resolveUsername(auth));
    }

    protected void setupLayout(Model model, String sidebarFragment, String contentFragment, String username) {
        model.addAttribute("sidebarFragment", sidebarFragment);
        model.addAttribute("contentFragment", contentFragment);
        if (username != null) {
            model.addAttribute("username", username);
        }
    }

    protected String resolveUsername(Authentication auth) {
        return auth == null ? "anonymous" : auth.getName();
    }
}
