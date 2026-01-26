package com.example.demo.controller;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.demo.entity.Ban;
import com.example.demo.entity.ChiTietDatBan;
import com.example.demo.entity.HoaDon;
import com.example.demo.entity.ThucDon;
import com.example.demo.payload.dto.InvoiceDto;
import com.example.demo.payload.dto.InvoiceItemDto;
import com.example.demo.payload.dto.MenuItemDto;
import com.example.demo.payload.dto.ReservationDto;
import com.example.demo.payload.dto.TableDto;
import com.example.demo.payload.request.AddItemRequest;
import com.example.demo.payload.request.MergeTableRequest;
import com.example.demo.payload.request.MenuSelectionRequest;
import com.example.demo.payload.request.MoveTableRequest;
import com.example.demo.payload.request.PayRequest;
import com.example.demo.payload.request.ReserveTableRequest;
import com.example.demo.payload.request.SplitItem;
import com.example.demo.payload.request.SplitRequest;
import com.example.demo.service.SalesService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;

/**
 * SalesController
 *
 * REST endpoints for sales flows (tables, invoices, menu, reservations).
 */
@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private final SalesService salesService;
    /**
     * Creates a new Sales Controller.
     * @param salesService sales service
     */
    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    private static TableDto toTableDto(Ban b) {
        if (b == null) return null;
        TableDto d = new TableDto();
        d.setMaBan(b.getMaBan());
        d.setTenBan(b.getTenBan());
        d.setTinhTrang(b.getTinhTrang() == null ? null : b.getTinhTrang().name());
        return d;
    }

    private static MenuItemDto toMenuItemDto(ThucDon t) {
        if (t == null) return null;
        MenuItemDto d = new MenuItemDto();
        d.setMaThucDon(t.getMaThucDon());
        d.setTenMon(t.getTenMon());
        d.setGiaHienTai(t.getGiaHienTai());
        d.setLoaiMon(t.getLoaiMon());
        return d;
    }

    private static InvoiceDto toInvoiceDto(HoaDon hd) {
        if (hd == null) return null;
        InvoiceDto dto = new InvoiceDto();
        dto.setMaHoaDon(hd.getMaHoaDon());
        dto.setMaBan(hd.getBan() == null ? null : hd.getBan().getMaBan());
        dto.setTinhTrang(hd.getTrangThai() == null ? null : hd.getTrangThai().name());
        dto.setNgayGioTao(hd.getNgayGioTao());
        dto.setNgayThanhToan(hd.getNgayThanhToan());
        dto.setTongTien(hd.getTongTien());
        dto.setTenNhanVien(hd.getNhanVien() == null ? null : hd.getNhanVien().getHoTen());
        dto.setTenKhachDat(hd.getTenKhachDat());
        if (hd.getChiTietHoaDons() != null) {
            dto.setItems(hd.getChiTietHoaDons().stream().map(ct -> {
                InvoiceItemDto it = new InvoiceItemDto();
                it.setMaThucDon(ct.getThucDon() == null ? null : ct.getThucDon().getMaThucDon());
                it.setTenMon(ct.getThucDon() == null ? null : ct.getThucDon().getTenMon());
                it.setSoLuong(ct.getSoLuong());
                it.setGiaTaiThoiDiemBan(ct.getGiaTaiThoiDiemBan());
                it.setThanhTien(ct.getThanhTien());
                return it;
            }).collect(Collectors.toList()));
        }
        return dto;
    }

    private static ReservationDto toReservationDto(ChiTietDatBan res) {
        if (res == null) return null;
        ReservationDto dto = new ReservationDto();
        dto.setMaBan(res.getBan() == null ? null : res.getBan().getMaBan());
        dto.setTenKhach(res.getTenKhach());
        dto.setSdt(res.getSdt());
        dto.setNgayGioDat(res.getNgayGioDat());
        return dto;
    }
    /**
     * Lists tables.
     * @return response entity
     */
    @GetMapping("/tables")
    public ResponseEntity<List<TableDto>> listTables() {
        List<TableDto> list = salesService.findAllTables().stream().map(SalesController::toTableDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
    /**
     * Returns table.
     * @param id id
     * @return response entity
     */
    @GetMapping("/tables/{id}")
    public ResponseEntity<?> getTable(@PathVariable long id) {
        Optional<Ban> b = salesService.findTableById(id);
        if (b.isEmpty()) {
            throw new java.util.NoSuchElementException("Không tìm thấy bàn");
        }
        var table = toTableDto(b.get());
        var reservation = toReservationDto(salesService.findLatestReservation(id).orElse(null));
        var invoice = salesService.findUnpaidInvoiceByTable(id).orElse(null);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", table);
        body.put("reservation", reservation);
        body.put("invoice", toInvoiceDto(invoice));
        return ResponseEntity.ok(body);
    }
    /**
     * Returns menu items.
     * @return response entity
     */
    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemDto>> menu() {
        List<MenuItemDto> list = salesService.findMenuItems().stream().map(SalesController::toMenuItemDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }
    /**
     * Adds item.
     * @param tableId table id
     * @param req request payload
     * @return response entity
     */
    @PostMapping("/tables/{tableId}/items")
    public ResponseEntity<?> addItem(@PathVariable long tableId, @Valid @RequestBody AddItemRequest req) {
        salesService.addItemToInvoice(tableId, req.getThucDonId(), req.getSoLuong());
        var invoice = salesService.findUnpaidInvoiceByTable(tableId).orElse(null);
        return ResponseEntity.ok(toInvoiceDto(invoice));
    }
    /**
     * Saves menu selection.
     * @param tableId table id
     * @param req request payload
     * @return response entity
     */
    @PostMapping("/tables/{tableId}/menu-selection")
    public ResponseEntity<?> menuSelection(@PathVariable long tableId, @Valid @RequestBody MenuSelectionRequest req) {
        salesService.saveSelectedMenu(tableId, req.getParams());
        var invoice = salesService.findUnpaidInvoiceByTable(tableId).orElse(null);
        return ResponseEntity.ok(toInvoiceDto(invoice));
    }
    /**
     * Processes payment.
     * @param tableId table id
     * @param req request payload
     * @return response entity
     */
    @PostMapping("/tables/{tableId}/pay")
    public ResponseEntity<?> pay(@PathVariable long tableId, @Valid @RequestBody PayRequest req) {
        salesService.payInvoice(tableId, req.getAmountPaid(),
            req.getReleaseTable() == null ? true : req.getReleaseTable());
        return ResponseEntity.ok().build();
    }
    /**
     * Cancels the invoice.
     * @param tableId table id
     * @return response entity
     */
    @PostMapping("/tables/{tableId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable long tableId) {
        salesService.cancelInvoice(tableId);
        return ResponseEntity.ok().build();
    }
    /**
     * Creates a reservation.
     * @param tableId table id
     * @param body request body
     * @return response entity
     */
    @PostMapping("/tables/{tableId}/reserve")
    public ResponseEntity<?> reserve(@PathVariable long tableId, @Valid @RequestBody ReserveTableRequest req) {
        salesService.reserveTable(tableId, req.getTenKhach(), req.getSdt(), req.getNgayGio());
        return ResponseEntity.ok().build();
    }
    /**
     * Cancels a reservation.
     * @param tableId table id
     * @return response entity
     */
    @PostMapping("/tables/{tableId}/cancel-reservation")
    public ResponseEntity<?> cancelReservation(@PathVariable long tableId) {
        salesService.cancelReservation(tableId);
        return ResponseEntity.ok().build();
    }
    /**
     * Moves a table.
     * @param payload payload
     * @return response entity
     */
    @PostMapping("/move")
    public ResponseEntity<?> move(@Valid @RequestBody MoveTableRequest payload) {
        salesService.moveTable(payload.getFromBanId(), payload.getToBanId());
        return ResponseEntity.ok().build();
    }
    /**
     * Merges tables.
     * @param payload payload
     * @return response entity
     */
    @PostMapping("/merge")
    public ResponseEntity<?> merge(@Valid @RequestBody MergeTableRequest payload) {
        salesService.mergeTables(payload.getTargetBanId(), payload.getSourceBanId());
        return ResponseEntity.ok().build();
    }
    /**
     * Splits a table.
     * @param req request payload
     * @return response entity
     */
    @PostMapping("/split")
    public ResponseEntity<?> split(@Valid @RequestBody SplitRequest req) {
        Long fromBanId = req.getFromBanId();
        if (fromBanId == null && req.getItems() != null && !req.getItems().isEmpty()) {
            SplitItem first = req.getItems().get(0);
            fromBanId = first.getFromBanId();
        }
        if (fromBanId == null) {
            throw new IllegalArgumentException("Missing fromBanId");
        }
        Map<Long, Integer> map = req.getItems().stream().collect(Collectors.toMap(
                SplitItem::getThucDonId,
                SplitItem::getSoLuong
        ));
        salesService.splitTable(fromBanId, req.getToBanId(), map);
        return ResponseEntity.ok().build();
    }
    /**
     * Returns invoice.
     * @param id id
     * @return response entity
     */
    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> getInvoice(@PathVariable long id) {
        return salesService.findInvoiceById(id)
                .map(SalesController::toInvoiceDto)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy hóa đơn"));
    }

}
