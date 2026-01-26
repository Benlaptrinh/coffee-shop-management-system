package com.example.demo.payload.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * Request payload for merging tables.
 */
@Getter
@Setter
public class MergeTableRequest {
    @NotNull(message = "Bàn đích bắt buộc")
    private Long targetBanId;

    @NotNull(message = "Bàn nguồn bắt buộc")
    private Long sourceBanId;
}
