package com.example.demo.entity.id;

import java.io.Serializable;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Chi Tiet Dat Ban Id.
 */
@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChiTietDatBanId implements Serializable {

    @Column(name = "ma_ban")
    private Long maBan;

    @Column(name = "ngay_gio_dat")
    private LocalDateTime ngayGioDat;
}

