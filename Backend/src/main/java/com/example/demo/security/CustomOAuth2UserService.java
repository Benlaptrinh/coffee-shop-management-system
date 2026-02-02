package com.example.demo.security;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.lang.NonNull;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.example.demo.entity.TaiKhoan;
import com.example.demo.enums.Role;
import com.example.demo.repository.TaiKhoanRepository;

/**
 * Custom OAuth2 User Service that handles user info from OAuth2 providers.
 */
@Service
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final TaiKhoanRepository taiKhoanRepository;

    public CustomOAuth2UserService(TaiKhoanRepository taiKhoanRepository) {
        this.taiKhoanRepository = taiKhoanRepository;
    }

    @Override
    public OAuth2User loadUser(@NonNull OAuth2UserRequest userRequest) {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        
        String registrationId = userRequest.getClientRegistration().getRegistrationId();
        String username = generateUsername(oAuth2User, registrationId);
        String email = getEmail(oAuth2User, registrationId);
        String avatar = getAvatar(oAuth2User, registrationId);
        
        // Find or create user
        TaiKhoan taiKhoan = taiKhoanRepository.findByTenDangNhap(username)
                .orElseGet(() -> createNewUser(username, email, avatar, registrationId));
        
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + taiKhoan.getQuyenHan().name()));
        
        return new CustomOAuth2User(
                oAuth2User.getAttributes(),
                authorities,
                "username",
                passwordFromOAuth2Token(userRequest.getAccessToken()),
                taiKhoan,
                true
        );
    }

    private TaiKhoan createNewUser(String username, String email, String avatar, String provider) {
        TaiKhoan taiKhoan = new TaiKhoan();
        taiKhoan.setTenDangNhap(username);
        taiKhoan.setMatKhau(passwordFromOAuth2Token(null)); // Placeholder
        taiKhoan.setQuyenHan(Role.NHANVIEN); // Default role for OAuth users
        taiKhoan.setAnh(avatar);
        taiKhoan.setEnabled(true);
        
        // Note: You might want to save NhanVien as well
        
        return taiKhoanRepository.save(taiKhoan);
    }

    private String generateUsername(OAuth2User oAuth2User, String registrationId) {
        return switch (registrationId) {
            case "google" -> "google_" + oAuth2User.getAttribute("sub");
            case "github" -> "github_" + oAuth2User.getAttribute("id");
            default -> registrationId + "_" + UUID.randomUUID().toString().substring(0, 8);
        };
    }

    private String getEmail(OAuth2User oAuth2User, String registrationId) {
        return switch (registrationId) {
            case "google" -> oAuth2User.getAttribute("email");
            case "github" -> oAuth2User.getAttribute("email");
            default -> null;
        };
    }

    private String getAvatar(OAuth2User oAuth2User, String registrationId) {
        return switch (registrationId) {
            case "google" -> oAuth2User.getAttribute("picture");
            case "github" -> oAuth2User.getAttribute("avatar_url");
            default -> null;
        };
    }

    private String passwordFromOAuth2Token(OAuth2AccessToken accessToken) {
        // Generate a secure random password for OAuth users
        // They won't use password login anyway
        return "{bcrypt}" + UUID.randomUUID().toString().replace("-", "");
    }
}

