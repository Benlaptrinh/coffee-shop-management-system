package com.example.demo.config;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * CORS settings for API.
 */
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "app.cors")
public class CorsProperties {
    private String pathPattern = "/api/**";
    private List<String> allowedOrigins = new ArrayList<>();
    private List<String> allowedMethods = new ArrayList<>();
    private List<String> allowedHeaders = new ArrayList<>();
    private boolean allowCredentials = true;
}
