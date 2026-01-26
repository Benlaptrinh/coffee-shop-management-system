package com.example.demo.payload.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Getter;
import lombok.Setter;
/**
 * Request payload for Pay.
 */
@Getter
@Setter
public class PayRequest {
    @NotNull(message = "Số tiền khách đưa bắt buộc")
    @Positive(message = "Số tiền khách đưa phải lớn hơn 0")
    private BigDecimal amountPaid;
    private Boolean releaseTable = true;
}
