package com.example.demo.service.impl;

import java.math.BigDecimal;
import java.util.List;

import com.example.demo.dto.KhuyenMaiForm;
import com.example.demo.entity.KhuyenMai;
import com.example.demo.repository.KhuyenMaiRepository;
import com.example.demo.service.KhuyenMaiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * KhuyenMaiServiceImpl
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
@Service
public class KhuyenMaiServiceImpl implements KhuyenMaiService {

    private static final Logger log = LoggerFactory.getLogger(KhuyenMaiServiceImpl.class);

    private final KhuyenMaiRepository khuyenMaiRepository;

    /**
     * Creates KhuyenMaiServiceImpl.
     *
     * @param khuyenMaiRepository khuyenMaiRepository
     */
    public KhuyenMaiServiceImpl(KhuyenMaiRepository khuyenMaiRepository) {
        this.khuyenMaiRepository = khuyenMaiRepository;
    }

    /**
     * Get all khuyen mai.
     *
     * @return result
     */
    @Override
    public List<KhuyenMai> getAllKhuyenMai() {
        return khuyenMaiRepository.findAll();
    }
    
    /**
     * Create khuyen mai.
     *
     * @param form form
     */
    @Override
    public void createKhuyenMai(KhuyenMaiForm form) {
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }

        if (form.getTenKhuyenMai() == null || form.getTenKhuyenMai().isBlank()) {
            throw new IllegalArgumentException("Chưa nhập tên khuyến mãi");
        }

        if (form.getGiaTriGiam() == null || form.getGiaTriGiam() <= 0 || form.getGiaTriGiam() > 100) {
            throw new IllegalArgumentException("Tỷ lệ giảm giá không hợp lệ (1-100)");
        }

        if (form.getNgayBatDau() != null && form.getNgayKetThuc() != null
                && form.getNgayBatDau().isAfter(form.getNgayKetThuc())) {
            throw new IllegalArgumentException("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc");
        }

        KhuyenMai km = new KhuyenMai();
        km.setTenKhuyenMai(form.getTenKhuyenMai());
        km.setNgayBatDau(form.getNgayBatDau());
        km.setNgayKetThuc(form.getNgayKetThuc());
        
        km.setGiaTriGiam(BigDecimal.valueOf(form.getGiaTriGiam()));

        KhuyenMai saved = khuyenMaiRepository.save(km);
        log.info("Created khuyenMai id={} ten={}", saved.getMaKhuyenMai(), saved.getTenKhuyenMai());
    }
    
    /**
     * Get form by id.
     *
     * @param id id
     * @return result
     */
    @Override
    public KhuyenMaiForm getFormById(long id) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khuyến mãi không tồn tại"));

        KhuyenMaiForm form = new KhuyenMaiForm();
        form.setTenKhuyenMai(km.getTenKhuyenMai());
        form.setNgayBatDau(km.getNgayBatDau());
        form.setNgayKetThuc(km.getNgayKetThuc());
        if (km.getGiaTriGiam() != null) {
            form.setGiaTriGiam(km.getGiaTriGiam().intValue());
        }
        return form;
    }

    /**
     * Update khuyen mai.
     *
     * @param id id
     * @param form form
     */
    @Override
    public void updateKhuyenMai(long id, KhuyenMaiForm form) {
        KhuyenMai km = khuyenMaiRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khuyến mãi không tồn tại"));

        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }

        if (form.getTenKhuyenMai() == null || form.getTenKhuyenMai().isBlank()) {
            throw new IllegalArgumentException("Chưa nhập tên khuyến mãi");
        }

        if (form.getGiaTriGiam() == null || form.getGiaTriGiam() <= 0 || form.getGiaTriGiam() > 100) {
            throw new IllegalArgumentException("Tỷ lệ giảm giá không hợp lệ (1-100)");
        }

        if (form.getNgayBatDau() != null && form.getNgayKetThuc() != null
                && form.getNgayBatDau().isAfter(form.getNgayKetThuc())) {
            throw new IllegalArgumentException("Ngày bắt đầu phải nhỏ hơn hoặc bằng ngày kết thúc");
        }

        km.setTenKhuyenMai(form.getTenKhuyenMai());
        km.setNgayBatDau(form.getNgayBatDau());
        km.setNgayKetThuc(form.getNgayKetThuc());
        km.setGiaTriGiam(BigDecimal.valueOf(form.getGiaTriGiam()));

        KhuyenMai saved = khuyenMaiRepository.save(km);
        log.info("Updated khuyenMai id={} ten={}", saved.getMaKhuyenMai(), saved.getTenKhuyenMai());
    }

    /**
     * Delete by id.
     *
     * @param id id
     */
    @Override
    public void deleteById(long id) {
        khuyenMaiRepository.deleteById(id);
        log.info("Deleted khuyenMai id={}", id);
    }
}
