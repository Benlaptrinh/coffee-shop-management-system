package com.example.demo.entity;

import java.time.LocalDateTime;

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
 * ChiTietDatBan
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
@Table(name = "chi_tiet_dat_ban")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class ChiTietDatBan {

    @EmbeddedId
    @EqualsAndHashCode.Include
    private com.example.demo.entity.id.ChiTietDatBanId id;

    @MapsId("maBan")
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ma_ban", nullable = false)
    private Ban ban;

    private String tenKhach;

    private String sdt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ma_nhan_vien")
    private NhanVien nhanVien;

    /** expose ngày giờ đặt qua embedded id để code hiện tại ít đổi */
    public java.time.LocalDateTime getNgayGioDat() {
        return id == null ? null : id.getNgayGioDat();
    }

    public void setNgayGioDat(java.time.LocalDateTime ngayGioDat) {
        if (this.id == null) this.id = new com.example.demo.entity.id.ChiTietDatBanId();
        this.id.setNgayGioDat(ngayGioDat);
    }

    @PrePersist
    @PreUpdate
    private void syncId() {
        if (this.id == null) this.id = new com.example.demo.entity.id.ChiTietDatBanId();
        this.id.setMaBan(this.ban != null ? this.ban.getMaBan() : null);
        // ngayGioDat handled via setNgayGioDat
    }
}


