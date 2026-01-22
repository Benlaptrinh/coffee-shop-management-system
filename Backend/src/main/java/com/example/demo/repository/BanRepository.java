package com.example.demo.repository;

import com.example.demo.entity.Ban;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Ban entities.
 */
public interface BanRepository extends JpaRepository<Ban, Long> {
}


