package com.example.demo.security;

import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * JwtUtil
 *
 * Minimal JWT utility implementation (HS256) without external libs.
 */
@Component
public class JwtUtil {

    @Value("${jwt.secret:changeit}")
    private String jwtSecret;

    @Value("${jwt.expiration.seconds:3600}")
    private long jwtExpirationSeconds;

    private static final String HMAC_ALGO = "HmacSHA256";

    public String generateToken(String username, List<String> roles) {
        try {
            String headerJson = "{\"alg\":\"HS256\",\"typ\":\"JWT\"}";
            long exp = Instant.now().getEpochSecond() + jwtExpirationSeconds;
            String payloadJson = String.format("{\"sub\":\"%s\",\"roles\":\"%s\",\"exp\":%d}",
                    escape(username), String.join(",", roles), exp);

            String header = base64UrlEncode(headerJson.getBytes(StandardCharsets.UTF_8));
            String payload = base64UrlEncode(payloadJson.getBytes(StandardCharsets.UTF_8));
            String signingInput = header + "." + payload;
            String signature = sign(signingInput, jwtSecret);
            return signingInput + "." + signature;
        } catch (Exception e) {
            throw new RuntimeException("Failed to generate token", e);
        }
    }

    public boolean validateToken(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) return false;
            String signingInput = parts[0] + "." + parts[1];
            String signature = parts[2];
            String expected = sign(signingInput, jwtSecret);
            if (!constantTimeEquals(expected, signature)) return false;
            String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            Map<String, Object> payload = JsonSimple.parse(payloadJson);
            Object expObj = payload.get("exp");
            if (expObj instanceof Number) {
                long exp = ((Number) expObj).longValue();
                return Instant.now().getEpochSecond() <= exp;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsername(String token) {
        String[] parts = token.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        Map<String, Object> payload = JsonSimple.parse(payloadJson);
        Object sub = payload.get("sub");
        return sub == null ? null : sub.toString();
    }

    public List<String> getRoles(String token) {
        String[] parts = token.split("\\.");
        String payloadJson = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
        Map<String, Object> payload = JsonSimple.parse(payloadJson);
        Object roles = payload.get("roles");
        if (roles == null) return List.of();
        String[] arr = roles.toString().split(",");
        return List.of(arr);
    }

    private static String base64UrlEncode(byte[] data) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private static String sign(String data, String secret) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(HMAC_ALGO);
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_ALGO));
        byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(sig);
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) return false;
        int result = 0;
        for (int i = 0; i < a.length(); i++) {
            result |= a.charAt(i) ^ b.charAt(i);
        }
        return result == 0;
    }

    private static String escape(String s) {
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    /**
     * tiny JSON parser for payload extraction (only handles flat objects with string/number values)
     */
    private static class JsonSimple {
        static Map<String, Object> parse(String json) {
            return java.util.Arrays.stream(json.trim().replaceAll("^\\{|\\}$", "").split(","))
                    .map(String::trim)
                    .filter(p -> !p.isEmpty())
                    .map(p -> {
                        int idx = p.indexOf(':');
                        String k = p.substring(0, idx).trim().replaceAll("^\"|\"$", "");
                        String vRaw = p.substring(idx + 1).trim();
                        Object v;
                        if (vRaw.startsWith("\"") && vRaw.endsWith("\"")) {
                            v = vRaw.substring(1, vRaw.length() - 1);
                        } else {
                            try { v = Long.parseLong(vRaw); } catch (Exception ex) {
                                try { v = Double.parseDouble(vRaw); } catch (Exception ex2) { v = vRaw; }
                            }
                        }
                        return new java.util.AbstractMap.SimpleEntry<>(k, v);
                    })
                    .collect(java.util.stream.Collectors.toMap(java.util.Map.Entry::getKey, java.util.Map.Entry::getValue));
        }
    }
}


