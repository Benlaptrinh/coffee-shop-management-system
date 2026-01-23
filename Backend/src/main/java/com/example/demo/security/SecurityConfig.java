package com.example.demo.security;

import static org.springframework.security.config.Customizer.withDefaults;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                                           JwtAuthenticationFilter jwtAuthenticationFilter) throws Exception {
        return http
            .cors(withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
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

}
