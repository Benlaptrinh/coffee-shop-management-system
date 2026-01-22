package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.entity.NhanVien;
import com.example.demo.payload.dto.HangHoaKhoDto;
import com.example.demo.payload.form.EditHangHoaForm;
import com.example.demo.payload.form.HangHoaNhapForm;

/**
 * Service contract for Hang Hoa.
 */
public interface HangHoaService {
    /**
     * Get danh sach kho.
     *
     * @return result
     */
    List<HangHoaKhoDto> getDanhSachKho();
    /**
     * Nhap hang.
     *
     * @param form form
     * @param nhanVien nhanVien
     */
    void nhapHang(HangHoaNhapForm form, NhanVien nhanVien);
    /**
     * Xuat hang.
     *
     * @param hangHoaId hangHoaId
     * @param soLuong soLuong
     * @param ngayXuat ngayXuat
     * @param nhanVien nhanVien
     */
    void xuatHang(long hangHoaId, int soLuong, LocalDateTime ngayXuat, NhanVien nhanVien);
    /**
     * Update hang hoa.
     *
     * @param form form
     */
    void updateHangHoa(EditHangHoaForm form);
    /**
     * Delete hang hoa.
     *
     * @param id id
     */
    void deleteHangHoa(long id);
    /**
     * Search hang hoa.
     *
     * @param keyword keyword
     * @return result
     */
    List<HangHoaKhoDto> searchHangHoa(String keyword);
}

