package com.example.demo.controller;

import com.example.demo.controller.base.BaseController;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * StaffPagesController
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
@RequestMapping("/staff")
public class StaffPagesController extends BaseController {

    /**
     * Sales.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/sales")
    public String sales(Model model, Authentication auth) {
        setupStaffLayout(model, "staff/sales", auth);
        return "layout/base";
    }
}

