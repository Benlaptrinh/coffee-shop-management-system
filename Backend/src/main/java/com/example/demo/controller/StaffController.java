package com.example.demo.controller;

import com.example.demo.controller.base.BaseController;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * StaffController
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
public class StaffController extends BaseController {

    /**
     * Home.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/staff/home")
    public String home(Model model, Authentication auth) {
        setupLayout(model, "fragments/sidebar-staff", "staff/home", auth.getName());
        return "layout/base";
    }
}

