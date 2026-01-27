package com.example.demo.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.exception.ApiError;
import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Configuration for Security.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /**
     * Password encoder.
     *
     * @return result
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Filter chain.
     *
     * @param http http
     * @param jwtAuthenticationFilter jwt authentication filter
     * @return result
     * @throws Exception if an error occurs
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http,
                                           JwtAuthenticationFilter jwtAuthenticationFilter,
                                           ObjectMapper objectMapper) throws Exception {
        return http
            .cors(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) ->
                        writeError(response, objectMapper, HttpStatus.UNAUTHORIZED,
                                authException.getMessage(), request))
                .accessDeniedHandler((request, response, accessDeniedException) ->
                        writeError(response, objectMapper, HttpStatus.FORBIDDEN,
                                accessDeniedException.getMessage(), request))
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/ws/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                .requestMatchers("/api/report/**").hasRole("ADMIN")
                .requestMatchers("/api/khuyenmai/**").hasRole("ADMIN")
                .requestMatchers("/api/chucvu/**").hasRole("ADMIN")
                .requestMatchers("/api/users/me").authenticated()
                .requestMatchers("/api/users/me/**").authenticated()
                .requestMatchers("/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/nhanvien/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/nhanvien/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/nhanvien/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/thucdon/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/thucdon/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/thucdon/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/thietbi/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/thietbi/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/thietbi/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/donvitinh/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/donvitinh/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, "/api/hanghoa/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/hanghoa/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/hanghoa/**").hasRole("ADMIN")
                .requestMatchers("/api/**").authenticated()
                .anyRequest().denyAll()
            )
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
            .build();
    }

    /**
     * Authentication manager (exposed for controllers).
     * @param configuration configuration
     * @return result
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private void writeError(HttpServletResponse response,
                            ObjectMapper objectMapper,
                            HttpStatus status,
                            String message,
                            HttpServletRequest request) throws java.io.IOException {
        ApiError err = new ApiError();
        err.setStatus(status.value());
        err.setError(status.getReasonPhrase());
        err.setMessage(message);
        err.setPath(request.getRequestURI());
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), err);
    }
}
