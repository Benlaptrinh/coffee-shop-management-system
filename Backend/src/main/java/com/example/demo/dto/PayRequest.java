package com.example.demo.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PayRequest {
    private BigDecimal amountPaid;
    private Boolean releaseTable = true;
}
