package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.KhuyenMai;
import com.example.demo.payload.form.KhuyenMaiForm;

/**
 * Service contract for Khuyen Mai.
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
    KhuyenMaiForm getFormById(long id);
    /**
     * Update khuyen mai.
     *
     * @param id id
     * @param form form
     */
    void updateKhuyenMai(long id, KhuyenMaiForm form);
    /**
     * Delete by id.
     *
     * @param id id
     */
    void deleteById(long id);
}

