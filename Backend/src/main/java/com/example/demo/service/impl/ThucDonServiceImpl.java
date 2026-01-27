package com.example.demo.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.demo.entity.ThucDon;
import com.example.demo.repository.ThucDonRepository;
import com.example.demo.service.ThucDonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

/**
 * Service implementation for Thuc Don.
 */
@Service
public class ThucDonServiceImpl implements ThucDonService {

    private static final Logger log = LoggerFactory.getLogger(ThucDonServiceImpl.class);

    private final ThucDonRepository thucDonRepository;

    /**
     * Creates ThucDonServiceImpl.
     *
     * @param thucDonRepository thucDonRepository
     */
    public ThucDonServiceImpl(ThucDonRepository thucDonRepository) {
        this.thucDonRepository = thucDonRepository;
    }

    /**
     * Find all.
     *
     * @return result
     */
    @Override
    public List<ThucDon> findAll() {
        return thucDonRepository.findAll();
    }
    
    /**
     * Create.
     *
     * @param tenMon tenMon
     * @param giaTien giaTien
     */
    @Override
    @CacheEvict(cacheNames = "menu", allEntries = true)
    public void create(String tenMon, BigDecimal giaTien) {
        if (tenMon == null || tenMon.isBlank() || giaTien == null) {
            throw new IllegalArgumentException("Chưa nhập các trường bắt buộc");
        }

        thucDonRepository.findByTenMonIgnoreCase(tenMon)
                .ifPresent(m -> {
                    throw new IllegalArgumentException("Tên món đã tồn tại");
                });

        ThucDon thucDon = new ThucDon();
        thucDon.setTenMon(tenMon);
        thucDon.setGiaHienTai(giaTien);

        ThucDon saved = thucDonRepository.save(thucDon);
        log.info("Created thucDon id={} tenMon={}", saved.getMaThucDon(), saved.getTenMon());
    }
    
    /**
     * Update.
     *
     * @param id id
     * @param tenMon tenMon
     * @param giaTien giaTien
     */
    @Override
    @CacheEvict(cacheNames = "menu", allEntries = true)
    public void update(long id, String tenMon, BigDecimal giaTien) {
        if (tenMon == null || tenMon.isBlank() || giaTien == null) {
            throw new IllegalArgumentException("Chưa nhập các trường bắt buộc");
        }

        ThucDon thucDon = thucDonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món"));

        thucDonRepository.findByTenMonIgnoreCase(tenMon)
                .ifPresent(m -> {
                    if (!m.getMaThucDon().equals(id)) {
                        throw new IllegalArgumentException("Tên món đã tồn tại");
                    }
                });

        thucDon.setTenMon(tenMon);
        thucDon.setGiaHienTai(giaTien);
        ThucDon saved = thucDonRepository.save(thucDon);
        log.info("Updated thucDon id={} tenMon={}", saved.getMaThucDon(), saved.getTenMon());
    }

    /**
     * Find by id.
     *
     * @param id id
     * @return result
     */
    @Override
    public Optional<ThucDon> findById(long id) {
        return thucDonRepository.findById(id);
    }

    /**
     * Delete by id.
     *
     * @param id id
     */
    @Override
    @CacheEvict(cacheNames = "menu", allEntries = true)
    public void deleteById(long id) {
        ThucDon thucDon = thucDonRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy món"));
        thucDonRepository.delete(thucDon);
        log.info("Deleted thucDon id={}", id);
    }

    /**
     * Search by ten mon.
     *
     * @param keyword keyword
     * @return result
     */
    @Override
    public List<ThucDon> searchByTenMon(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return thucDonRepository.findAll();
        }
        return thucDonRepository.findByTenMonContainingIgnoreCase(keyword.trim());
    }
}
