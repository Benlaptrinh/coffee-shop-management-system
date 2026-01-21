package com.example.demo.dto;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MenuSelectionRequest {
    private Map<String, String> params;
}
