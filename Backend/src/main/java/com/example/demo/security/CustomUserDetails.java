package com.example.demo.security;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

import com.example.demo.entity.TaiKhoan;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * CustomUserDetails
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
public class CustomUserDetails implements UserDetails {

    private final TaiKhoan taiKhoan;
    private final boolean nhanVienEnabled;

    /**
     * Creates CustomUserDetails.
     *
     * @param taiKhoan taiKhoan
     */
    public CustomUserDetails(TaiKhoan taiKhoan, boolean nhanVienEnabled) {
        this.taiKhoan = taiKhoan;
        this.nhanVienEnabled = nhanVienEnabled;
    }

    /**
     * Get authorities.
     *
     * @return result
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        if (taiKhoan.getQuyenHan() == null) return List.of();
        return List.of(new SimpleGrantedAuthority("ROLE_" + taiKhoan.getQuyenHan().name()));
    }

    /**
     * Get password.
     *
     * @return result
     */
    @Override
    public String getPassword() {
        return taiKhoan.getMatKhau();
    }

    /**
     * Get username.
     *
     * @return result
     */
    @Override
    public String getUsername() {
        return taiKhoan.getTenDangNhap();
    }

    /**
     * Is account non expired.
     *
     * @return result
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Is account non locked.
     *
     * @return result
     */
    @Override
    public boolean isAccountNonLocked() {
        return taiKhoan.isEnabled() && nhanVienEnabled;
    }

    /**
     * Is credentials non expired.
     *
     * @return result
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Is enabled.
     *
     * @return result
     */
    @Override
    public boolean isEnabled() {
        return taiKhoan.isEnabled() && nhanVienEnabled;
    }

    /**
     * Get tai khoan.
     *
     * @return result
     */
    public TaiKhoan getTaiKhoan() {
        return taiKhoan;
    }

    /**
     * Equals.
     *
     * @param o o
     * @return result
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomUserDetails)) return false;
        CustomUserDetails that = (CustomUserDetails) o;
        return Objects.equals(getUsername(), that.getUsername());
    }

    /**
     * Hash code.
     *
     * @return result
     */
    @Override
    public int hashCode() {
        return Objects.hash(getUsername());
    }
}

