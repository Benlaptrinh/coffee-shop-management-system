package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.service.CloudinaryService;

/**
 * REST Controller for Cloudinary image uploads.
 */
@RestController
@RequestMapping("/api/upload")
public class CloudinaryController {

    private final CloudinaryService cloudinaryService;

    public CloudinaryController(CloudinaryService cloudinaryService) {
        this.cloudinaryService = cloudinaryService;
    }

    /**
     * Upload a general image.
     *
     * @param file The file to upload
     * @param folder The folder (optional, defaults to "general")
     * @return Upload result
     */
    @PostMapping("/image")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", required = false, defaultValue = "general") String folder) {
        Map<String, Object> result = cloudinaryService.uploadImage(file, folder);
        return ResponseEntity.ok(result);
    }

    /**
     * Upload an avatar image.
     *
     * @param file The avatar file
     * @param userId The user ID
     * @return Upload result
     */
    @PostMapping("/avatar")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> uploadAvatar(
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") Long userId) {
        Map<String, Object> result = cloudinaryService.uploadAvatar(file, userId);
        return ResponseEntity.ok(result);
    }

    /**
     * Upload a menu item image.
     *
     * @param file The menu image
     * @param menuId The menu item ID
     * @return Upload result
     */
    @PostMapping("/menu")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> uploadMenuImage(
            @RequestParam("file") MultipartFile file,
            @RequestParam("menuId") Long menuId) {
        Map<String, Object> result = cloudinaryService.uploadMenuImage(file, menuId);
        return ResponseEntity.ok(result);
    }

    /**
     * Delete an image.
     *
     * @param publicId The public ID of the image to delete
     * @return Delete result
     */
    @DeleteMapping("/image/{publicId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> deleteImage(@PathVariable String publicId) {
        Map<String, Object> result = cloudinaryService.deleteImage(publicId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get optimized URL for an image.
     *
     * @param publicId The public ID
     * @param width Desired width
     * @param height Desired height
     * @return Optimized URL
     */
    @GetMapping("/optimize/{publicId}")
    public ResponseEntity<Map<String, String>> getOptimizedUrl(
            @PathVariable String publicId,
            @RequestParam(defaultValue = "800") int width,
            @RequestParam(defaultValue = "800") int height) {
        String url = cloudinaryService.getOptimizedUrl(publicId, width, height);
        return ResponseEntity.ok(Map.of("url", url));
    }

    /**
     * Get thumbnail URL.
     *
     * @param publicId The public ID
     * @return Thumbnail URL
     */
    @GetMapping("/thumbnail/{publicId}")
    public ResponseEntity<Map<String, String>> getThumbnailUrl(@PathVariable String publicId) {
        String url = cloudinaryService.getThumbnailUrl(publicId);
        return ResponseEntity.ok(Map.of("url", url));
    }
}

