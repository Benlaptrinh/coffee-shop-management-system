package com.example.demo.controller;

import com.example.demo.controller.base.BaseController;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * AdminController
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
public class AdminController extends BaseController {

    /**
     * Dashboard.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/admin/dashboard")
    public String dashboard(Model model, Authentication auth) {
        setupLayout(model, "fragments/sidebar-admin", "admin/dashboard", auth.getName());
        return "layout/base";
    }
}

