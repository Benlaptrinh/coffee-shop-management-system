package com.example.demo.controller;

import com.example.demo.service.DataService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * DataController
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
@Controller
@RequestMapping("/admin/data")
public class DataController {

    private final DataService dataService;

    /**
     * Creates DataController.
     *
     * @param dataService dataService
     */
    public DataController(DataService dataService) {
        this.dataService = dataService;
    }

    /**
     * Backup page.
     *
     * @param model model
     * @return result
     */
    @GetMapping("/backup")
    public String backupPage(Model model) {
        model.addAttribute("sidebarFragment", "fragments/sidebar-admin");
        model.addAttribute("contentFragment", "admin/data-backup");
        model.addAttribute("activeMenu", "data");
        return "layout/base";
    }

    


    /**
     * Backup.
     *
     * @param ra ra
     * @return result
     */
    @PostMapping("/backup")
    public Object backup(RedirectAttributes ra) {
        java.io.File f = null;
        try {
            f = dataService.createBackupFile();
            InputStreamResource resource = new InputStreamResource(new java.io.FileInputStream(f));
            ResponseEntity<InputStreamResource> resp = ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + f.getName() + "\"")
                    .contentLength(f.length())
                    .contentType(MediaType.APPLICATION_OCTET_STREAM)
                    .body(resource);
            return resp;
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Tạo sao lưu thất bại: " + e.getMessage());
            return "redirect:/admin/data/backup";
        }
    }

    /**
     * Restore page.
     *
     * @param model model
     * @return result
     */
    @GetMapping("/restore")
    public String restorePage(Model model) {
        model.addAttribute("sidebarFragment", "fragments/sidebar-admin");
        model.addAttribute("contentFragment", "admin/data-restore");
        model.addAttribute("activeMenu", "data");
        return "layout/base";
    }

    


    /**
     * Restore.
     *
     * @param file file
     * @param ra ra
     * @return result
     */
    @PostMapping(value = "/restore", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String restore(@RequestPart("file") MultipartFile file, RedirectAttributes ra) {
        if (file == null || file.isEmpty()) {
            ra.addFlashAttribute("error", "Vui lòng chọn file .sql để phục hồi");
            return "redirect:/admin/data/restore";
        }
        try {
            dataService.restoreFromFile(file);
            ra.addFlashAttribute("success", "Phục hồi dữ liệu thành công");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Phục hồi thất bại: " + e.getMessage());
        }
        return "redirect:/admin/data/restore";
    }
}


