package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.demo.entity.Ban;
import com.example.demo.entity.ChiTietDatBan;
import com.example.demo.entity.ChiTietHoaDon;
import com.example.demo.entity.HoaDon;
import com.example.demo.entity.ThucDon;
import com.example.demo.service.SalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * SalesApiController
 *
 * REST endpoints for sales flows (tables, invoices, menu, reservations).
 */
@RestController
@RequestMapping("/api/sales")
public class SalesApiController {

    private static final Logger log = LoggerFactory.getLogger(SalesApiController.class);

    private final SalesService salesService;

    public SalesApiController(SalesService salesService) {
        this.salesService = salesService;
    }

    public static class TableDto {
        public Long maBan;
        public String tenBan;
        public String tinhTrang;
    }

    public static class MenuItemDto {
        public Long maThucDon;
        public String tenMon;
        public BigDecimal giaHienTai;
        public String loaiMon;
    }

    public static class InvoiceItemDto {
        public Long maThucDon;
        public String tenMon;
        public Integer soLuong;
        public BigDecimal giaTaiThoiDiemBan;
        public BigDecimal thanhTien;
    }

    public static class InvoiceDto {
        public Long maHoaDon;
        public Long maBan;
        public String tinhTrang;
        public LocalDateTime ngayGioTao;
        public LocalDateTime ngayThanhToan;
        public BigDecimal tongTien;
        public List<InvoiceItemDto> items;
    }

    private static TableDto toTableDto(Ban b) {
        if (b == null) return null;
        TableDto d = new TableDto();
        d.maBan = b.getMaBan();
        d.tenBan = b.getTenBan();
        d.tinhTrang = b.getTinhTrang() == null ? null : b.getTinhTrang().name();
        return d;
    }

    private static MenuItemDto toMenuItemDto(ThucDon t) {
        if (t == null) return null;
        MenuItemDto d = new MenuItemDto();
        d.maThucDon = t.getMaThucDon();
        d.tenMon = t.getTenMon();
        d.giaHienTai = t.getGiaHienTai();
        d.loaiMon = t.getLoaiMon();
        return d;
    }

    private static InvoiceDto toInvoiceDto(HoaDon hd) {
        if (hd == null) return null;
        InvoiceDto dto = new InvoiceDto();
        dto.maHoaDon = hd.getMaHoaDon();
        dto.maBan = hd.getBan() == null ? null : hd.getBan().getMaBan();
        dto.tinhTrang = hd.getTrangThai() == null ? null : hd.getTrangThai().name();
        dto.ngayGioTao = hd.getNgayGioTao();
        dto.ngayThanhToan = hd.getNgayThanhToan();
        dto.tongTien = hd.getTongTien();
        if (hd.getChiTietHoaDons() != null) {
            dto.items = hd.getChiTietHoaDons().stream().map(ct -> {
                InvoiceItemDto it = new InvoiceItemDto();
                it.maThucDon = ct.getThucDon() == null ? null : ct.getThucDon().getMaThucDon();
                it.tenMon = ct.getThucDon() == null ? null : ct.getThucDon().getTenMon();
                it.soLuong = ct.getSoLuong();
                it.giaTaiThoiDiemBan = ct.getGiaTaiThoiDiemBan();
                it.thanhTien = ct.getThanhTien();
                return it;
            }).collect(Collectors.toList());
        }
        return dto;
    }

    @GetMapping("/tables")
    public ResponseEntity<List<TableDto>> listTables() {
        List<TableDto> list = salesService.findAllTables().stream().map(SalesApiController::toTableDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @GetMapping("/tables/{id}")
    public ResponseEntity<?> getTable(@PathVariable Long id) {
        Optional<Ban> b = salesService.findTableById(id);
        if (b.isEmpty()) return ResponseEntity.notFound().build();
        var table = toTableDto(b.get());
        var reservation = salesService.findLatestReservation(id).orElse(null);
        var invoice = salesService.findUnpaidInvoiceByTable(id).orElse(null);
        return ResponseEntity.ok(Map.of(
                "table", table,
                "reservation", reservation,
                "invoice", toInvoiceDto(invoice)
        ));
    }

    @GetMapping("/menu")
    public ResponseEntity<List<MenuItemDto>> menu() {
        List<MenuItemDto> list = salesService.findMenuItems().stream().map(SalesApiController::toMenuItemDto).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    public static class AddItemRequest {
        public Long thucDonId;
        public Integer soLuong;
    }

    @PostMapping("/tables/{tableId}/items")
    public ResponseEntity<?> addItem(@PathVariable Long tableId, @RequestBody AddItemRequest req) {
        if (req == null || req.thucDonId == null || req.soLuong == null || req.soLuong <= 0) {
            return ResponseEntity.badRequest().body("Invalid payload");
        }
        try {
            salesService.addItemToInvoice(tableId, req.thucDonId, req.soLuong);
            var invoice = salesService.findUnpaidInvoiceByTable(tableId).orElse(null);
            return ResponseEntity.ok(toInvoiceDto(invoice));
        } catch (Exception ex) {
            log.warn("Add item failed tableId={} itemId={} qty={} msg={}", tableId, req.thucDonId, req.soLuong, ex.getMessage());
            return ResponseEntity.status(409).body("ERROR:" + ex.getMessage());
        }
    }

    public static class MenuSelectionRequest {
        public Map<String, String> params;
    }

    @PostMapping("/tables/{tableId}/menu-selection")
    public ResponseEntity<?> menuSelection(@PathVariable Long tableId, @RequestBody MenuSelectionRequest req) {
        if (req == null || req.params == null) return ResponseEntity.badRequest().body("Missing params");
        try {
            salesService.saveSelectedMenu(tableId, req.params);
            var invoice = salesService.findUnpaidInvoiceByTable(tableId).orElse(null);
            return ResponseEntity.ok(toInvoiceDto(invoice));
        } catch (Exception ex) {
            return ResponseEntity.status(409).body("ERROR:" + ex.getMessage());
        }
    }

    public static class PayRequest {
        public BigDecimal amountPaid;
        public Boolean releaseTable = true;
    }

    @PostMapping("/tables/{tableId}/pay")
    public ResponseEntity<?> pay(@PathVariable Long tableId, @RequestBody PayRequest req) {
        if (req == null || req.amountPaid == null) return ResponseEntity.badRequest().body("Missing amountPaid");
        try {
            salesService.payInvoice(tableId, req.amountPaid, req.releaseTable == null ? true : req.releaseTable);
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("ERROR:" + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("ERROR:" + ex.getMessage());
        }
    }

    @PostMapping("/tables/{tableId}/cancel")
    public ResponseEntity<?> cancel(@PathVariable Long tableId) {
        try {
            salesService.cancelInvoice(tableId);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("ERROR:" + ex.getMessage());
        }
    }

    @PostMapping("/tables/{tableId}/reserve")
    public ResponseEntity<?> reserve(@PathVariable Long tableId, @RequestBody Map<String, Object> body) {
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
            Long from = ((Number) payload.get("fromBanId")).longValue();
            Long to = ((Number) payload.get("toBanId")).longValue();
            salesService.moveTable(from, to);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(400).body("ERROR:" + ex.getMessage());
        }
    }

    @PostMapping("/merge")
    public ResponseEntity<?> merge(@RequestBody Map<String, Object> payload) {
        try {
            Long target = ((Number) payload.get("targetBanId")).longValue();
            Long source = ((Number) payload.get("sourceBanId")).longValue();
            salesService.mergeTables(target, source);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(400).body("ERROR:" + ex.getMessage());
        }
    }

    public static class SplitRequest {
        public Long toBanId;
        public List<Map<String, Object>> items;
    }

    @PostMapping("/split")
    public ResponseEntity<?> split(@RequestBody SplitRequest req) {
        try {
            if (req == null || req.toBanId == null || req.items == null) return ResponseEntity.badRequest().body("Invalid payload");
            // expecting items as list of { thucDonId: number, soLuong: number, fromBanId: number }
            Long fromBanId = null;
            Map<Long, Integer> map = req.items.stream().collect(Collectors.toMap(
                    i -> ((Number) i.get("thucDonId")).longValue(),
                    i -> ((Number) i.get("soLuong")).intValue()
            ));
            // try to get fromBanId from first item if present
            if (!req.items.isEmpty() && req.items.get(0).containsKey("fromBanId")) {
                fromBanId = ((Number) req.items.get(0).get("fromBanId")).longValue();
            }
            if (fromBanId == null) return ResponseEntity.badRequest().body("Missing fromBanId");
            salesService.splitTable(fromBanId, req.toBanId, map);
            return ResponseEntity.ok().build();
        } catch (Exception ex) {
            return ResponseEntity.status(400).body("ERROR:" + ex.getMessage());
        }
    }

    @GetMapping("/invoices/{id}")
    public ResponseEntity<?> getInvoice(@PathVariable Long id) {
        return salesService.findInvoiceById(id).map(SalesApiController::toInvoiceDto).map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

}


