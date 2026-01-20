package com.example.demo.service;

import java.util.List;

import com.example.demo.dto.KhuyenMaiForm;
import com.example.demo.entity.KhuyenMai;

/**
 * KhuyenMaiService
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
public interface KhuyenMaiService {
    /**
     * Get all khuyen mai.
     *
     * @return result
     */
    List<KhuyenMai> getAllKhuyenMai();
    /**
     * Create khuyen mai.
     *
     * @param form form
     */
    void createKhuyenMai(KhuyenMaiForm form);
    /**
     * Get form by id.
     *
     * @param id id
     * @return result
     */
    KhuyenMaiForm getFormById(Long id);
    /**
     * Update khuyen mai.
     *
     * @param id id
     * @param form form
     */
    void updateKhuyenMai(Long id, KhuyenMaiForm form);
    /**
     * Delete by id.
     *
     * @param id id
     */
    void deleteById(Long id);
}


