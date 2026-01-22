package com.example.demo.repository;

import com.example.demo.entity.DonViTinh;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for Don Vi Tinh entities.
 */
public interface DonViTinhRepository extends JpaRepository<DonViTinh, Long> {
    Optional<DonViTinh> findByTenDonViIgnoreCase(String tenDonVi);
}

