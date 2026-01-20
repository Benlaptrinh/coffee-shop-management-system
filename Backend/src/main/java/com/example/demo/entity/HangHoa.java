package com.example.demo.entity;

import java.math.BigDecimal;

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
 * HangHoa
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
@Table(name = "hang_hoa")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
public class HangHoa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long maHangHoa;

    private String tenHangHoa;

    private Integer soLuong;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_don_vi_tinh")
    private DonViTinh donViTinh;

    @Column(precision = 13, scale = 2)
    private BigDecimal donGia;
}


