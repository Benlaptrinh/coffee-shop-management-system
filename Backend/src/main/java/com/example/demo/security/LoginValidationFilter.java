package com.example.demo.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.example.demo.dto.LoginForm;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * LoginValidationFilter
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
@Component
public class LoginValidationFilter extends OncePerRequestFilter {

    public static final String SESSION_LOGIN_ERRORS = "LOGIN_ERRORS";
    public static final String SESSION_LOGIN_USERNAME = "LOGIN_USERNAME";

    private final Validator validator;
    private final RequestMatcher loginMatcher = new AntPathRequestMatcher("/login", "POST");

    /**
     * Creates LoginValidationFilter.
     *
     * @param validator validator
     */
    public LoginValidationFilter(Validator validator) {
        this.validator = validator;
    }

    /**
     * Validate login request before authentication.
     *
     * @param request request
     * @param response response
     * @param filterChain filterChain
     * @throws ServletException if a servlet error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (!loginMatcher.matches(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        HttpSession existing = request.getSession(false);
        if (existing != null) {
            existing.removeAttribute(SESSION_LOGIN_ERRORS);
            existing.removeAttribute(SESSION_LOGIN_USERNAME);
        }

        LoginForm form = new LoginForm();
        form.setUsername(request.getParameter("username"));
        form.setPassword(request.getParameter("password"));

        Set<ConstraintViolation<LoginForm>> violations = validator.validate(form);
        if (!violations.isEmpty()) {
            Map<String, String> fieldErrors = new HashMap<>();
            for (ConstraintViolation<LoginForm> v : violations) {
                fieldErrors.put(v.getPropertyPath().toString(), v.getMessage());
            }
            HttpSession session = request.getSession(true);
            session.setAttribute(SESSION_LOGIN_ERRORS, fieldErrors);
            session.setAttribute(SESSION_LOGIN_USERNAME, form.getUsername());
            response.sendRedirect(request.getContextPath() + "/login");
            return;
        }

        filterChain.doFilter(request, response);
    }
}
