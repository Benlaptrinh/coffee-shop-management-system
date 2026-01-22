package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.ThietBi;

/**
 * Service contract for Thiet Bi.
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
    Optional<ThietBi> findById(long id);
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
    void deleteById(long id);
}

