package com.example.demo.security;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import com.example.demo.entity.TaiKhoan;

import lombok.Getter;

/**
 * Custom OAuth2 User that wraps TaiKhoan entity.
 */
@Getter
public class CustomOAuth2User implements OAuth2User {

    private final Map<String, Object> attributes;
    private final Collection<? extends GrantedAuthority> authorities;
    private final String nameAttributeKey;
    private final String password;
    private final TaiKhoan taiKhoan;
    private final boolean accountNonExpired;
    private final boolean accountNonLocked;
    private final boolean credentialsNonExpired;
    private final boolean enabled;

    public CustomOAuth2User(Map<String, Object> attributes,
                           Collection<? extends GrantedAuthority> authorities,
                           String nameAttributeKey,
                           String password,
                           TaiKhoan taiKhoan,
                           boolean enabled) {
        this.attributes = attributes;
        this.authorities = authorities;
        this.nameAttributeKey = nameAttributeKey;
        this.password = password;
        this.taiKhoan = taiKhoan;
        this.accountNonExpired = true;
        this.accountNonLocked = true;
        this.credentialsNonExpired = true;
        this.enabled = enabled;
    }

    @Override
    public String getName() {
        return this.getAttribute(nameAttributeKey);
    }

    public String getUsername() {
        return taiKhoan != null ? taiKhoan.getTenDangNhap() : null;
    }

    public List<String> getRoles() {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .map(auth -> auth.replace("ROLE_", ""))
                .collect(Collectors.toList());
    }
}
