package com.example.demo.security;

import com.example.demo.entity.NhanVien;
import com.example.demo.entity.TaiKhoan;
import com.example.demo.repository.NhanVienRepository;
import com.example.demo.repository.TaiKhoanRepository;
import org.springframework.lang.NonNull;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Service contract for Custom User Details.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final TaiKhoanRepository taiKhoanRepository;
    private final NhanVienRepository nhanVienRepository;

    /**
     * Creates CustomUserDetailsService.
     *
     * @param taiKhoanRepository taiKhoanRepository
     * @param nhanVienRepository nhan vien repository
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
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException {
        TaiKhoan tk = taiKhoanRepository.findByTenDangNhap(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        boolean nhanVienEnabled = nhanVienRepository.findByTaiKhoan_MaTaiKhoan(tk.getMaTaiKhoan())
                .map(NhanVien::getEnabled)
                .orElse(true);
        return new CustomUserDetails(tk, nhanVienEnabled);
    }
}
