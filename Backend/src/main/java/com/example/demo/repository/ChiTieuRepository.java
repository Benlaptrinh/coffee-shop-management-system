package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.entity.ChiTieu;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ChiTieuRepository
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
public interface ChiTieuRepository extends JpaRepository<ChiTieu, Long> {
    /**
     * Find by ngay chi between.
     *
     * @param from from
     * @param to to
     * @return result
     */
    List<ChiTieu> findByNgayChiBetween(LocalDate from, LocalDate to);
}


