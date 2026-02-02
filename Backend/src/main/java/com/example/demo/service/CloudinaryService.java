package com.example.demo.service;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for Cloudinary image uploads using direct HTTP API.
 * No SDK dependency needed - uses Java 11+ HTTP client.
 */
@Service
public class CloudinaryService {

    @Value("${cloudinary.cloud-name}")
    private String cloudName;

    @Value("${cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    private final ObjectMapper objectMapper;
    private final java.net.http.HttpClient httpClient;

    public CloudinaryService(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        this.httpClient = java.net.http.HttpClient.newHttpClient();
    }

    /**
     * Generate signature for upload.
     */
    private String generateSignature(Map<String, Object> params) throws Exception {
        // Sort parameters alphabetically
        var sortedKeys = params.keySet().stream().sorted().toList();
        StringBuilder toSign = new StringBuilder();
        
        for (String key : sortedKeys) {
            if (toSign.length() > 0) {
                toSign.append("&");
            }
            toSign.append(key).append("=").append(params.get(key).toString());
        }
        toSign.append(apiSecret);
        
        // SHA-1 hash
        java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
        byte[] hash = digest.digest(toSign.toString().getBytes());
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Upload an image to Cloudinary.
     */
    public Map<String, Object> uploadImage(MultipartFile file, String folder) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String publicId = "quancafepro_" + folder + "_" + System.currentTimeMillis();

            // Build signature
            Map<String, Object> params = new HashMap<>();
            params.put("timestamp", timestamp);
            params.put("folder", "quancafepro/" + folder);
            params.put("public_id", publicId);
            params.put("upload_preset", "quancafepro_unsigned"); // Use unsigned upload preset
            
            // Generate signature (optional for unsigned upload)
            // String signature = generateSignature(params);

            // Build multipart form data
            String boundary = "----CloudinaryBoundary" + System.currentTimeMillis();
            byte[] fileBytes = file.getBytes();
            
            StringBuilder body = new StringBuilder();
            body.append("--").append(boundary).append("\r\n");
            body.append("Content-Disposition: form-data; name=\"file\"; filename=\"").append(file.getOriginalFilename()).append("\"\r\n");
            body.append("Content-Type: ").append(file.getContentType()).append("\r\n\r\n");
            String bodyStart = body.toString();
            String bodyEnd = "\r\n--" + boundary + "--\r\n";
            
            byte[] bodyStartBytes = bodyStart.getBytes();
            byte[] bodyEndBytes = bodyEnd.getBytes();
            
            byte[] fullBody = new byte[bodyStartBytes.length + fileBytes.length + bodyEndBytes.length];
            System.arraycopy(bodyStartBytes, 0, fullBody, 0, bodyStartBytes.length);
            System.arraycopy(fileBytes, 0, fullBody, bodyStartBytes.length, fileBytes.length);
            System.arraycopy(bodyEndBytes, 0, fullBody, bodyStartBytes.length + fileBytes.length, bodyEndBytes.length);

            // Send request
            String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(fullBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Cloudinary upload failed: " + response.body());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            Map<String, Object> result = new HashMap<>();
            result.put("url", jsonNode.get("secure_url").asText());
            result.put("publicId", jsonNode.get("public_id").asText());
            result.put("format", jsonNode.get("format").asText());
            result.put("width", jsonNode.get("width").asInt());
            result.put("height", jsonNode.get("height").asInt());
            result.put("bytes", jsonNode.get("bytes").asLong());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload image to Cloudinary", e);
        }
    }

    /**
     * Upload an avatar image.
     */
    public Map<String, Object> uploadAvatar(MultipartFile file, Long userId) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            String publicId = "quancafepro_avatar_" + userId;

            String boundary = "----CloudinaryBoundary" + System.currentTimeMillis();
            byte[] fileBytes = file.getBytes();
            
            StringBuilder body = new StringBuilder();
            body.append("--").append(boundary).append("\r\n");
            body.append("Content-Disposition: form-data; name=\"file\"; filename=\"avatar.jpg\"\r\n");
            body.append("Content-Type: image/jpeg\r\n\r\n");
            String bodyStart = body.toString();
            String bodyEnd = "\r\n--" + boundary + "--\r\n";
            
            byte[] bodyStartBytes = bodyStart.getBytes();
            byte[] bodyEndBytes = bodyEnd.getBytes();
            
            byte[] fullBody = new byte[bodyStartBytes.length + fileBytes.length + bodyEndBytes.length];
            System.arraycopy(bodyStartBytes, 0, fullBody, 0, bodyStartBytes.length);
            System.arraycopy(fileBytes, 0, fullBody, bodyStartBytes.length, fileBytes.length);
            System.arraycopy(bodyEndBytes, 0, fullBody, bodyStartBytes.length + fileBytes.length, bodyEndBytes.length);

            // Upload with transformation for avatar (200x200, circular crop)
            String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/upload";
            String uploadUrl = url + "?transformation=w_200,h_200,c_fill,g_face,r_max,q_auto";
            
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(uploadUrl))
                    .header("Content-Type", "multipart/form-data; boundary=" + boundary)
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofByteArray(fullBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Cloudinary avatar upload failed: " + response.body());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            Map<String, Object> result = new HashMap<>();
            result.put("url", jsonNode.get("secure_url").asText());
            result.put("publicId", jsonNode.get("public_id").asText());

            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload avatar to Cloudinary", e);
        }
    }

    /**
     * Upload a menu item image.
     */
    public Map<String, Object> uploadMenuImage(MultipartFile file, Long menuId) {
        try {
            // Menu images use the same uploadImage method but with different folder
            Map<String, Object> result = uploadImage(file, "menu");
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to upload menu image to Cloudinary", e);
        }
    }

    /**
     * Delete an image from Cloudinary (requires signed request).
     */
    public Map<String, Object> deleteImage(String publicId) {
        try {
            String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
            
            // Build signature
            String toSign = "public_id=" + publicId + "&timestamp=" + timestamp + apiSecret;
            java.security.MessageDigest digest = java.security.MessageDigest.getInstance("SHA-1");
            byte[] hash = digest.digest(toSign.getBytes());
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            String signature = hexString.toString();

            String auth = apiKey + ":" + ""; // API key as username, empty password
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            String url = "https://api.cloudinary.com/v1_1/" + cloudName + "/image/destroy";
            String jsonBody = String.format("{\"public_id\":\"%s\",\"timestamp\":\"%s\",\"api_key\":\"%s\",\"signature\":\"%s\"}",
                    publicId, timestamp, apiKey, signature);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(url))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            
            if (response.statusCode() != 200) {
                throw new RuntimeException("Cloudinary delete failed: " + response.body());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            Map<String, Object> result = new HashMap<>();
            result.put("result", jsonNode.get("result").asText());
            
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete image from Cloudinary", e);
        }
    }

    /**
     * Get optimized URL for an image.
     */
    public String getOptimizedUrl(String publicId, int width, int height) {
        return String.format(
            "https://res.cloudinary.com/%s/image/upload/c_fill,w_%d,h_%d,q_auto,f_auto/%s",
            cloudName, width, height, publicId
        );
    }

    /**
     * Generate thumbnail URL.
     */
    public String getThumbnailUrl(String publicId) {
        return getOptimizedUrl(publicId, 150, 150);
    }
}
