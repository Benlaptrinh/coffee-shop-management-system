package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.NhanVien;

/**
 * NhanVienService
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
public interface NhanVienService {
    /**
     * Save.
     *
     * @param nhanVien nhanVien
     * @return result
     */
    NhanVien save(NhanVien nhanVien);
    /**
     * Find all.
     *
     * @return result
     */
    List<NhanVien> findAll();
    /**
     * Find by id.
     *
     * @param id id
     * @return result
     */
    Optional<NhanVien> findById(Long id);
    /**
     * Find by tai khoan id.
     *
     * @param maTaiKhoan maTaiKhoan
     * @return result
     */
    Optional<NhanVien> findByTaiKhoanId(Long maTaiKhoan);
    /**
     * Delete by tai khoan id.
     *
     * @param maTaiKhoan maTaiKhoan
     */
    void deleteByTaiKhoanId(Long maTaiKhoan);
    /**
     * Delete by id.
     *
     * @param id id
     */
    void deleteById(Long id);
    /**
     * Find by ho ten containing.
     *
     * @param keyword keyword
     * @return result
     */
    List<NhanVien> findByHoTenContaining(String keyword);
}

