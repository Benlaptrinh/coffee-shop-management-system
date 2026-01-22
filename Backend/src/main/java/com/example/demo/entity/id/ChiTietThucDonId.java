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
 * JPA entity for Chi Tiet Thuc Don Id.
 */
@Embeddable
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class ChiTietThucDonId implements Serializable {

    @Column(name = "ma_thuc_don")
    private Long maThucDon;

    @Column(name = "ma_hang_hoa")
    private Long maHangHoa;
}

