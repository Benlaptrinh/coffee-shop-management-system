package com.example.demo.repository;

import com.example.demo.entity.TaiKhoan;
import com.example.demo.enums.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.TestPropertySource;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;MODE=MySQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.flyway.enabled=false"
})
class TaiKhoanRepositoryTest {

    @Autowired
    private TaiKhoanRepository taiKhoanRepository;

    @Test
    void findByTenDangNhap_returnsUser() {
        TaiKhoan tk = new TaiKhoan();
        tk.setTenDangNhap("admin");
        tk.setMatKhau("secret");
        tk.setQuyenHan(Role.ADMIN);
        tk.setEnabled(true);
        taiKhoanRepository.save(tk);

        Optional<TaiKhoan> found = taiKhoanRepository.findByTenDangNhap("admin");
        assertThat(found).isPresent();
        assertThat(found.get().getQuyenHan()).isEqualTo(Role.ADMIN);
    }
}
