package com.example.demo.security;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

/**
 * LoginAuthFailureHandler
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
public class LoginAuthFailureHandler implements AuthenticationFailureHandler {

    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates LoginAuthFailureHandler.
     *
     * @param userDetailsService userDetailsService
     * @param passwordEncoder passwordEncoder
     */
    public LoginAuthFailureHandler(UserDetailsService userDetailsService,
                                   PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Handle login failure and attach field errors.
     *
     * @param request request
     * @param response response
     * @param exception exception
     * @throws IOException if an I/O error occurs
     */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request,
                                        HttpServletResponse response,
                                        AuthenticationException exception) throws IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");

        Map<String, String> fieldErrors = new HashMap<>();

        if (username == null || username.isBlank()) {
            fieldErrors.put("username", "Tên đăng nhập bắt buộc");
        } else if (password == null || password.isBlank()) {
            fieldErrors.put("password", "Mật khẩu bắt buộc");
        } else {
            if (exception instanceof DisabledException || exception instanceof LockedException) {
                fieldErrors.put("username", "Tài khoản không hoạt động");
            } else {
                try {
                    UserDetails user = userDetailsService.loadUserByUsername(username);
                    if (!user.isEnabled()) {
                        fieldErrors.put("username", "Tài khoản không hoạt động");
                    } else if (!passwordEncoder.matches(password, user.getPassword())) {
                        fieldErrors.put("password", "Mật khẩu không đúng");
                    } else {
                        fieldErrors.put("password", "Đăng nhập thất bại");
                    }
                } catch (UsernameNotFoundException ex) {
                    fieldErrors.put("username", "Tên đăng nhập không tồn tại");
                }
            }
        }

        HttpSession session = request.getSession(true);
        session.setAttribute(LoginValidationFilter.SESSION_LOGIN_ERRORS, fieldErrors);
        session.setAttribute(LoginValidationFilter.SESSION_LOGIN_USERNAME, username);

        response.sendRedirect(request.getContextPath() + "/login");
    }
}
