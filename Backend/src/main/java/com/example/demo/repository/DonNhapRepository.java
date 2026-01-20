package com.example.demo.repository;

import java.time.LocalDateTime;

import com.example.demo.entity.DonNhap;
import com.example.demo.entity.HangHoa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * DonNhapRepository
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
public interface DonNhapRepository extends JpaRepository<DonNhap, Long> {

    /**
     * Max.
     *
     * @param ngayNhap ngayNhap
     * @return result
     */
    @Query("""
        select max(d.ngayNhap)
        from DonNhap d
        where d.hangHoa.maHangHoa = :id
    """)
    LocalDateTime findNgayNhapGanNhat(@Param("id") Long hangHoaId);
    /**
     * Exists by hang hoa.
     *
     * @param hangHoa hangHoa
     * @return result
     */
    boolean existsByHangHoa(HangHoa hangHoa);
}


