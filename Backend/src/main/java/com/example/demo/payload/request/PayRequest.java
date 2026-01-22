package com.example.demo.payload.request;
import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayRequest {
    private BigDecimal amountPaid;
    private Boolean releaseTable = true;
}
