package com.example.demo.controller;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.demo.dto.AddItemRequest;
import com.example.demo.dto.InvoiceDto;
import com.example.demo.dto.InvoiceItemDto;
import com.example.demo.dto.MenuItemDto;
import com.example.demo.dto.MenuSelectionRequest;
import com.example.demo.dto.PayRequest;
import com.example.demo.dto.ReservationDto;
import com.example.demo.dto.SplitRequest;
import com.example.demo.dto.TableDto;
import com.example.demo.entity.Ban;
import com.example.demo.entity.ChiTietDatBan;
import com.example.demo.entity.HoaDon;
import com.example.demo.entity.ThucDon;
import com.example.demo.service.SalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * SalesController
 *
 * REST endpoints for sales flows (tables, invoices, menu, reservations).
 */
@RestController
@RequestMapping("/api/sales")
public class SalesController {

    private static final Logger log = LoggerFactory.getLogger(SalesController.class);

    private final SalesService salesService;

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

    @GetMapping("/tables")
    public ResponseEntity<List<TableDto>> listTables() {
        List<TableDto> list = salesService.findAllTables().stream().map(SalesController::toTableDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/tables/{id}")
    public ResponseEntity<?> getTable(@PathVariable long id) {
        Optional<Ban> b = salesService.findTableById(id);
        if (b.isEmpty()) return ResponseEntity.notFound().build();
        var table = toTableDto(b.get());
        var reservation = toReservationDto(salesService.findLatestReservation(id).orElse(null));
        var invoice = salesService.findUnpaidInvoiceByTable(id).orElse(null);
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("table", table);
        body.put("reservation", reservation);
        body.put("invoice", toInvoiceDto(invoice));
        return ResponseEntity.ok(body);
    }

    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemDto>> menu() {
        List<MenuItemDto> list = salesService.findMenuItems().stream().map(SalesController::toMenuItemDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/tables/{tableId}/items")
    public ResponseEntity<?> addItem(@PathVariable long tableId, @RequestBody AddItemRequest req) {
        if (req == null || req.getThucDonId() == null || req.getSoLuong() == null || req.getSoLuong() <= 0) {
            return ResponseEntity.badRequest().body("Invalid payload");
        }
        try {
            salesService.addItemToInvoice(tableId, req.getThucDonId(), req.getSoLuong());
            var invoice = salesService.findUnpaidInvoiceByTable(tableId).orElse(null);
            return ResponseEntity.ok(toInvoiceDto(invoice));
        } catch (Exception ex) {
            log.warn("Add item failed tableId={} itemId={} qty={} msg={}", tableId, req.getThucDonId(), req.getSoLuong(), ex.getMessage());
            return ResponseEntity.status(409).body("ERROR:" + ex.getMessage());
        }
    }

    @PostMapping("/tables/{tableId}/menu-selection")
    public ResponseEntity<?> menuSelection(@PathVariable long tableId, @RequestBody MenuSelectionRequest req) {
        if (req == null || req.getParams() == null) return ResponseEntity.badRequest().body("Missing params");
        try {
            salesService.saveSelectedMenu(tableId, req.getParams());
            var invoice = salesService.findUnpaidInvoiceByTable(tableId).orElse(null);
            return ResponseEntity.ok(toInvoiceDto(invoice));
        } catch (Exception ex) {
            return ResponseEntity.status(409).body("ERROR:" + ex.getMessage());
        }
    }

    @PostMapping("/tables/{tableId}/pay")
    public ResponseEntity<?> pay(@PathVariable long tableId, @RequestBody PayRequest req) {
        if (req == null || req.getAmountPaid() == null) return ResponseEntity.badRequest().body("Missing amountPaid");
        try {
            salesService.payInvoice(tableId, req.getAmountPaid(),
                req.getReleaseTable() == null ? true : req.getReleaseTable());
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("ERROR:" + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("ERROR:" + ex.getMessage());
        }
    }

    @PostMapping("/tables/{tableId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable long tableId) {
        try {
            salesService.cancelInvoice(tableId);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("ERROR:" + ex.getMessage());
        }
    }

    @PostMapping("/tables/{tableId}/reserve")
    public ResponseEntity<?> reserve(@PathVariable long tableId, @RequestBody Map<String, Object> body) {
        try {
            String ten = (String) body.get("tenKhach");
            String sdt = (String) body.get("sdt");
            String ngayGio = (String) body.get("ngayGio");
            LocalDateTime when = ngayGio == null ? null : LocalDateTime.parse(ngayGio);
            salesService.reserveTable(tableId, ten, sdt, when);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("ERROR:" + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body("ERROR:" + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("ERROR:" + ex.getMessage());
        }
    }

    @PostMapping("/move")
    public ResponseEntity<?> move(@RequestBody Map<String, Object> payload) {
        try {
            long from = ((Number) payload.get("fromBanId")).longValue();
            long to = ((Number) payload.get("toBanId")).longValue();
            salesService.moveTable(from, to);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(400).body("ERROR:" + ex.getMessage());
        }
    }

    @PostMapping("/merge")
    public ResponseEntity<?> merge(@RequestBody Map<String, Object> payload) {
        try {
            long target = ((Number) payload.get("targetBanId")).longValue();
            long source = ((Number) payload.get("sourceBanId")).longValue();
            salesService.mergeTables(target, source);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(400).body("ERROR:" + ex.getMessage());
        }
    }

    @PostMapping("/split")
    public ResponseEntity<?> split(@RequestBody SplitRequest req) {
        try {
            if (req == null || req.getToBanId() == null || req.getItems() == null) {
                return ResponseEntity.badRequest().body("Invalid payload");
            }
            Long fromBanId = null;
            Map<Long, Integer> map = req.getItems().stream().collect(Collectors.toMap(
                    i -> ((Number) i.get("thucDonId")).longValue(),
                    i -> ((Number) i.get("soLuong")).intValue()
            ));
            if (!req.getItems().isEmpty() && req.getItems().get(0).containsKey("fromBanId")) {
                fromBanId = ((Number) req.getItems().get(0).get("fromBanId")).longValue();
            }
            if (fromBanId == null) return ResponseEntity.badRequest().body("Missing fromBanId");
            salesService.splitTable(fromBanId, req.getToBanId(), map);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(400).body("ERROR:" + ex.getMessage());
        }
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> getInvoice(@PathVariable long id) {
        return salesService.findInvoiceById(id).map(SalesController::toInvoiceDto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}
