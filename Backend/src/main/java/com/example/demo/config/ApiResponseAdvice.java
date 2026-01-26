package com.example.demo.config;

import com.example.demo.exception.ApiError;
import com.example.demo.payload.response.ApiResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.time.Instant;

/**
 * Wraps successful responses in ApiResponse.
 */
@RestControllerAdvice(basePackages = {
        "com.example.demo.controller",
        "com.example.demo.report.controller"
})
public class ApiResponseAdvice implements ResponseBodyAdvice<Object> {

    private final ObjectMapper objectMapper;

    public ApiResponseAdvice(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean supports(@NonNull MethodParameter returnType,
                            @NonNull Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(@Nullable Object body,
                                  @NonNull MethodParameter returnType,
                                  @NonNull MediaType selectedContentType,
                                  @NonNull Class<? extends HttpMessageConverter<?>> selectedConverterType,
                                  @NonNull ServerHttpRequest request,
                                  @NonNull ServerHttpResponse response) {
        if (body instanceof ApiError || body instanceof ApiResponse) {
            return body;
        }

        int statusValue = HttpStatus.OK.value();
        if (response instanceof ServletServerHttpResponse servletResponse) {
            statusValue = servletResponse.getServletResponse().getStatus();
        }
        if (statusValue == HttpStatus.NO_CONTENT.value()) {
            return body;
        }

        String path = null;
        if (request instanceof ServletServerHttpRequest servletRequest) {
            path = servletRequest.getServletRequest().getRequestURI();
        } else if (request != null) {
            path = request.getURI().getPath();
        }

        HttpStatus resolved = HttpStatus.resolve(statusValue);
        String message = resolved != null ? resolved.getReasonPhrase() : HttpStatus.OK.getReasonPhrase();
        ApiResponse<Object> wrapper = new ApiResponse<>(Instant.now(), statusValue, message, path, body);

        if (StringHttpMessageConverter.class.isAssignableFrom(selectedConverterType)) {
            try {
                return objectMapper.writeValueAsString(wrapper);
            } catch (JsonProcessingException ex) {
                throw new IllegalStateException("Failed to serialize ApiResponse", ex);
            }
        }

        return wrapper;
    }
}
