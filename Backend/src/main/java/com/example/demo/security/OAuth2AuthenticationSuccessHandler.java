package com.example.demo.security;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.oidc.user.DefaultOidcUser;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.example.demo.config.JwtProperties;
import com.example.demo.security.JwtUtil;
import com.example.demo.repository.TaiKhoanRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 Authentication Success Handler that generates JWT token.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2AuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;
    private final TaiKhoanRepository taiKhoanRepository;

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        
        OAuth2User oauth2User = (OAuth2User) authentication.getPrincipal();
        String username;
        List<String> roles;
        
        // Check if it's CustomOAuth2User or DefaultOidcUser
        if (oauth2User instanceof CustomOAuth2User) {
            CustomOAuth2User customUser = (CustomOAuth2User) oauth2User;
            username = customUser.getUsername();
            roles = customUser.getRoles();
            log.info("OAuth2 login with CustomOAuth2User: {}", username);
        } else {
            // Handle DefaultOidcUser (directly from Spring Security)
            username = extractUsername(oauth2User);
            roles = List.of("NHANVIEN"); // Default role for OAuth users
            
            // Create or update TaiKhoan in database
            createOrUpdateTaiKhoan(oauth2User, username);
            
            log.info("OAuth2 login with DefaultOidcUser: {}", username);
        }
        
        // Generate JWT
        String token = jwtUtil.generateToken(username, roles);
        
        // Redirect to frontend with token
        String redirectUrl = frontendUrl + "/oauth2/callback?token=" + token
                + "&username=" + username
                + "&roles=" + String.join(",", roles);
        
        log.info("Redirecting to: {}", redirectUrl);
        response.sendRedirect(redirectUrl);
    }
    
    private String extractUsername(OAuth2User oauth2User) {
        // Try different attributes to get username
        String email = oauth2User.getAttribute("email");
        if (email != null) {
            return email;
        }
        
        String name = oauth2User.getAttribute("name");
        if (name != null) {
            return name.replaceAll("\\s+", "_").toLowerCase();
        }
        
        String sub = oauth2User.getAttribute("sub");
        if (sub != null) {
            return sub;
        }
        
        String login = oauth2User.getAttribute("login");
        if (login != null) {
            return login;
        }
        
        return "oauth2_user";
    }
    
    private void createOrUpdateTaiKhoan(OAuth2User oauth2User, String username) {
        try {
            taiKhoanRepository.findByTenDangNhap(username).ifPresentOrElse(
                existing -> {
                    log.info("TaiKhoan already exists: {}", username);
                },
                () -> {
                    // Create new TaiKhoan for OAuth2 user
                    com.example.demo.entity.TaiKhoan newAccount = new com.example.demo.entity.TaiKhoan();
                    newAccount.setTenDangNhap(username);
                    newAccount.setMatKhau("{bcrypt}$2a$10$dXJ3SW6G7P50lGmMkkmwe.20cQQubK3.HZWzG3YB1tlRy.fqvM/BG"); // password: password
                    newAccount.setQuyenHan(com.example.demo.enums.Role.NHANVIEN);
                    newAccount.setEnabled(true);
                    newAccount.setAnh(null);
                    taiKhoanRepository.save(newAccount);
                    log.info("Created new TaiKhoan for OAuth2 user: {}", username);
                }
            );
        } catch (Exception e) {
            log.error("Error creating/updating TaiKhoan: {}", e.getMessage());
        }
    }
}
