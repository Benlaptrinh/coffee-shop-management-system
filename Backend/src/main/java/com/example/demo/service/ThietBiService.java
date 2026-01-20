package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.ThietBi;

/**
 * ThietBiService
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
public interface ThietBiService {
    /**
     * Find all.
     *
     * @return result
     */
    List<ThietBi> findAll();
    /**
     * Find by id.
     *
     * @param id id
     * @return result
     */
    Optional<ThietBi> findById(Long id);
    /**
     * Save.
     *
     * @param thietBi thietBi
     * @return result
     */
    ThietBi save(ThietBi thietBi);
    /**
     * Delete by id.
     *
     * @param id id
     */
    void deleteById(Long id);
}


