package com.example.demo.payload.request;

import java.util.Map;

import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;
/**
 * Request payload for Menu Selection.
 */
@Getter
@Setter
public class MenuSelectionRequest {
    @NotEmpty(message = "Params bắt buộc")
    private Map<String, String> params;
}
