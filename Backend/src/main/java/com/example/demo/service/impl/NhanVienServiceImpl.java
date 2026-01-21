package com.example.demo.service.impl;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.NhanVien;
import com.example.demo.repository.NhanVienRepository;
import com.example.demo.repository.TaiKhoanRepository;
import com.example.demo.service.NhanVienService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * NhanVienServiceImpl
 *
 * Version 1.0
 *
 * Date: 09-01-2026
 *
 * Copyright
 *
 * Modification Logs:
 * DATE        AUTHOR      DESCRIPTION
 * -----------------------------------
 * 09-01-2026  Việt    Create
 */
@Service
public class NhanVienServiceImpl implements NhanVienService {

    private static final Logger log = LoggerFactory.getLogger(NhanVienServiceImpl.class);

    private final NhanVienRepository nhanVienRepository;
    private final TaiKhoanRepository taiKhoanRepository;

    /**
     * Creates NhanVienServiceImpl.
     *
     * @param nhanVienRepository nhanVienRepository
     * @param taiKhoanRepository taiKhoanRepository
     */
    public NhanVienServiceImpl(NhanVienRepository nhanVienRepository, TaiKhoanRepository taiKhoanRepository) {
        this.nhanVienRepository = nhanVienRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    

    /**
     * Find all.
     *
     * @return result
     */
    @Override
    public List<NhanVien> findAll() {
        return nhanVienRepository.findAll();
    }

    /**
     * Find by id.
     *
     * @param id id
     * @return result
     */
    @Override
    public Optional<NhanVien> findById(long id) {
        return nhanVienRepository.findById(id);
    }

    /**
     * Find by tai khoan id.
     *
     * @param maTaiKhoan maTaiKhoan
     * @return result
     */
    @Override
    public Optional<NhanVien> findByTaiKhoanId(long maTaiKhoan) {
        return nhanVienRepository.findByTaiKhoan_MaTaiKhoan(maTaiKhoan);
    }

    /**
     * Find by ho ten containing.
     *
     * @param keyword keyword
     * @return result
     */
    @Override
    public List<NhanVien> findByHoTenContaining(String keyword) {
        return nhanVienRepository.findByHoTenContainingIgnoreCase(keyword);
    }

    /**
     * Save.
     *
     * @param nhanVien nhanVien
     * @return result
     */
    @Override
    public NhanVien save(NhanVien nhanVien) {
        if (nhanVien.getChucVu() != null && nhanVien.getChucVu().getLuong() != null) {
            nhanVien.setLuong(nhanVien.getChucVu().getLuong());
        }
        NhanVien saved = nhanVienRepository.save(nhanVien);
        Long taiKhoanId = saved.getTaiKhoan() == null ? null : saved.getTaiKhoan().getMaTaiKhoan();
        log.info("Saved nhanVien id={} taiKhoanId={}", saved.getMaNhanVien(), taiKhoanId);
        return saved;
    }

    /**
     * Delete by tai khoan id.
     *
     * @param maTaiKhoan maTaiKhoan
     */
    @Override
    /**
     * Delete by tai khoan id.
     *
     * @param maTaiKhoan maTaiKhoan
     */
    @Transactional
    public void deleteByTaiKhoanId(long maTaiKhoan) {
        Optional<NhanVien> target = nhanVienRepository.findByTaiKhoan_MaTaiKhoan(maTaiKhoan);
        if (target.isEmpty()) {
            log.warn("NhanVien not found for taiKhoanId={}", maTaiKhoan);
            return;
        }
        NhanVien nv = target.get();
        if (nv.getTaiKhoan() != null) {
            taiKhoanRepository.delete(nv.getTaiKhoan());
        }
        nhanVienRepository.delete(nv);
        log.info("Deleted nhanVien id={} for taiKhoanId={}", nv.getMaNhanVien(), maTaiKhoan);
    }

    /**
     * Delete by id.
     *
     * @param id id
     */
    @Override
    /**
     * Delete by id.
     *
     * @param id id
     */
    @Transactional
    public void deleteById(long id) {
        Optional<NhanVien> target = nhanVienRepository.findById(id);
        if (target.isEmpty()) {
            log.warn("NhanVien not found id={}", id);
            return;
        }
        NhanVien nv = target.get();
        if (nv.getTaiKhoan() != null) {
            taiKhoanRepository.delete(nv.getTaiKhoan());
        }
        nhanVienRepository.delete(nv);
        log.info("Deleted nhanVien id={}", id);
    }
}
