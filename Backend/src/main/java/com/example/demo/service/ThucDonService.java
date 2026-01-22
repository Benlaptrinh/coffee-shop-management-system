package com.example.demo.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.example.demo.entity.ThucDon;

/**
 * Service contract for Thuc Don.
 */
public interface ThucDonService {
    /**
     * Find all.
     *
     * @return result
     */
    List<ThucDon> findAll();
    /**
     * Create.
     *
     * @param tenMon tenMon
     * @param giaTien giaTien
     */
    void create(String tenMon, BigDecimal giaTien);
    /**
     * Update.
     *
     * @param id id
     * @param tenMon tenMon
     * @param giaTien giaTien
     */
    void update(long id, String tenMon, BigDecimal giaTien);
    /**
     * Find by id.
     *
     * @param id id
     * @return result
     */
    Optional<ThucDon> findById(long id);
    /**
     * Delete by id.
     *
     * @param id id
     */
    void deleteById(long id);
    /**
     * Search by ten mon.
     *
     * @param keyword keyword
     * @return result
     */
    List<ThucDon> searchByTenMon(String keyword);
}

