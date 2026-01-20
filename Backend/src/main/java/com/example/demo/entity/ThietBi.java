package com.example.demo.entity;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ThietBi
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
@Table(name = "thiet_bi")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class ThietBi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maThietBi;

    @Column(name = "ten", nullable = false)
    private String tenThietBi;

    @Column(name = "so_luong", nullable = false)
    private Integer soLuong;

    @Column(name = "don_gia_mua", precision = 13, scale = 2, nullable = false)
    private BigDecimal donGiaMua;

    @Column(name = "ngay_mua", nullable = false)
    private LocalDate ngayMua;

    @Column(name = "ghi_chu")
    private String ghiChu;

    @Transient
    public BigDecimal getTongGia() {
        if (donGiaMua == null || soLuong == null) {
            return BigDecimal.ZERO;
        }
        return donGiaMua.multiply(BigDecimal.valueOf(soLuong));
    }
}


