package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.payload.dto.ThuChiDto;
import com.example.demo.payload.form.ChiTieuForm;

/**
 * NganSachService
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
public interface NganSachService {
    /**
     * Xem thu chi.
     *
     * @param from from
     * @param to to
     * @return result
     */
    List<ThuChiDto> xemThuChi(LocalDate from, LocalDate to);
    /**
     * Them chi tieu.
     *
     * @param form form
     * @param username username
     */
    void themChiTieu(ChiTieuForm form, String username);
}


