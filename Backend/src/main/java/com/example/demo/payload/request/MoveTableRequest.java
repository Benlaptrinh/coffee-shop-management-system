package com.example.demo.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for moving a table.
 */
@Getter
@Setter
public class MoveTableRequest {
    @NotNull(message = "Bàn nguồn bắt buộc")
    private Long fromBanId;

    @NotNull(message = "Bàn đích bắt buộc")
    private Long toBanId;
}
