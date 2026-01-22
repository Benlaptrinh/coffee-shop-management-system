package com.example.demo.repository;

import java.util.Optional;

import com.example.demo.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * TaiKhoanRepository
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
public interface TaiKhoanRepository extends JpaRepository<TaiKhoan, Long> {
    /**
     * Find by ten dang nhap.
     *
     * @param tenDangNhap tenDangNhap
     * @return result
     */
    Optional<TaiKhoan> findByTenDangNhap(String tenDangNhap);
}


