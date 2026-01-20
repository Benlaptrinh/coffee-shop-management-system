package com.example.demo.controller.base;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * GlobalModelAttributes
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
 * 09-01-2026  Viet    Create
 */
@ControllerAdvice
public class GlobalModelAttributes {

    /**
     * Add common attributes.
     *
     * @param model model
     * @param request request
     */
    @ModelAttribute
    public void addCommonAttributes(Model model, HttpServletRequest request) {
        String uri = "";
        if (request != null) {
            uri = request.getRequestURI();
            String contextPath = request.getContextPath();
            if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
                uri = uri.substring(contextPath.length());
            }
        }
        model.addAttribute("currentPath", uri);
    }
}
