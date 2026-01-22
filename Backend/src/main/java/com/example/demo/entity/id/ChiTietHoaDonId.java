package com.example.demo.entity.id;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA entity for Chi Tiet Hoa Don Id.
 */
@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChiTietHoaDonId implements Serializable {

    @Column(name = "ma_hoa_don")
    private Long maHoaDon;

    @Column(name = "ma_thuc_don")
    private Long maThucDon;
}

