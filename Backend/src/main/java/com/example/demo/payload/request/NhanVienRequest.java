package com.example.demo.payload.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for Nhan Vien create/update.
 */
@Getter
@Setter
public class NhanVienRequest {
    @NotBlank(message = "Họ tên bắt buộc")
    @Size(max = 100, message = "Họ tên tối đa 100 ký tự")
    private String hoTen;

    @Size(max = 255, message = "Địa chỉ tối đa 255 ký tự")
    private String diaChi;

    @Pattern(regexp = "^\\d{9,15}$", message = "Số điện thoại chỉ được nhập số (9-15 chữ số)")
    private String soDienThoai;

    private Long chucVuId;
    private Long taiKhoanId;
    private Boolean enabled;

    // Optional override if needed; typically derived from ChucVu
    private BigDecimal luong;
}
