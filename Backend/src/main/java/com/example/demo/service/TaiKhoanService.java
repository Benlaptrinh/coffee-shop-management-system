package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.TaiKhoan;

/**
 * Service contract for Tai Khoan.
 */
public interface TaiKhoanService {
    /**
     * Save.
     *
     * @param taiKhoan taiKhoan
     * @return result
     */
    TaiKhoan save(TaiKhoan taiKhoan);
    /**
     * Find all.
     *
     * @return result
     */
    List<TaiKhoan> findAll();
    /**
     * Find by id.
     *
     * @param id id
     * @return result
     */
    Optional<TaiKhoan> findById(long id);
    /**
     * Find by username.
     *
     * @param username username
     * @return result
     */
    Optional<TaiKhoan> findByUsername(String username);
    /**
     * Disable.
     *
     * @param id id
     */
    void disable(long id);
}

