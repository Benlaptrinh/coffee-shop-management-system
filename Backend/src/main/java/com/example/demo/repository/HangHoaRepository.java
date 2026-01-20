package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.HangHoa;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * HangHoaRepository
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
public interface HangHoaRepository extends JpaRepository<HangHoa, Long> {
    /**
     * Find by ten hang hoa.
     *
     * @param tenHangHoa tenHangHoa
     * @return result
     */
    Optional<HangHoa> findByTenHangHoa(String tenHangHoa);
    /**
     * Find by ten hang hoa ignore case and don vi.
     *
     * @param tenHangHoa tenHangHoa
     * @param donViTinhId donViTinhId
     * @return result
     */
    Optional<HangHoa> findByTenHangHoaIgnoreCaseAndDonViTinh_MaDonViTinh(String tenHangHoa, Long donViTinhId);
    /**
     * Exists by ten hang hoa ignore case and don vi excluding id.
     *
     * @param tenHangHoa tenHangHoa
     * @param donViTinhId donViTinhId
     * @param maHangHoa maHangHoa
     * @return result
     */
    boolean existsByTenHangHoaIgnoreCaseAndDonViTinh_MaDonViTinhAndMaHangHoaNot(String tenHangHoa, Long donViTinhId, Long maHangHoa);
    /**
     * Find by ten hang hoa containing ignore case.
     *
     * @param keyword keyword
     * @return result
     */
    List<HangHoa> findByTenHangHoaContainingIgnoreCase(String keyword);
}

