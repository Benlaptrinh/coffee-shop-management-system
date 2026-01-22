package com.example.demo.payload.request;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
/**
 * Request payload for Menu Selection.
 */
@Getter
@Setter
public class MenuSelectionRequest {
    private Map<String, String> params;
}
