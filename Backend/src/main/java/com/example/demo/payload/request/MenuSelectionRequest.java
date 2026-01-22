package com.example.demo.payload.request;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuSelectionRequest {
    private Map<String, String> params;
}
