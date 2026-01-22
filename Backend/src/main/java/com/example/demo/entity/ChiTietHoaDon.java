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
 * ChiTietHoaDon
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
@Table(name = "chi_tiet_hoa_don")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChiTietHoaDon {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private com.example.demo.entity.id.ChiTietHoaDonId id = new com.example.demo.entity.id.ChiTietHoaDonId();

    @MapsId("maHoaDon")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ma_hoa_don", nullable = false)
    private HoaDon hoaDon;

    @MapsId("maThucDon")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ma_thuc_don", nullable = false)
    private ThucDon thucDon;

    private Integer soLuong;

    @Column(precision = 13, scale = 2)
    private BigDecimal giaTaiThoiDiemBan;

    @Column(precision = 13, scale = 2)
    private BigDecimal thanhTien;

    @PrePersist
    @PreUpdate
    private void syncId() {
        if (this.id == null) this.id = new com.example.demo.entity.id.ChiTietHoaDonId();
        this.id.setMaHoaDon(this.hoaDon != null ? this.hoaDon.getMaHoaDon() : null);
        this.id.setMaThucDon(this.thucDon != null ? this.thucDon.getMaThucDon() : null);
    }

    public void setHoaDon(HoaDon hoaDon) {
        this.hoaDon = hoaDon;
        if (this.id == null) this.id = new com.example.demo.entity.id.ChiTietHoaDonId();
        this.id.setMaHoaDon(hoaDon != null ? hoaDon.getMaHoaDon() : null);
    }

    public void setThucDon(ThucDon thucDon) {
        this.thucDon = thucDon;
        if (this.id == null) this.id = new com.example.demo.entity.id.ChiTietHoaDonId();
        this.id.setMaThucDon(thucDon != null ? thucDon.getMaThucDon() : null);
    }
}
