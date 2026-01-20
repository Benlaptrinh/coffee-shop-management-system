package com.example.demo.repository;

import com.example.demo.entity.Ban;
import com.example.demo.entity.ChiTietDatBan;
import com.example.demo.entity.id.ChiTietDatBanId;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * ChiTietDatBanRepository
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
public interface ChiTietDatBanRepository extends JpaRepository<ChiTietDatBan, ChiTietDatBanId> {
    Optional<ChiTietDatBan> findTopByBanOrderById_NgayGioDatDesc(Ban ban);
    Optional<ChiTietDatBan> findTopByBanAndId_NgayGioDatAfterOrderById_NgayGioDatAsc(Ban ban, LocalDateTime ngayGioDat);
    List<ChiTietDatBan> findByBanAndId_NgayGioDatBetween(Ban ban, LocalDateTime start, LocalDateTime end);
    boolean existsByBan(Ban ban);
    @Query("""
        select (count(c) > 0)
        from ChiTietDatBan c
        where c.ban = :ban
          and c.id.ngayGioDat > :start
          and c.id.ngayGioDat < :end
    """)
    boolean existsOverlappingReservation(@Param("ban") Ban ban,
                                         @Param("start") LocalDateTime start,
                                         @Param("end") LocalDateTime end);
    /**
     * Delete by ban.
     *
     * @param ban ban
     */
    void deleteByBan(Ban ban);
}
