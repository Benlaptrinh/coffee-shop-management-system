package com.example.demo.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NhanVienDto {
    private Long maNhanVien;
    private String hoTen;
    private String soDienThoai;
    private String diaChi;
    private String chucVu;
    private Long taiKhoanId;
    private boolean enabled;
}
