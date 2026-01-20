package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.ThucDon;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * ThucDonRepository
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
public interface ThucDonRepository extends JpaRepository<ThucDon, Long> {
    /**
     * Find by ten mon ignore case.
     *
     * @param tenMon tenMon
     * @return result
     */
    Optional<ThucDon> findByTenMonIgnoreCase(String tenMon);
    /**
     * Find by ten mon containing ignore case.
     *
     * @param tenMon tenMon
     * @return result
     */
    List<ThucDon> findByTenMonContainingIgnoreCase(String tenMon);
}



