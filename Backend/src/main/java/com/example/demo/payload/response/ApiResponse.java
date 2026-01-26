package com.example.demo.payload.response;

import java.time.Instant;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ApiResponse
 *
 * Standard success response envelope for REST APIs.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    private Instant timestamp = Instant.now();
    private int status;
    private String message;
    private String path;
    private T data;
}
