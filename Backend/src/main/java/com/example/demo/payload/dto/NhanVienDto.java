package com.example.demo.payload.dto;
import lombok.Getter;
import lombok.Setter;
/**
 * DTO for Nhan Vien.
 */
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
