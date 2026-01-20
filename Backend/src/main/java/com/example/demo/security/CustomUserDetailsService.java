package com.example.demo.security;

import com.example.demo.entity.NhanVien;
import com.example.demo.entity.TaiKhoan;
import com.example.demo.repository.NhanVienRepository;
import com.example.demo.repository.TaiKhoanRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * CustomUserDetailsService
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
public class CustomUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final NhanVienRepository nhanVienRepository;

    /**
     * Creates CustomUserDetailsService.
     *
     * @param taiKhoanRepository taiKhoanRepository
     */
    public CustomUserDetailsService(TaiKhoanRepository taiKhoanRepository,
                                    NhanVienRepository nhanVienRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
        this.nhanVienRepository = nhanVienRepository;
    }

    /**
     * Load user by username.
     *
     * @param username username
     * @return result
     * @throws UsernameNotFoundException if an error occurs
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        TaiKhoan tk = taiKhoanRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        boolean nhanVienEnabled = nhanVienRepository.findByTaiKhoan_MaTaiKhoan(tk.getMaTaiKhoan())
                .map(NhanVien::getEnabled)
                .orElse(true);
        return new CustomUserDetails(tk, nhanVienEnabled);
    }
}
