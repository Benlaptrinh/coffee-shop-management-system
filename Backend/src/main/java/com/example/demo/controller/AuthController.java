package com.example.demo.controller;

import java.util.Map;

import com.example.demo.dto.LoginForm;
import com.example.demo.security.LoginValidationFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * AuthController
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
public class AuthController {

    /**
     * Login.
     *
     * @param model model
     * @param request request
     * @return result
     */
    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request) {
        LoginForm form = new LoginForm();
        HttpSession session = request.getSession(false);
        if (session != null) {
            Object username = session.getAttribute(LoginValidationFilter.SESSION_LOGIN_USERNAME);
            if (username instanceof String) {
                form.setUsername((String) username);
                session.removeAttribute(LoginValidationFilter.SESSION_LOGIN_USERNAME);
            }
            @SuppressWarnings("unchecked")
            Map<String, String> fieldErrors = (Map<String, String>) session.getAttribute(LoginValidationFilter.SESSION_LOGIN_ERRORS);
            if (fieldErrors != null) {
                model.addAttribute("fieldErrors", fieldErrors);
                session.removeAttribute(LoginValidationFilter.SESSION_LOGIN_ERRORS);
            }
        }
        model.addAttribute("loginForm", form);
        return "login";
    }
}

