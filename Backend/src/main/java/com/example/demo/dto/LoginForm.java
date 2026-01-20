package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

/**
 * LoginForm
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
@Getter
@Setter
public class LoginForm {

    @NotBlank(message = "Tên đăng nhập bắt buộc")
    private String username;

    @NotBlank(message = "Mật khẩu bắt buộc")
    private String password;
}
