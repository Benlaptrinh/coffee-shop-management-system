package com.example.demo.exception;

import java.time.Instant;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * ApiError
 *
 * Simple DTO for API error responses.
 */
@Getter
@Setter
@NoArgsConstructor
public class ApiError {
    private Instant timestamp = Instant.now();
    private int status;
    private String error;
    private String message;
    private String path;
    private List<ApiFieldError> errors;
    /**
     * Component for Api Field Error.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ApiFieldError {
        private String field;
        private String message;
    }
}
