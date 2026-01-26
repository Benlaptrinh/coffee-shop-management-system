package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * WebConfig
 *
 * Global CORS configuration for API.
 */
@Configuration
public class WebConfig {
    private final CorsProperties corsProperties;

    public WebConfig(CorsProperties corsProperties) {
        this.corsProperties = corsProperties;
    }
    /**
     * Creates the CORS configurer.
     * @return CORS configurer
     */
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            /**
             * Adds CORS mappings.
             * @param registry CORS registry
             */
            @Override
            public void addCorsMappings(@NonNull CorsRegistry registry) {
                String[] origins = corsProperties.getAllowedOrigins().toArray(new String[0]);
                String[] methods = corsProperties.getAllowedMethods().toArray(new String[0]);
                String[] headers = corsProperties.getAllowedHeaders().toArray(new String[0]);
                registry.addMapping(corsProperties.getPathPattern())
                        .allowedOrigins(origins)
                        .allowedMethods(methods)
                        .allowedHeaders(headers)
                        .allowCredentials(corsProperties.isAllowCredentials());
            }
        };
    }
}
