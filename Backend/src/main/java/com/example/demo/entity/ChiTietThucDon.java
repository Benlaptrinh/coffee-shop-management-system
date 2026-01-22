package com.example.demo.entity;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ChiTietThucDon
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
@Table(name = "chi_tiet_thuc_don")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChiTietThucDon {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private com.example.demo.entity.id.ChiTietThucDonId id;

    @MapsId("maThucDon")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ma_thuc_don", nullable = false)
    private ThucDon thucDon;

    @MapsId("maHangHoa")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ma_hang_hoa", nullable = false)
    private HangHoa hangHoa;

    @Column(precision = 13, scale = 3)
    private BigDecimal khoiLuong;

    @PrePersist
    @PreUpdate
    private void syncId() {
        if (this.id == null) this.id = new com.example.demo.entity.id.ChiTietThucDonId();
        this.id.setMaThucDon(this.thucDon != null ? this.thucDon.getMaThucDon() : null);
        this.id.setMaHangHoa(this.hangHoa != null ? this.hangHoa.getMaHangHoa() : null);
    }
}

