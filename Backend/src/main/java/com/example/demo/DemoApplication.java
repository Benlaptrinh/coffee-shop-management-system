package com.example.demo;

import com.example.demo.entity.TaiKhoan;
import com.example.demo.enums.Role;
import com.example.demo.repository.TaiKhoanRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.Bean;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot application entry point.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
@EnableCaching
public class DemoApplication {

    private static final Logger log = LoggerFactory.getLogger(DemoApplication.class);

    /**
     * Main.
     *
     * @param args args
     */
    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    /**
     * Seed admin.
     *
     * @param taiKhoanRepository taiKhoanRepository
     * @param passwordEncoder passwordEncoder
     * @return result
     */
    @Bean
    @ConditionalOnBean({TaiKhoanRepository.class, PasswordEncoder.class})
    public CommandLineRunner seedAdmin(TaiKhoanRepository taiKhoanRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            boolean adminExists = taiKhoanRepository.findAll()
                    .stream()
                    .anyMatch(t -> t.getQuyenHan() == Role.ADMIN);
            if (!adminExists) {
                TaiKhoan admin = new TaiKhoan();
                admin.setTenDangNhap("admin");
                admin.setMatKhau(passwordEncoder.encode("admin123"));
                admin.setQuyenHan(Role.ADMIN);
                admin.setEnabled(true);
                taiKhoanRepository.save(admin);
                log.info("Seeded default admin account");
            }
        };
    }
}
