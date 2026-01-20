package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.demo.dto.EditHangHoaForm;
import com.example.demo.dto.HangHoaKhoDTO;
import com.example.demo.dto.HangHoaNhapForm;
import com.example.demo.entity.NhanVien;

/**
 * HangHoaService
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
public interface HangHoaService {
    /**
     * Get danh sach kho.
     *
     * @return result
     */
    List<HangHoaKhoDTO> getDanhSachKho();
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
    void xuatHang(Long hangHoaId, Integer soLuong, LocalDateTime ngayXuat, NhanVien nhanVien);
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
    void deleteHangHoa(Long id);
    /**
     * Search hang hoa.
     *
     * @param keyword keyword
     * @return result
     */
    List<HangHoaKhoDTO> searchHangHoa(String keyword);
}


