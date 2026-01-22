package com.example.demo.repository;

import java.util.Optional;

import com.example.demo.entity.TaiKhoan;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Tai Khoan entities.
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


