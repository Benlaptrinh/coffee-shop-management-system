package com.example.demo.repository;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.entity.ChiTieu;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Chi Tieu entities.
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


