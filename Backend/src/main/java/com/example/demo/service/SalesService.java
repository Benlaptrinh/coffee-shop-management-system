package com.example.demo.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.demo.entity.Ban;
import com.example.demo.entity.ChiTietDatBan;
import com.example.demo.entity.HoaDon;
import com.example.demo.entity.ThucDon;

/**
 * SalesService
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
public interface SalesService {
    /**
     * Find all tables.
     *
     * @return result
     */
    List<Ban> findAllTables();
    /**
     * Find table by id.
     *
     * @param tableId tableId
     * @return result
     */
    Optional<Ban> findTableById(long tableId);
    /**
     * Find latest reservation for table.
     *
     * @param banId banId
     * @return result
     */
    Optional<ChiTietDatBan> findLatestReservation(long banId);
    /**
     * Find unpaid invoice by table.
     *
     * @param tableId tableId
     * @return result
     */
    Optional<HoaDon> findUnpaidInvoiceByTable(long tableId);
    /**
     * Find menu items.
     *
     * @return result
     */
    List<ThucDon> findMenuItems();
    /**
     * Add item to invoice.
     *
     * @param tableId tableId
     * @param itemId itemId
     * @param quantity quantity
     */
    void addItemToInvoice(long tableId, long itemId, int quantity);
    /**
     * Pay invoice.
     *
     * @param tableId tableId
     * @param tienKhach tienKhach
     * @param releaseTable releaseTable
     */
    void payInvoice(long tableId, BigDecimal tienKhach, boolean releaseTable);
    /**
     * Reserve table.
     *
     * @param banId banId
     * @param tenKhach tenKhach
     * @param sdt sdt
     * @param ngayGioDat ngayGioDat
     */
    void reserveTable(long banId, String tenKhach, String sdt, LocalDateTime ngayGioDat);
    /**
     * Save selected menu.
     *
     * @param banId banId
     * @param params params
     */
    void saveSelectedMenu(long banId, Map<String,String> params);
    /**
     * Cancel invoice.
     *
     * @param banId banId
     */
    void cancelInvoice(long banId);
    /**
     * Find invoice by id.
     *
     * @param id id
     * @return result
     */
    Optional<HoaDon> findInvoiceById(long id);

    
    /**
     * Move table.
     *
     * @param fromBanId fromBanId
     * @param toBanId toBanId
     */
    void moveTable(long fromBanId, long toBanId);
    /**
     * Find empty tables.
     *
     * @return result
     */
    List<Ban> findEmptyTables();
    /**
     * Find merge candidates.
     *
     * @param excludeBanId excludeBanId
     * @return result
     */
    List<Ban> findMergeCandidates(long excludeBanId);
    /**
     * Merge tables.
     *
     * @param targetBanId targetBanId
     * @param sourceBanId sourceBanId
     */
    void mergeTables(long targetBanId, long sourceBanId);
    /**
     * Split table.
     *
     * @param fromBanId fromBanId
     * @param toBanId toBanId
     * @param itemQuantities itemQuantities
     */
    void splitTable(long fromBanId, long toBanId, Map<Long, Integer> itemQuantities);
    /**
     * Cancel reservation.
     *
     * @param banId banId
     */
    void cancelReservation(long banId);
}
