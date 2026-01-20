package com.example.demo.service;

import java.io.File;

import org.springframework.web.multipart.MultipartFile;

/**
 * DataService
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
public interface DataService {
    



    /**
     * Create backup file.
     *
     * @return result
     */
    File createBackupFile();

    


    /**
     * Restore from file.
     *
     * @param file file
     */
    void restoreFromFile(MultipartFile file);
}


