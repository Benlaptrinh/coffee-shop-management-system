package com.example.demo.payload.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;

/**
 * Item payload for Split.
 */
@Getter
@Setter
public class SplitItem {
    @NotNull(message = "Mã thực đơn bắt buộc")
    private Long thucDonId;

    @NotNull(message = "Số lượng bắt buộc")
    @Positive(message = "Số lượng phải lớn hơn 0")
    private Integer soLuong;

    // Backward compatibility for older payloads that included fromBanId per item.
    private Long fromBanId;
}
