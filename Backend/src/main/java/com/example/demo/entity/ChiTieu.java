package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ChiTieu
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
@Entity
@Table(name = "chi_tieu")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChiTieu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maChiTieu;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_tai_khoan")
    private TaiKhoan taiKhoan;

    @Column(nullable = false)
    private String tenKhoanChi;

    @Column(precision = 13, scale = 2)
    private BigDecimal soTien;

    private LocalDate ngayChi;
}
