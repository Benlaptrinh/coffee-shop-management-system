package com.example.demo.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.demo.entity.ThietBi;
import com.example.demo.repository.ThietBiRepository;
import com.example.demo.service.ThietBiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Thiet Bi.
 */
@Service
public class ThietBiServiceImpl implements ThietBiService {

    private static final Logger log = LoggerFactory.getLogger(ThietBiServiceImpl.class);

    private final ThietBiRepository thietBiRepository;

    /**
     * Creates ThietBiServiceImpl.
     *
     * @param thietBiRepository thietBiRepository
     */
    public ThietBiServiceImpl(ThietBiRepository thietBiRepository) {
        this.thietBiRepository = thietBiRepository;
    }

    /**
     * Find all.
     *
     * @return result
     */
    @Override
    public List<ThietBi> findAll() {
        return thietBiRepository.findAll();
    }

    /**
     * Find by id.
     *
     * @param id id
     * @return result
     */
    @Override
    public Optional<ThietBi> findById(long id) {
        return thietBiRepository.findById(id);
    }

    /**
     * Save.
     *
     * @param thietBi thietBi
     * @return result
     */
    @Override
    @Transactional
    public ThietBi save(ThietBi thietBi) {
        if (thietBi.getTenThietBi() == null || thietBi.getTenThietBi().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên thiết bị bắt buộc");
        }
        if (thietBi.getSoLuong() == null || thietBi.getSoLuong() <= 0) {
            throw new IllegalArgumentException("Số lượng phải lớn hơn 0");
        }
        if (thietBi.getSoLuong() != null && thietBi.getSoLuong() > 10) {
            throw new IllegalArgumentException("Số lượng tối đa 10");
        }
        if (thietBi.getDonGiaMua() == null || thietBi.getDonGiaMua().compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá mua phải lớn hơn hoặc bằng 0");
        }
        if (thietBi.getNgayMua() == null) {
            throw new IllegalArgumentException("Ngày mua bắt buộc");
        }
        if (thietBi.getNgayMua().isAfter(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Ngày mua không được sau hôm nay");
        }
        ThietBi saved = thietBiRepository.save(thietBi);
        log.info("Saved thietBi id={} soLuong={}", saved.getMaThietBi(), saved.getSoLuong());
        return saved;
    }

    /**
     * Delete by id.
     *
     * @param id id
     */
    @Override
    @Transactional
    public void deleteById(long id) {
        thietBiRepository.deleteById(id);
        log.info("Deleted thietBi id={}", id);
    }
}
