package com.example.demo.payload.request;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SplitRequest {
    private Long toBanId;
    private List<Map<String, Object>> items;
}
