package com.example.demo.repository;

import com.example.demo.entity.ChiTietHoaDon;
import com.example.demo.entity.id.ChiTietHoaDonId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * ChiTietHoaDonRepository
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
public interface ChiTietHoaDonRepository extends JpaRepository<ChiTietHoaDon, ChiTietHoaDonId> {
    /**
     * Delete by hoa don id.
     *
     * @param hoaDonId hoaDonId
     */
    @Modifying
    @Transactional
    @Query("DELETE FROM ChiTietHoaDon c WHERE c.hoaDon.maHoaDon = :hoaDonId")
    void deleteByHoaDonId(Long hoaDonId);
}

