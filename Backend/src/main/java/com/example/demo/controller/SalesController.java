package com.example.demo.controller;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.example.demo.entity.Ban;
import com.example.demo.entity.ChiTietDatBan;
import com.example.demo.entity.ChiTietHoaDon;
import com.example.demo.entity.HoaDon;
import com.example.demo.service.SalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * SalesController
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
@RequestMapping("/sales")
public class SalesController {

    private static final Logger log = LoggerFactory.getLogger(SalesController.class);

    private final SalesService salesService;

    /**
     * Creates SalesController.
     *
     * @param salesService salesService
     */
    public SalesController(SalesService salesService) {
        this.salesService = salesService;
    }

    /**
     * Index.
     *
     * @param model model
     * @return result
     */
    @GetMapping
    public String index(Model model) {
        List<Ban> tables = salesService.findAllTables();
        log.info("Sales index loaded tables={}", tables.size());
        model.addAttribute("tables", tables);
        return "sales/index";
    }

    /**
     * View ban.
     *
     * @param id id
     * @param model model
     * @return result
     */
    @GetMapping("/ban/{id}")
    public String viewBan(@PathVariable Long id, Model model) {
        Optional<HoaDon> hoaDonOpt = salesService.findUnpaidInvoiceByTable(id);
        model.addAttribute("hoaDon", hoaDonOpt.orElse(null));
        model.addAttribute("tableId", id);
        boolean reserved = false;
        Optional<ChiTietDatBan> reservationOpt = salesService.findLatestReservation(id);
        if (reservationOpt.isPresent()) {
            reserved = true;
            model.addAttribute("reservation", reservationOpt.get());
        }
        log.info("View ban id={} invoicePresent={} reserved={}", id, hoaDonOpt.isPresent(), reserved);
        model.addAttribute("reserved", reserved);
        return "sales/view-ban";
    }

    /**
     * Select menu form.
     *
     * @param banId banId
     * @param model model
     * @return result
     */
    @GetMapping("/{banId}/menu")
    public String selectMenuForm(@PathVariable Long banId, Model model) {
        List<?> menu = salesService.findMenuItems();
        model.addAttribute("menu", menu);
        model.addAttribute("tableId", banId);
        Optional<HoaDon> hoaDonOpt = salesService.findUnpaidInvoiceByTable(banId);
        model.addAttribute("hoaDon", hoaDonOpt.orElse(null));
        Map<Long, Integer> qtyMap = new HashMap<>();
        hoaDonOpt.ifPresent(hd -> {
            if (hd.getChiTietHoaDons() != null) {
                for (ChiTietHoaDon ct : hd.getChiTietHoaDons()) {
                    if (ct.getThucDon() != null && ct.getSoLuong() != null) {
                        qtyMap.put(ct.getThucDon().getMaThucDon(), ct.getSoLuong());
                    }
                }
            }
        });
        log.info("Select menu form banId={} menuItems={} invoicePresent={}",
                banId, menu.size(), hoaDonOpt.isPresent());
        model.addAttribute("qtyMap", qtyMap);
        return "sales/fragments/select-menu :: content";
    }

    /**
     * Select menu modal.
     *
     * @param banId banId
     * @param model model
     * @return result
     */
    @GetMapping("/{banId}/menu/modal")
    public String selectMenuModal(@PathVariable Long banId, Model model) {
        
        List<?> menu = salesService.findMenuItems();
        model.addAttribute("menu", menu);
        model.addAttribute("tableId", banId);
        Optional<HoaDon> hoaDonOpt = salesService.findUnpaidInvoiceByTable(banId);
        model.addAttribute("hoaDon", hoaDonOpt.orElse(null));
        Map<Long, Integer> qtyMap = new HashMap<>();
        hoaDonOpt.ifPresent(hd -> {
            if (hd.getChiTietHoaDons() != null) {
                for (ChiTietHoaDon ct : hd.getChiTietHoaDons()) {
                    if (ct.getThucDon() != null && ct.getSoLuong() != null) {
                        qtyMap.put(ct.getThucDon().getMaThucDon(), ct.getSoLuong());
                    }
                }
            }
        });
        log.info("Select menu modal banId={} menuItems={} invoicePresent={}",
                banId, menu.size(), hoaDonOpt.isPresent());
        model.addAttribute("qtyMap", qtyMap);
        return "sales/fragments/select-menu :: content";
    }

    /**
     * Menu redirect.
     *
     * @param banId banId
     * @return result
     */
    @GetMapping("/menu")
    public String menuRedirect(@RequestParam("banId") Long banId) {
        return "redirect:/sales/" + banId + "/menu";
    }

    /**
     * Select menu submit.
     *
     * @param banId banId
     * @param params params
     * @return result
     */
    @PostMapping("/{banId}/menu")
    public String selectMenuSubmit(@PathVariable Long banId,
                                   @RequestParam Map<String,String> params) {
        int itemCount = 0;
        int totalQty = 0;
        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (entry.getKey().startsWith("qty_")) {
                try {
                    int qty = Integer.parseInt(entry.getValue());
                    if (qty > 0) {
                        itemCount++;
                        totalQty += qty;
                    }
                } catch (NumberFormatException ignored) {
                }
            }
        }
        log.info("Select menu submit banId={} itemsSelected={} totalQty={} paramsSize={}",
                banId, itemCount, totalQty, params.size());
        salesService.saveSelectedMenu(banId, params);
        return "redirect:/admin/sales";
    }

    /**
     * Payment form.
     *
     * @param banId banId
     * @param model model
     * @return result
     */
    @GetMapping("/{banId}/payment")
    public String paymentForm(@PathVariable Long banId, Model model) {
        Optional<HoaDon> hoaDonOpt = salesService.findUnpaidInvoiceByTable(banId);
        model.addAttribute("hoaDon", hoaDonOpt.orElse(null));
        model.addAttribute("tableId", banId);
        log.info("Payment form banId={} invoicePresent={}", banId, hoaDonOpt.isPresent());
        return "sales/payment";
    }

    /**
     * Payment redirect.
     *
     * @param banId banId
     * @return result
     */
    @GetMapping("/payment")
    public String paymentRedirect(@RequestParam("banId") Long banId) {
        return "redirect:/sales/" + banId + "/payment";
    }

    /**
     * Thanh toan.
     *
     * @param banId banId
     * @param tienKhach tienKhach
     * @param print print
     * @return result
     */
    @PostMapping("/thanh-toan/{banId}")
    public String thanhToan(@PathVariable Long banId,
                            @RequestParam("tienKhach") String tienKhach,
                            @RequestParam(value = "print", required = false) boolean print) {
        BigDecimal money;
        try {
            money = new BigDecimal(tienKhach.replaceAll("[^0-9.]", ""));
        } catch (Exception ex) {
            money = BigDecimal.ZERO;
        }
        
        Optional<HoaDon> hdOpt = salesService.findUnpaidInvoiceByTable(banId);
        boolean release = false;
        if (hdOpt.isPresent()) {
            HoaDon hd = hdOpt.get();
            BigDecimal total = hd.getTongTien() == null ? BigDecimal.ZERO : hd.getTongTien();
            if (money.compareTo(total) >= 0) release = true;
        }
        log.info("Thanh toan request banId={} tienKhach={} releaseTable={} print={}",
                banId, money, release, print);
        try {
            salesService.payInvoice(banId, money, release);
            log.info("Thanh toan completed banId={} releaseTable={}", banId, release);
        } catch (RuntimeException ex) {
            log.warn("Thanh toan failed banId={} message={}", banId, ex.getMessage());
            throw ex;
        }
        return "redirect:/admin/sales";
    }

    /**
     * Payment modal.
     *
     * @param banId banId
     * @param model model
     * @return result
     */
    @GetMapping("/{banId}/payment/modal")
    public String paymentModal(@PathVariable Long banId, Model model) {
        model.addAttribute("tableId", banId);
        Optional<HoaDon> hoaDonOpt = salesService.findUnpaidInvoiceByTable(banId);
        model.addAttribute("hoaDon", hoaDonOpt.orElse(null));
        log.info("Payment modal banId={} invoicePresent={}", banId, hoaDonOpt.isPresent());
        return "sales/fragments/payment :: content";
    }

    /**
     * Payment submit.
     *
     * @param banId banId
     * @param tienKhach tienKhach
     * @param print print
     * @return result
     */
    @PostMapping("/{banId}/payment")
    /**
     * Payment submit.
     *
     * @param banId banId
     * @param tienKhach tienKhach
     * @param print print
     * @return result
     */
    @ResponseBody
    public String paymentSubmit(@PathVariable Long banId,
                                @RequestParam("tienKhach") String tienKhach,
                                @RequestParam(value = "print", required = false) boolean print) {
        BigDecimal money;
        try {
            money = new BigDecimal(tienKhach.replaceAll("[^0-9.]", ""));
        } catch (Exception ex) {
            money = BigDecimal.ZERO;
        }
        try {
            Optional<HoaDon> hdOpt = salesService.findUnpaidInvoiceByTable(banId);
            if (hdOpt.isEmpty()) {
                log.warn("Payment submit failed banId={} reason=no unpaid invoice", banId);
                return "ERROR:Không tìm thấy hóa đơn để thanh toán";
            }
            HoaDon hd = hdOpt.get();
            BigDecimal total = hd.getTongTien() == null ? BigDecimal.ZERO : hd.getTongTien();
            boolean release = money.compareTo(total) >= 0;
            salesService.payInvoice(banId, money, release);
            if (print) {
                
                Long id = hd.getMaHoaDon();
                log.info("Payment submit ok banId={} invoiceId={} releaseTable={} print=true",
                        banId, id, release);
                return "OK:PRINT:" + id;
            }
            log.info("Payment submit ok banId={} invoiceId={} releaseTable={} print=false",
                    banId, hd.getMaHoaDon(), release);
            return "OK";
        } catch (Exception ex) {
            log.warn("Payment submit failed banId={} message={}", banId, ex.getMessage());
            return "ERROR:" + ex.getMessage();
        }
    }

    /**
     * Cancel invoice.
     *
     * @param banId banId
     * @param ra ra
     * @return result
     */
    @PostMapping("/{banId}/cancel")
    public String cancelInvoice(@PathVariable Long banId, RedirectAttributes ra) {
        log.info("Cancel invoice request banId={}", banId);
        try {
            salesService.cancelInvoice(banId);
            ra.addFlashAttribute("success", "Hủy hóa đơn thành công");
            log.info("Cancel invoice completed banId={}", banId);
            return "redirect:/admin/sales";
        } catch (RuntimeException ex) {
            log.error("Cancel invoice failed banId={} message={}", banId, ex.getMessage(), ex);
            throw ex;
        }
    }

    /**
     * View redirect.
     *
     * @param banId banId
     * @param model model
     * @return result
     */
    @GetMapping("/view")
    public String viewRedirect(@RequestParam("banId") Long banId, Model model) {
        return viewBan(banId, model);
    }

    /**
     * View ban fragment.
     *
     * @param id id
     * @param model model
     * @return result
     */
    @GetMapping("/ban/{id}/view")
    public String viewBanFragment(@PathVariable("id") Long id, Model model) {
        Optional<HoaDon> hdOpt = salesService.findUnpaidInvoiceByTable(id);
        model.addAttribute("banId", id);
        boolean reserved = false;
        Optional<ChiTietDatBan> reservationOpt = salesService.findLatestReservation(id);
        if (reservationOpt.isPresent()) {
            reserved = true;
            model.addAttribute("reservation", reservationOpt.get());
        }
        if (hdOpt.isPresent()) {
            HoaDon hd = hdOpt.get();
            model.addAttribute("hoaDon", hd);
            model.addAttribute("details", hd.getChiTietHoaDons());
        }
        log.info("View ban fragment id={} invoicePresent={} reserved={}", id, hdOpt.isPresent(), reserved);
        model.addAttribute("reserved", reserved);
        return "sales/fragments/view-ban :: content";
    }

    /**
     * Reserve ban fragment.
     *
     * @param id id
     * @param model model
     * @return result
     */
    @GetMapping("/ban/{id}/reserve")
    public String reserveBanFragment(@PathVariable("id") Long id, Model model) {
        model.addAttribute("banId", id);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        String minFormatted = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(now);
        model.addAttribute("minDate", minFormatted);
        log.info("Reserve ban fragment banId={} minDate={}", id, minFormatted);
        return "sales/fragments/reserve :: content";
    }

    /**
     * Reserve ban submit.
     *
     * @param id id
     * @param tenKhach tenKhach
     * @param sdt sdt
     * @param ngayGio ngayGio
     * @return result
     */
    @PostMapping("/ban/{id}/reserve")
    /**
     * Reserve ban submit.
     *
     * @param id id
     * @param tenKhach tenKhach
     * @param sdt sdt
     * @param ngayGio ngayGio
     * @return result
     */
    @ResponseBody
    public String reserveBanSubmit(@PathVariable("id") Long id,
                                   @RequestParam("tenKhach") String tenKhach,
                                   @RequestParam("sdt") String sdt,
                                   @RequestParam(value = "ngayGio", required = false) String ngayGioStr) {
        try {
            Map<String, String> fieldErrors = new LinkedHashMap<>();
            String ten = tenKhach == null ? "" : tenKhach.trim();
            String phone = sdt == null ? "" : sdt.trim();
            LocalDateTime ngayGio = null;

            if (ten.isEmpty()) {
                fieldErrors.put("tenKhach", "Tên khách bắt buộc");
            }
            if (phone.isEmpty()) {
                fieldErrors.put("sdt", "Số điện thoại bắt buộc");
            } else if (!phone.matches("\\d{9,15}")) {
                fieldErrors.put("sdt", "Số điện thoại chỉ được nhập số (9-15 chữ số)");
            }
            if (ngayGioStr == null || ngayGioStr.trim().isEmpty()) {
                fieldErrors.put("ngayGio", "Ngày giờ đến bắt buộc");
            } else {
                try {
                    ngayGio = LocalDateTime.parse(ngayGioStr);
                    LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
                    if (ngayGio.isBefore(now)) {
                        fieldErrors.put("ngayGio", "Ngày giờ đến không được trước thời điểm hiện tại");
                    }
                } catch (DateTimeParseException ex) {
                    fieldErrors.put("ngayGio", "Ngày giờ đến không hợp lệ");
                }
            }

            if (!fieldErrors.isEmpty()) {
                String message = fieldErrors.entrySet().stream()
                        .map(entry -> entry.getKey() + "=" + entry.getValue())
                        .reduce((a, b) -> a + "|" + b)
                        .orElse("");
                log.warn("Reserve ban validation failed banId={} tenLen={} phoneMasked={} errors={}",
                        id, ten.length(), maskPhone(phone), message);
                return "ERROR_FIELDS:" + message;
            }

            salesService.reserveTable(id, ten, phone, ngayGio);
            log.info("Reserve ban submit ok banId={} tenLen={} phoneMasked={} ngayGio={}",
                    id, ten.length(), maskPhone(phone), ngayGio);
            return "OK";
        } catch (IllegalArgumentException ex) {
            log.warn("Reserve ban submit failed banId={} message={}", id, ex.getMessage(), ex);
            if (ex.getMessage() != null && ex.getMessage().contains("Giờ đến")) {
                return "ERROR_FIELDS:ngayGio=" + ex.getMessage();
            }
            return "ERROR:" + ex.getMessage();
        } catch (IllegalStateException ex) {
            log.warn("Reserve ban submit conflict banId={} message={}", id, ex.getMessage(), ex);
            if (ex.getMessage() != null && ex.getMessage().contains("khung giờ")) {
                return "ERROR_FIELDS:ngayGio=" + ex.getMessage();
            }
            return "ERROR:" + ex.getMessage();
        } catch (Exception e) {
            log.error("Reserve ban submit error banId={} message={}", id, e.getMessage(), e);
            return "ERROR:" + e.getMessage();
        }
    }

    
    /**
     * Move ban fragment.
     *
     * @param fromBanId fromBanId
     * @param model model
     * @return result
     */
    @GetMapping("/ban/{fromBanId}/move")
    public String moveBanFragment(@PathVariable("fromBanId") Long fromBanId, Model model) {
        model.addAttribute("fromBanId", fromBanId);
        List<Ban> empties = salesService.findEmptyTables();
        
        empties.removeIf(b -> b.getMaBan().equals(fromBanId));
        model.addAttribute("available", empties);
        log.info("Move ban fragment fromBanId={} availableTables={}", fromBanId, empties.size());
        return "sales/fragments/move-ban :: content";
    }

    
    /**
     * Move table json.
     *
     * @param payload payload
     * @return result
     */
    @PostMapping("/move")
    /**
     * Move table json.
     *
     * @param payload payload
     * @return result
     */
    @ResponseBody
    public ResponseEntity<String> moveTableJson(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null || !payload.containsKey("fromBanId") || !payload.containsKey("toBanId")) {
                log.warn("Move table payload invalid");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing fromBanId or toBanId");
            }
            Long fromBanId = ((Number) payload.get("fromBanId")).longValue();
            Long toBanId = ((Number) payload.get("toBanId")).longValue();

            salesService.moveTable(fromBanId, toBanId);
            log.info("Move table ok fromBanId={} toBanId={}", fromBanId, toBanId);
            return ResponseEntity.ok("OK");
        } catch (ClassCastException ex) {
            log.warn("Move table payload invalid types");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id types");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.warn("Move table failed message={}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ERROR:" + ex.getMessage());
        } catch (Exception ex) {
            log.error("Move table error message={}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR:" + ex.getMessage());
        }
    }

    
    /**
     * Merge ban fragment.
     *
     * @param targetBanId targetBanId
     * @param model model
     * @return result
     */
    @GetMapping("/ban/{targetBanId}/merge")
    public String mergeBanFragment(@PathVariable("targetBanId") Long targetBanId, Model model) {
        model.addAttribute("targetBanId", targetBanId);
        List<Ban> candidates = salesService.findMergeCandidates(targetBanId);
        model.addAttribute("candidates", candidates);
        log.info("Merge ban fragment targetBanId={} candidates={}", targetBanId, candidates.size());
        return "sales/fragments/merge-ban :: content";
    }

    
    /**
     * Split ban fragment.
     *
     * @param fromBanId fromBanId
     * @param model model
     * @return result
     */
    @GetMapping("/ban/{fromBanId}/split")
    public String splitBanFragment(@PathVariable("fromBanId") Long fromBanId, Model model) {
        Optional<HoaDon> hdOpt = salesService.findUnpaidInvoiceByTable(fromBanId);
        if (hdOpt.isEmpty()) {
            log.warn("Split ban fragment failed fromBanId={} reason=no invoice", fromBanId);
            model.addAttribute("error", "Bàn không có hóa đơn để tách");
            return "sales/fragments/view-ban :: content";
        }
        model.addAttribute("fromBanId", fromBanId);
        model.addAttribute("hoaDon", hdOpt.get());
        List<Ban> empties = salesService.findEmptyTables();
        model.addAttribute("empties", empties);
        log.info("Split ban fragment fromBanId={} empties={}", fromBanId, empties.size());
        return "sales/fragments/split-ban :: content";
    }

    
    /**
     * Split table.
     *
     * @param fromBanId fromBanId
     * @param toBanId toBanId
     * @param params params
     * @return result
     */
    @PostMapping("/ban/{fromBanId}/split")
    /**
     * Split table.
     *
     * @param fromBanId fromBanId
     * @param toBanId toBanId
     * @param params params
     * @return result
     */
    @ResponseBody
    public String splitTable(@PathVariable("fromBanId") Long fromBanId,
                             @RequestParam("toBanId") Long toBanId,
                             @RequestParam Map<String, String> params) {
        try {
            Map<Long, Integer> itemQuantities = new HashMap<>();
            int totalQty = 0;
            for (Map.Entry<String, String> entry : params.entrySet()) {
                if (entry.getKey().startsWith("split_qty_")) {
                    Long itemId = Long.parseLong(entry.getKey().substring(10));
                    Integer qty = Integer.parseInt(entry.getValue());
                    if (qty > 0) {
                        itemQuantities.put(itemId, qty);
                        totalQty += qty;
                    }
                }
            }
            log.info("Split table request fromBanId={} toBanId={} items={} totalQty={}",
                    fromBanId, toBanId, itemQuantities.size(), totalQty);
            salesService.splitTable(fromBanId, toBanId, itemQuantities);
            log.info("Split table ok fromBanId={} toBanId={}", fromBanId, toBanId);
            return "OK";
        } catch (Exception e) {
            log.error("Split table failed fromBanId={} toBanId={} message={}",
                    fromBanId, toBanId, e.getMessage(), e);
            return "ERROR:" + e.getMessage();
        }
    }

    
    /**
     * Cancel reservation.
     *
     * @param banId banId
     * @return result
     */
    @PostMapping("/ban/{banId}/cancel-reservation")
    /**
     * Cancel reservation.
     *
     * @param banId banId
     * @return result
     */
    @ResponseBody
    public ResponseEntity<String> cancelReservation(@PathVariable("banId") Long banId) {
        try {
            salesService.cancelReservation(banId);
            log.info("Cancel reservation ok banId={}", banId);
            return ResponseEntity.ok("OK");
        } catch (IllegalArgumentException ex) {
            log.warn("Cancel reservation failed banId={} message={}", banId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("ERROR:" + ex.getMessage());
        } catch (IllegalStateException ex) {
            log.warn("Cancel reservation conflict banId={} message={}", banId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ERROR:" + ex.getMessage());
        } catch (Exception ex) {
            log.error("Cancel reservation error banId={} message={}", banId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR:" + ex.getMessage());
        }
    }

    
    /**
     * Split table json.
     *
     * @param fromBanId fromBanId
     * @param payload payload
     * @return result
     */
    @PostMapping("/{fromBanId}/split")
    /**
     * Split table json.
     *
     * @param fromBanId fromBanId
     * @param payload payload
     * @return result
     */
    @ResponseBody
    public ResponseEntity<String> splitTableJson(@PathVariable("fromBanId") Long fromBanId, @RequestBody Map<String, Object> payload) {
        try {
            if (payload == null || !payload.containsKey("toBanId") || !payload.containsKey("items")) {
                log.warn("Split table json invalid payload fromBanId={}", fromBanId);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing toBanId or items");
            }
            Long toBanId = ((Number) payload.get("toBanId")).longValue();
            List<?> items = (List<?>) payload.get("items");
            Map<Long, Integer> itemQuantities = new HashMap<>();
            int totalQty = 0;
            for (Object o : items) {
                if (o instanceof Map) {
                    Map m = (Map) o;
                    Long id = ((Number) m.get("thucDonId")).longValue();
                    Integer qty = ((Number) m.get("soLuong")).intValue();
                    if (qty > 0) {
                        itemQuantities.put(id, qty);
                        totalQty += qty;
                    }
                }
            }
            log.info("Split table json request fromBanId={} toBanId={} items={} totalQty={}",
                    fromBanId, toBanId, itemQuantities.size(), totalQty);
            salesService.splitTable(fromBanId, toBanId, itemQuantities);
            log.info("Split table json ok fromBanId={} toBanId={}", fromBanId, toBanId);
            return ResponseEntity.ok("OK");
        } catch (ClassCastException ex) {
            log.warn("Split table json invalid payload types fromBanId={}", fromBanId);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid payload");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.warn("Split table json failed fromBanId={} message={}", fromBanId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ERROR:" + ex.getMessage());
        } catch (Exception ex) {
            log.error("Split table json error fromBanId={} message={}", fromBanId, ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR:" + ex.getMessage());
        }
    }

    
    /**
     * Merge table.
     *
     * @param targetBanId targetBanId
     * @param sourceBanId sourceBanId
     * @return result
     */
    @PostMapping("/ban/{targetBanId}/merge")
    /**
     * Merge table.
     *
     * @param targetBanId targetBanId
     * @param sourceBanId sourceBanId
     * @return result
     */
    @ResponseBody
    public String mergeTable(@PathVariable Long targetBanId, @RequestParam("sourceBanId") Long sourceBanId) {
        try {
            log.info("Merge table request targetBanId={} sourceBanId={}", targetBanId, sourceBanId);
            salesService.mergeTables(targetBanId, sourceBanId);
            log.info("Merge table ok targetBanId={} sourceBanId={}", targetBanId, sourceBanId);
            return "OK";
        } catch (Exception e) {
            log.error("Merge table failed targetBanId={} sourceBanId={} message={}",
                    targetBanId, sourceBanId, e.getMessage(), e);
            return "ERROR:" + e.getMessage();
        }
    }

    
    /**
     * Merge table json.
     *
     * @param payload payload
     * @return result
     */
    @PostMapping("/merge")
    /**
     * Merge table json.
     *
     * @param payload payload
     * @return result
     */
    @ResponseBody
    public ResponseEntity<String> mergeTableJson(@RequestBody Map<String, Object> payload) {
        try {
            if (payload == null || !payload.containsKey("targetBanId") || !payload.containsKey("sourceBanId")) {
                log.warn("Merge table json invalid payload");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Missing targetBanId or sourceBanId");
            }
            Long targetBanId = ((Number) payload.get("targetBanId")).longValue();
            Long sourceBanId = ((Number) payload.get("sourceBanId")).longValue();

            log.info("Merge table json request targetBanId={} sourceBanId={}", targetBanId, sourceBanId);
            salesService.mergeTables(targetBanId, sourceBanId);
            log.info("Merge table json ok targetBanId={} sourceBanId={}", targetBanId, sourceBanId);
            return ResponseEntity.ok("OK");
        } catch (ClassCastException ex) {
            log.warn("Merge table json invalid id types");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid id types");
        } catch (IllegalArgumentException | IllegalStateException ex) {
            log.warn("Merge table json failed message={}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.CONFLICT).body("ERROR:" + ex.getMessage());
        } catch (Exception ex) {
            log.error("Merge table json error message={}", ex.getMessage(), ex);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("ERROR:" + ex.getMessage());
        }
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.isBlank()) {
            return "";
        }
        String normalized = phone.trim();
        int len = normalized.length();
        if (len <= 3) {
            return "***";
        }
        return "***" + normalized.substring(len - 3);
    }
}
