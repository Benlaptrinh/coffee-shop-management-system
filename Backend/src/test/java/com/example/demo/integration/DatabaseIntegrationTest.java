package com.example.demo.integration;

import com.example.demo.entity.TaiKhoan;
import com.example.demo.enums.Role;
import com.example.demo.repository.TaiKhoanRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.containers.MySQLContainer;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers(disabledWithoutDocker = true)
class DatabaseIntegrationTest {

    @Container
    static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("tiemchung")
            .withUsername("root")
            .withPassword("root");

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysql::getJdbcUrl);
        registry.add("spring.datasource.username", mysql::getUsername);
        registry.add("spring.datasource.password", mysql::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.cache.type", () -> "simple");
    }

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Test
    void saveAndFindUser() {
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("admin_tc");
        tk.setMatKhau("secret");
        tk.setQuyenHan(Role.ADMIN);
        tk.setEnabled(true);
        taiKhoanRepository.save(tk);

        assertThat(taiKhoanRepository.findByTenDangNhap("admin_tc")).isPresent();
    }
}
