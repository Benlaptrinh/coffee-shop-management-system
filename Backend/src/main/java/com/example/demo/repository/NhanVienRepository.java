package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.NhanVien;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for Nhan Vien entities.
 */
public interface NhanVienRepository extends JpaRepository<NhanVien, Long> {
    /**
     * Find by tai khoan ma tai khoan.
     *
     * @param maTaiKhoan maTaiKhoan
     * @return result
     */
    Optional<NhanVien> findByTaiKhoan_MaTaiKhoan(Long maTaiKhoan);
    /**
     * Find by ho ten containing ignore case.
     *
     * @param keyword keyword
     * @return result
     */
    List<NhanVien> findByHoTenContainingIgnoreCase(String keyword);

    /**
     * Count.
     *
     * @param nv nv
     * @return result
     */
    @Query("""
        SELECT nv.enabled, COUNT(nv)
        FROM NhanVien nv
        GROUP BY nv.enabled
    """)
    List<Object[]> thongKeNhanVienRaw();
    
    
    
}


