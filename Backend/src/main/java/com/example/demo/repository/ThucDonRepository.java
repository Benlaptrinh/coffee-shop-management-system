package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.ThucDon;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Thuc Don entities.
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



