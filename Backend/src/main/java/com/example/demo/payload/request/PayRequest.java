package com.example.demo.payload.request;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;
/**
 * Request payload for Pay.
 */
@Getter
@Setter
public class PayRequest {
    private BigDecimal amountPaid;
    private Boolean releaseTable = true;
}
