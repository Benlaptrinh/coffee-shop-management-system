package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * NhanVienForm
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
public class NhanVienForm {

    @NotBlank(message = "Họ tên bắt buộc")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @NotBlank(message = "Địa chỉ bắt buộc")
    @Size(max = 10, message = "Địa chỉ tối đa 10 ký tự")
    private String diaChi;

    @Pattern(regexp = "^\\d{9,15}$", message = "Số điện thoại chỉ được nhập số (9-15 chữ số)")
    private String soDienThoai;

    private String tenDangNhap;

    private String matKhau;

    @NotBlank(message = "Vai trò bắt buộc")
    private String role;

    @NotNull(message = "Trạng thái bắt buộc")
    private Boolean enabled;
}
