package com.example.demo.service.impl;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;

import com.example.demo.service.DataService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * DataServiceImpl
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
@Service
public class DataServiceImpl implements DataService {

    private static final Logger log = LoggerFactory.getLogger(DataServiceImpl.class);

    @Value("${spring.datasource.url:}")
    private String datasourceUrl;

    @Value("${spring.datasource.username:root}")
    private String dbUser;

    @Value("${spring.datasource.password:}")
    private String dbPass;

    private String parseDbName() {
        if (datasourceUrl == null) return "";
        
        int idx = datasourceUrl.lastIndexOf('/');
        if (idx < 0) return datasourceUrl;
        String after = datasourceUrl.substring(idx + 1);
        int q = after.indexOf('?');
        if (q >= 0) return after.substring(0, q);
        return after;
    }

    private void execCommand(String[] command) {
        try {
            Process process = Runtime.getRuntime().exec(command);
            StringBuilder err = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(process.getErrorStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    err.append(line).append('\n');
                }
            }
            int rc = process.waitFor();
            if (rc != 0) {
                log.error("Command failed with exitCode={}", rc);
                throw new RuntimeException("Command failed (exit " + rc + "): " + err.toString());
            }
        } catch (Exception e) {
            log.error("Error executing command", e);
            throw new RuntimeException("Error executing command", e);
        }
    }

    /**
     * Create backup file.
     *
     * @return result
     */
    @Override
    public File createBackupFile() {
        String dbName = parseDbName();
        try {
            Path tmp = Files.createTempFile("backup-", ".sql");
            String path = tmp.toAbsolutePath().toString();
            log.info("Creating backup for dbName={}", dbName);
            String cmd = String.format("mysqldump -u%s -p%s %s -r %s", dbUser, dbPass, dbName, path);
            String[] command = isWindows() ? new String[]{"cmd.exe", "/c", cmd} : new String[]{"/bin/sh", "-c", cmd};
            execCommand(command);
            log.info("Backup created name={}", tmp.getFileName());
            return tmp.toFile();
        } catch (Exception e) {
            log.error("Failed to create backup file for dbName={}", dbName, e);
            throw new RuntimeException("Failed to create backup file: " + e.getMessage(), e);
        }
    }

    /**
     * Restore from file.
     *
     * @param file file
     */
    @Override
    public void restoreFromFile(MultipartFile file) {
        String dbName = parseDbName();
        try {
            String originalName = file == null ? null : file.getOriginalFilename();
            log.info("Restoring database dbName={} from file={}", dbName, originalName);
            Path tmp = Files.createTempFile("restore-", ".sql");
            try (InputStream in = new BufferedInputStream(file.getInputStream());
                 BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tmp.toFile()))) {
                byte[] buf = new byte[8192];
                int r;
                while ((r = in.read(buf)) != -1) {
                    out.write(buf, 0, r);
                }
            }

            String path = tmp.toAbsolutePath().toString();
            String cmd = String.format("mysql -u%s -p%s %s < %s", dbUser, dbPass, dbName, path);
            String[] command = isWindows() ? new String[]{"cmd.exe", "/c", cmd} : new String[]{"/bin/sh", "-c", cmd};
            execCommand(command);
            
            try { Files.deleteIfExists(tmp); } catch (Exception ignored) {}
            log.info("Restore completed for dbName={}", dbName);
        } catch (Exception e) {
            log.error("Failed to restore database dbName={}", dbName, e);
            throw new RuntimeException("Failed to restore database", e);
        }
    }

    private boolean isWindows() {
        String os = System.getProperty("os.name");
        return os != null && os.toLowerCase().contains("win");
    }
}
