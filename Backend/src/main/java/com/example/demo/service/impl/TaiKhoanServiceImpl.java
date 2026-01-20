package com.example.demo.service.impl;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.TaiKhoan;
import com.example.demo.repository.TaiKhoanRepository;
import com.example.demo.service.TaiKhoanService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * TaiKhoanServiceImpl
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
public class TaiKhoanServiceImpl implements TaiKhoanService {

    private static final Logger log = LoggerFactory.getLogger(TaiKhoanServiceImpl.class);

    private final TaiKhoanRepository taiKhoanRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates TaiKhoanServiceImpl.
     *
     * @param taiKhoanRepository taiKhoanRepository
     * @param passwordEncoder passwordEncoder
     */
    public TaiKhoanServiceImpl(TaiKhoanRepository taiKhoanRepository,
                              PasswordEncoder passwordEncoder) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Save.
     *
     * @param taiKhoan taiKhoan
     * @return result
     */
    @Override
    public TaiKhoan save(TaiKhoan taiKhoan) {
        
        if (taiKhoan.getTenDangNhap() != null && !taiKhoan.getTenDangNhap().isEmpty()) {
            taiKhoanRepository.findByTenDangNhap(taiKhoan.getTenDangNhap())
                    .ifPresent(existing -> {
                        
                        if (taiKhoan.getMaTaiKhoan() == null || !existing.getMaTaiKhoan().equals(taiKhoan.getMaTaiKhoan())) {
                            throw new IllegalArgumentException("Tên đăng nhập đã tồn tại");
                        }
                    });
        }

        
        if (taiKhoan.getMaTaiKhoan() == null) {
            if (taiKhoan.getMatKhau() == null || taiKhoan.getMatKhau().isBlank()) {
                throw new IllegalArgumentException("Mật khẩu bắt buộc khi tạo tài khoản");
            }
        }

        if (taiKhoan.getMatKhau() != null && !taiKhoan.getMatKhau().isEmpty()) {
            
            if (!taiKhoan.getMatKhau().startsWith("$2a$") && !taiKhoan.getMatKhau().startsWith("$2b$")) {
                taiKhoan.setMatKhau(passwordEncoder.encode(taiKhoan.getMatKhau()));
            }
        }

        TaiKhoan saved = taiKhoanRepository.save(taiKhoan);
        log.info("Saved taiKhoan id={} username={}", saved.getMaTaiKhoan(), saved.getTenDangNhap());
        return saved;
    }

    /**
     * Find all.
     *
     * @return result
     */
    @Override
    public List<TaiKhoan> findAll() {
        return taiKhoanRepository.findAll();
    }

    /**
     * Find by id.
     *
     * @param id id
     * @return result
     */
    @Override
    public Optional<TaiKhoan> findById(Long id) {
        return taiKhoanRepository.findById(id);
    }

    /**
     * Find by username.
     *
     * @param username username
     * @return result
     */
    @Override
    public Optional<TaiKhoan> findByUsername(String username) {
        return taiKhoanRepository.findByTenDangNhap(username);
    }

    /**
     * Disable.
     *
     * @param id id
     */
    @Override
    public void disable(Long id) {
        Optional<TaiKhoan> target = taiKhoanRepository.findById(id);
        if (target.isEmpty()) {
            log.warn("TaiKhoan not found id={}", id);
            return;
        }
        TaiKhoan taiKhoan = target.get();
        taiKhoan.setEnabled(false);
        taiKhoanRepository.save(taiKhoan);
        log.info("Disabled taiKhoan id={} username={}", taiKhoan.getMaTaiKhoan(), taiKhoan.getTenDangNhap());
    }
}
