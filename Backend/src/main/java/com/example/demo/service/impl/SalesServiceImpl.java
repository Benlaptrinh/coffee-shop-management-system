package com.example.demo.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import com.example.demo.entity.Ban;
import com.example.demo.entity.ChiTietDatBan;
import com.example.demo.entity.ChiTietHoaDon;
import com.example.demo.entity.HoaDon;
import com.example.demo.entity.NhanVien;
import com.example.demo.entity.TaiKhoan;
import com.example.demo.entity.ThucDon;
import com.example.demo.enums.TinhTrangBan;
import com.example.demo.enums.TrangThaiHoaDon;
import com.example.demo.repository.BanRepository;
import com.example.demo.repository.ChiTietDatBanRepository;
import com.example.demo.repository.ChiTietHoaDonRepository;
import com.example.demo.repository.HoaDonRepository;
import com.example.demo.repository.NhanVienRepository;
import com.example.demo.repository.TaiKhoanRepository;
import com.example.demo.repository.ThucDonRepository;
import com.example.demo.realtime.WsEventPublisher;
import com.example.demo.service.SalesService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Sales.
 */
@Service
@Transactional
public class SalesServiceImpl implements SalesService {

    private static final Logger log = LoggerFactory.getLogger(SalesServiceImpl.class);

    private final BanRepository banRepository;
    private final HoaDonRepository hoaDonRepository;
    private final ThucDonRepository thucDonRepository;
    private final ChiTietHoaDonRepository chiTietHoaDonRepository;
    private final ChiTietDatBanRepository chiTietDatBanRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    private final NhanVienRepository nhanVienRepository;
    private final WsEventPublisher wsEventPublisher;

    /**
     * Creates SalesServiceImpl.
     *
     * @param banRepository banRepository
     * @param hoaDonRepository hoaDonRepository
     * @param thucDonRepository thucDonRepository
     * @param chiTietHoaDonRepository chiTietHoaDonRepository
     * @param chiTietDatBanRepository chiTietDatBanRepository
     * @param taiKhoanRepository taiKhoanRepository
     * @param nhanVienRepository nhanVienRepository
     */
    public SalesServiceImpl(BanRepository banRepository,
                            HoaDonRepository hoaDonRepository,
                            ThucDonRepository thucDonRepository,
                            ChiTietHoaDonRepository chiTietHoaDonRepository,
                            ChiTietDatBanRepository chiTietDatBanRepository,
                            TaiKhoanRepository taiKhoanRepository,
                            NhanVienRepository nhanVienRepository,
                            WsEventPublisher wsEventPublisher) {
        this.banRepository = banRepository;
        this.hoaDonRepository = hoaDonRepository;
        this.thucDonRepository = thucDonRepository;
        this.chiTietHoaDonRepository = chiTietHoaDonRepository;
        this.chiTietDatBanRepository = chiTietDatBanRepository;
        this.taiKhoanRepository = taiKhoanRepository;
        this.nhanVienRepository = nhanVienRepository;
        this.wsEventPublisher = wsEventPublisher;
    }

    /**
     * Find all tables.
     *
     * @return result
     */
    @Override
    public List<Ban> findAllTables() {
        // ensure expired reservations are cleaned up before returning table list
        cleanupExpiredReservations();
        List<Ban> tables = banRepository.findAll();
        log.info("findAllTables count={}", tables.size());
        return tables;
    }

    /**
     * Find table by id.
     *
     * @param tableId tableId
     * @return result
     */
    @Override
    public Optional<Ban> findTableById(long tableId) {
        Optional<Ban> banOpt = banRepository.findById(tableId);
        log.info("findTableById id={} found={}", tableId, banOpt.isPresent());
        return banOpt;
    }

    /**
     * Find latest reservation for table.
     *
     * @param banId banId
     * @return result
     */
    @Override
    public Optional<ChiTietDatBan> findLatestReservation(long banId) {
        // cleanup expired reservations first so latest reservation reflects current state
        cleanupExpiredReservations();
        LocalDateTime now = LocalDateTime.now().minusMinutes(1);
        Optional<ChiTietDatBan> result = banRepository.findById(banId)
                .flatMap(ban -> {
                    Optional<ChiTietDatBan> upcoming = chiTietDatBanRepository.findTopByBanAndId_NgayGioDatAfterOrderById_NgayGioDatAsc(ban, now);
                    if (upcoming.isPresent()) {
                        return upcoming;
                    }
                    return chiTietDatBanRepository.findTopByBanOrderById_NgayGioDatDesc(ban);
                });
        log.info("findLatestReservation banId={} found={} ngayGioDat={}",
                banId, result.isPresent(), result.map(ChiTietDatBan::getNgayGioDat).orElse(null));
        return result;
    }

    /**
     * Find unpaid invoice by table.
     *
     * @param tableId tableId
     * @return result
     */
    @Override
    public Optional<HoaDon> findUnpaidInvoiceByTable(long tableId) {
        Optional<HoaDon> result = hoaDonRepository.findChuaThanhToanByBan(tableId);
        log.info("findUnpaidInvoiceByTable tableId={} found={}", tableId, result.isPresent());
        return result;
    }

    /**
     * Find menu items.
     *
     * @return result
     */
    @Override
    @Cacheable(cacheNames = "menu")
    public List<ThucDon> findMenuItems() {
        List<ThucDon> items = thucDonRepository.findAll();
        items.sort(java.util.Comparator.comparing(
                item -> item.getTenMon() == null ? "" : item.getTenMon(),
                String.CASE_INSENSITIVE_ORDER
        ));
        log.info("findMenuItems count={}", items.size());
        return items;
    }

    /**
     * Add item to invoice.
     *
     * @param tableId tableId
     * @param itemId itemId
     * @param quantity quantity
     */
    @Override
    public void addItemToInvoice(long tableId, long itemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Số lượng phải > 0");
        }
        int requestedQty = quantity;
        HoaDon hd = getOrCreateInvoice(tableId, true);
        assignCurrentNhanVien(hd);
        ThucDon item = thucDonRepository.findById(itemId).orElseThrow();

        ChiTietHoaDon existing = findExistingDetail(hd, item.getMaThucDon());
        boolean existed = existing != null;
        int finalQty;
        if (existing != null) {
            int newQty = (existing.getSoLuong() == null ? 0 : existing.getSoLuong()) + requestedQty;
            existing.setSoLuong(newQty);
            existing.setGiaTaiThoiDiemBan(item.getGiaHienTai());
            existing.setThanhTien(item.getGiaHienTai().multiply(new BigDecimal(newQty)));
            chiTietHoaDonRepository.save(existing);
            finalQty = newQty;
        } else {
            ChiTietHoaDon ct = new ChiTietHoaDon();
            ct.setHoaDon(hd);
            ct.setThucDon(item);
            int qty = requestedQty;
            ct.setSoLuong(qty);
            ct.setGiaTaiThoiDiemBan(item.getGiaHienTai());
            ct.setThanhTien(item.getGiaHienTai().multiply(new BigDecimal(qty)));
            chiTietHoaDonRepository.save(ct);
            finalQty = qty;

            if (hd.getChiTietHoaDons() == null) {
                hd.setChiTietHoaDons(new ArrayList<>());
            }
            hd.getChiTietHoaDons().add(ct);
        }

        updateInvoiceTotal(hd);
        wsEventPublisher.publishInvoiceEvent("INVOICE_UPDATED", hd.getMaHoaDon(), tableId);
        wsEventPublisher.publishTableEvent("TABLE_UPDATED", tableId);
        log.info("Add item to invoice tableId={} invoiceId={} itemId={} requestedQty={} finalQty={} existed={}",
                tableId, hd.getMaHoaDon(), itemId, requestedQty, finalQty, existed);
    }

    /**
     * Pay invoice.
     *
     * @param tableId tableId
     * @param tienKhach tienKhach
     * @param releaseTable releaseTable
     */
    @Override
    public void payInvoice(long tableId, BigDecimal tienKhach, boolean releaseTable) {
        Optional<HoaDon> hdOpt = hoaDonRepository.findChuaThanhToanByBan(tableId);
        if (hdOpt.isEmpty()) {
            log.warn("Pay invoice failed tableId={} reason=no unpaid invoice", tableId);
            return;
        }
        HoaDon hd = hdOpt.get();
        assignCurrentNhanVien(hd);
        if (hd.getTenKhachDat() == null && hd.getBan() != null) {
            chiTietDatBanRepository.findTopByBanOrderById_NgayGioDatDesc(hd.getBan())
                .ifPresent(res -> {
                    hd.setTenKhachDat(res.getTenKhach());
                    hd.setSdtKhachDat(res.getSdt());
                });
        }

        BigDecimal total = calculateTotal(hd);
        hd.setTongTien(total);
        validatePayment(tienKhach, total);

        log.info("Pay invoice tableId={} invoiceId={} total={} tienKhach={} releaseTable={}",
                tableId, hd.getMaHoaDon(), total, tienKhach, releaseTable);
        hd.setTrangThai(TrangThaiHoaDon.DA_THANH_TOAN);
        hd.setNgayThanhToan(LocalDateTime.now());
        hoaDonRepository.save(hd);

        if (releaseTable && hd.getBan() != null) {
            Ban ban = hd.getBan();
            chiTietDatBanRepository.deleteByBan(ban);
            ban.setTinhTrang(TinhTrangBan.TRONG);
            banRepository.save(ban);
            log.info("Released table and cleared reservations after payment tableId={}", tableId);
        }
        wsEventPublisher.publishInvoiceEvent("INVOICE_PAID", hd.getMaHoaDon(), tableId);
        wsEventPublisher.publishTableEvent("TABLE_UPDATED", tableId);
    }

    /**
     * Reserve table.
     *
     * @param banId banId
     * @param tenKhach tenKhach
     * @param sdt sdt
     * @param ngayGioDat ngayGioDat
     */
    @Override
    public void reserveTable(long banId, String tenKhach, String sdt, LocalDateTime ngayGioDat) {
        log.info("Reserve table request banId={} ngayGioDat={} tenKhachLen={} sdtMasked={}",
                banId,
                ngayGioDat,
                tenKhach == null ? 0 : tenKhach.trim().length(),
                maskPhone(sdt));
        Ban ban = banRepository.findById(banId).orElseThrow();
        if (ban.getTinhTrang() == TinhTrangBan.DANG_SU_DUNG) {
            throw new IllegalStateException("Bàn đang phục vụ, không thể đặt");
        }
        LocalDateTime now = LocalDateTime.now().withSecond(0).withNano(0);
        if (ngayGioDat.isBefore(now)) {
            throw new IllegalArgumentException("Giờ đến không được trước thời điểm hiện tại");
        }
        LocalDateTime windowStart = ngayGioDat.minusHours(1);
        LocalDateTime windowEnd = ngayGioDat.plusHours(1);
        if (chiTietDatBanRepository.existsOverlappingReservation(ban, windowStart, windowEnd)) {
            throw new IllegalStateException("Bàn đã có khách đặt trong khung giờ 1 tiếng này");
        }
        ChiTietDatBan d = new ChiTietDatBan();
        d.setBan(ban);
        d.setTenKhach(tenKhach);
        d.setSdt(sdt);
        d.setNgayGioDat(ngayGioDat);

        NhanVien nv = getOrCreateCurrentNhanVien();
        d.setNhanVien(nv);
        chiTietDatBanRepository.save(d);
        ban.setTinhTrang(TinhTrangBan.DA_DAT);
        banRepository.save(ban);
        wsEventPublisher.publishTableEvent("RESERVATION_CREATED", banId);
        log.info("Reserve table ok banId={} ngayGioDat={} nhanVienId={}",
                banId, ngayGioDat, nv.getMaNhanVien());
    }

    /**
     * Save selected menu.
     *
     * @param banId banId
     * @param params params
     */
    @Override
    public void saveSelectedMenu(long banId, Map<String, String> params) {
        HoaDon hd = getOrCreateInvoice(banId, false);
        assignCurrentNhanVien(hd);

        List<ThucDon> menu = thucDonRepository.findAll();

        if (hd.getChiTietHoaDons() == null) {
            hd.setChiTietHoaDons(new ArrayList<>());
        }

        
        Map<Long, ChiTietHoaDon> existingMap = new HashMap<>();
        for (ChiTietHoaDon ct : new ArrayList<>(hd.getChiTietHoaDons())) {
            if (ct.getThucDon() != null && ct.getThucDon().getMaThucDon() != null) {
                existingMap.put(ct.getThucDon().getMaThucDon(), ct);
            }
        }

        int selectedItems = 0;
        int totalQty = 0;
        for (ThucDon mon : menu) {
            String key = "qty_" + (mon.getMaThucDon() == null ? "" : mon.getMaThucDon());
            int qty = 0;
            if (params.containsKey(key)) {
                try { qty = Integer.parseInt(params.get(key)); } catch (Exception ignored) {}
            }
            ChiTietHoaDon existing = existingMap.get(mon.getMaThucDon());

            if (qty > 0) {
                selectedItems++;
                totalQty += qty;
                if (existing != null) {
                    existing.setSoLuong(qty);
                    existing.setGiaTaiThoiDiemBan(mon.getGiaHienTai());
                    existing.setThanhTien(mon.getGiaHienTai().multiply(new BigDecimal(qty)));
                } else {
                    ChiTietHoaDon ct = new ChiTietHoaDon();
                    ct.setHoaDon(hd);
                    ct.setThucDon(mon);
                    ct.setSoLuong(qty);
                    ct.setGiaTaiThoiDiemBan(mon.getGiaHienTai());
                    ct.setThanhTien(mon.getGiaHienTai().multiply(new BigDecimal(qty)));
                    hd.getChiTietHoaDons().add(ct);
                }
            } else {
                if (existing != null) {
                    
                    hd.getChiTietHoaDons().remove(existing);
                }
            }
        }

        updateInvoiceTotal(hd);

        boolean hasItems = hd.getChiTietHoaDons() != null
                && hd.getChiTietHoaDons().stream()
                    .anyMatch(ct -> ct.getSoLuong() != null && ct.getSoLuong() > 0);
        if (hd.getBan() != null) {
            Ban b = hd.getBan();
            if (hasItems) {
                b.setTinhTrang(TinhTrangBan.DANG_SU_DUNG);
            } else {
                boolean reserved = chiTietDatBanRepository.existsByBan(b);
                b.setTinhTrang(reserved ? TinhTrangBan.DA_DAT : TinhTrangBan.TRONG);
            }
            banRepository.save(b);
        }
        wsEventPublisher.publishInvoiceEvent("INVOICE_UPDATED", hd.getMaHoaDon(), banId);
        wsEventPublisher.publishTableEvent("TABLE_UPDATED", banId);
        log.info("Save selected menu banId={} invoiceId={} itemsSelected={} totalQty={} hasItems={}",
                banId, hd.getMaHoaDon(), selectedItems, totalQty, hasItems);
    }

    /**
     * Cancel invoice.
     *
     * @param banId banId
     */
    @Override
    public void cancelInvoice(long banId) {
        log.info("Cancel invoice start banId={}", banId);
        Optional<HoaDon> hdOpt = hoaDonRepository.findChuaThanhToanByBan(banId);
        if (hdOpt.isEmpty()) {
            throw new IllegalStateException("Không có hóa đơn CHƯA THANH TOÁN cho bàn");
        }
        HoaDon hd = hdOpt.get();
        if (hd.getTrangThai() != TrangThaiHoaDon.MOI_TAO) {
            throw new IllegalStateException("Chỉ hủy hóa đơn ở trạng thái MOI_TAO");
        }

        Long hoaDonId = hd.getMaHoaDon();
        
        chiTietHoaDonRepository.deleteByHoaDonId(hoaDonId);
        
        hoaDonRepository.delete(hd);
        
        Ban b = banRepository.findById(banId).orElse(null);
        if (b != null) {
            chiTietDatBanRepository.deleteByBan(b);
            b.setTinhTrang(TinhTrangBan.TRONG);
            banRepository.save(b);
        }
        wsEventPublisher.publishTableEvent("TABLE_UPDATED", banId);
        log.info("Cancel invoice ok banId={} invoiceId={}", banId, hoaDonId);
    }

    /**
     * Find invoice by id.
     *
     * @param id id
     * @return result
     */
    @Override
    public Optional<HoaDon> findInvoiceById(long id) {
        Optional<HoaDon> result = hoaDonRepository.findById(id);
        log.info("findInvoiceById id={} found={}", id, result.isPresent());
        return result;
    }

    /**
     * Move table.
     *
     * @param fromBanId fromBanId
     * @param toBanId toBanId
     */
    @Override
    public void moveTable(long fromBanId, long toBanId) {
        log.info("Move table start fromBanId={} toBanId={}", fromBanId, toBanId);
        if (fromBanId == toBanId) {
            throw new IllegalArgumentException("Không thể chuyển bàn với chính nó");
        }

        Ban fromBan = banRepository.findById(fromBanId).orElseThrow(() -> new IllegalArgumentException("Bàn nguồn không tồn tại"));
        Ban toBan = banRepository.findById(toBanId).orElseThrow(() -> new IllegalArgumentException("Bàn đích không tồn tại"));

        if (fromBan.getTinhTrang() != TinhTrangBan.DANG_SU_DUNG) {
            throw new IllegalStateException("Bàn nguồn không đang sử dụng");
        }
        if (toBan.getTinhTrang() != TinhTrangBan.TRONG) {
            throw new IllegalStateException("Bàn đích không trống");
        }

        Optional<HoaDon> hdOpt = hoaDonRepository.findChuaThanhToanByBan(fromBanId);
        if (hdOpt.isEmpty()) {
            throw new IllegalStateException("Bàn nguồn không có hóa đơn MOI_TAO");
        }
        HoaDon hd = hdOpt.get();

        
        hd.setBan(toBan);
        hoaDonRepository.save(hd);

        List<ChiTietDatBan> reservations = chiTietDatBanRepository.findByBan(fromBan);
        List<ChiTietDatBan> reservationsToMove = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (ChiTietDatBan res : reservations) {
            LocalDateTime time = res.getNgayGioDat();
            if (time != null && !now.isBefore(time.minusMinutes(5))) {
                reservationsToMove.add(res);
            }
        }
        int movedReservations = reservationsToMove.size();
        if (!reservationsToMove.isEmpty()) {
            for (ChiTietDatBan res : reservationsToMove) {
                LocalDateTime time = res.getNgayGioDat();
                if (time != null) {
                    LocalDateTime overlapStart = time.minusHours(1);
                    LocalDateTime overlapEnd = time.plusHours(1);
                    if (chiTietDatBanRepository.existsOverlappingReservation(toBan, overlapStart, overlapEnd)) {
                        throw new IllegalStateException("Bàn đích đã có khách đặt trong khung giờ này");
                    }
                }
            }
            for (ChiTietDatBan res : reservationsToMove) {
                ChiTietDatBan moved = new ChiTietDatBan();
                moved.setBan(toBan);
                moved.setTenKhach(res.getTenKhach());
                moved.setSdt(res.getSdt());
                moved.setNhanVien(res.getNhanVien());
                moved.setNgayGioDat(res.getNgayGioDat());
                chiTietDatBanRepository.save(moved);
            }
            chiTietDatBanRepository.deleteAll(reservationsToMove);
        }

        
        fromBan.setTinhTrang(chiTietDatBanRepository.existsByBan(fromBan) ? TinhTrangBan.DA_DAT : TinhTrangBan.TRONG);
        toBan.setTinhTrang(TinhTrangBan.DANG_SU_DUNG);
        banRepository.save(fromBan);
        banRepository.save(toBan);
        wsEventPublisher.publishTableEvent("TABLE_MOVED", fromBanId);
        wsEventPublisher.publishTableEvent("TABLE_MOVED", toBanId);
        log.info("Move table ok fromBanId={} toBanId={} invoiceId={} reservationsMoved={}",
                fromBanId, toBanId, hd.getMaHoaDon(), movedReservations);
    }

    /**
     * Find empty tables.
     *
     * @return result
     */
    @Override
    public List<Ban> findEmptyTables() {
        List<Ban> empties = banRepository.findAll().stream()
                .filter(b -> b.getTinhTrang() == TinhTrangBan.TRONG)
                .collect(Collectors.toList());
        log.info("findEmptyTables count={}", empties.size());
        return empties;
    }

    /**
     * Find merge candidates.
     *
     * @param excludeBanId excludeBanId
     * @return result
     */
    @Override
    public List<Ban> findMergeCandidates(long excludeBanId) {
        List<Ban> candidates = banRepository.findAll().stream()
                .filter(b -> b.getTinhTrang() == TinhTrangBan.DANG_SU_DUNG)
                .filter(b -> !b.getMaBan().equals(excludeBanId))
                .filter(b -> hoaDonRepository.findChuaThanhToanByBan(b.getMaBan()).isPresent())
                .collect(Collectors.toList());
        log.info("findMergeCandidates excludeBanId={} count={}", excludeBanId, candidates.size());
        return candidates;
    }

    /**
     * Merge tables.
     *
     * @param targetBanId targetBanId
     * @param sourceBanId sourceBanId
     */
    @Override
    public void mergeTables(long targetBanId, long sourceBanId) {
        log.info("Merge tables start targetBanId={} sourceBanId={}", targetBanId, sourceBanId);
        if (targetBanId == sourceBanId) {
            throw new IllegalArgumentException("Không thể gộp bàn vào chính nó");
        }

        Ban targetBan = banRepository.findById(targetBanId).orElseThrow(() -> new IllegalArgumentException("Bàn đích không tồn tại"));
        Ban sourceBan = banRepository.findById(sourceBanId).orElseThrow(() -> new IllegalArgumentException("Bàn nguồn không tồn tại"));

        if (targetBan.getTinhTrang() != TinhTrangBan.DANG_SU_DUNG) {
            throw new IllegalStateException("Bàn đích không đang sử dụng");
        }
        if (sourceBan.getTinhTrang() != TinhTrangBan.DANG_SU_DUNG) {
            throw new IllegalStateException("Bàn nguồn không đang sử dụng");
        }

        Optional<HoaDon> targetHdOpt = hoaDonRepository.findChuaThanhToanByBan(targetBanId);
        Optional<HoaDon> sourceHdOpt = hoaDonRepository.findChuaThanhToanByBan(sourceBanId);
        if (targetHdOpt.isEmpty()) {
            throw new IllegalStateException("Bàn đích không có hóa đơn MOI_TAO");
        }
        if (sourceHdOpt.isEmpty()) {
            throw new IllegalStateException("Bàn nguồn không có hóa đơn MOI_TAO");
        }

        HoaDon targetHd = targetHdOpt.get();
        HoaDon sourceHd = sourceHdOpt.get();

        int sourceItemCount = 0;
        int sourceTotalQty = 0;
        if (sourceHd.getChiTietHoaDons() != null) {
            for (ChiTietHoaDon ct : sourceHd.getChiTietHoaDons()) {
                sourceItemCount++;
                if (ct.getSoLuong() != null) {
                    sourceTotalQty += ct.getSoLuong();
                }
            }
        }
        
        if (sourceHd.getChiTietHoaDons() != null) {
            for (ChiTietHoaDon srcCt : sourceHd.getChiTietHoaDons()) {
                Long thucDonId = srcCt.getThucDon().getMaThucDon();
                ChiTietHoaDon existing = targetHd.getChiTietHoaDons() == null ? null :
                        targetHd.getChiTietHoaDons().stream()
                                .filter(ct -> ct.getThucDon().getMaThucDon().equals(thucDonId))
                                .findFirst().orElse(null);
                if (existing != null) {
                    int newQty = (existing.getSoLuong() == null ? 0 : existing.getSoLuong()) + (srcCt.getSoLuong() == null ? 0 : srcCt.getSoLuong());
                    existing.setSoLuong(newQty);
                    existing.setThanhTien(existing.getGiaTaiThoiDiemBan().multiply(new BigDecimal(newQty)));
                    chiTietHoaDonRepository.save(existing);
                } else {
                    ChiTietHoaDon newCt = new ChiTietHoaDon();
                    newCt.setHoaDon(targetHd);
                    newCt.setThucDon(srcCt.getThucDon());
                    newCt.setSoLuong(srcCt.getSoLuong());
                    newCt.setGiaTaiThoiDiemBan(srcCt.getGiaTaiThoiDiemBan());
                    newCt.setThanhTien(srcCt.getThanhTien());
                    chiTietHoaDonRepository.save(newCt);
                    if (targetHd.getChiTietHoaDons() == null) targetHd.setChiTietHoaDons(new ArrayList<>());
                    targetHd.getChiTietHoaDons().add(newCt);
                }
            }
        }

        targetHd.setTrangThai(TrangThaiHoaDon.MOI_TAO);
        updateInvoiceTotal(targetHd);

        
        sourceHd.setTrangThai(TrangThaiHoaDon.DA_GOP);
        hoaDonRepository.save(sourceHd);

        
        sourceBan.setTinhTrang(TinhTrangBan.TRONG);
        banRepository.save(sourceBan);
        targetBan.setTinhTrang(TinhTrangBan.DANG_SU_DUNG);
        banRepository.save(targetBan);
        wsEventPublisher.publishTableEvent("TABLE_MERGED", targetBanId);
        wsEventPublisher.publishTableEvent("TABLE_MERGED", sourceBanId);
        log.info("Merge tables ok targetBanId={} sourceBanId={} targetInvoiceId={} sourceInvoiceId={} sourceItems={} sourceTotalQty={}",
                targetBanId, sourceBanId, targetHd.getMaHoaDon(), sourceHd.getMaHoaDon(), sourceItemCount, sourceTotalQty);
    }

    /**
     * Split table.
     *
     * @param fromBanId fromBanId
     * @param toBanId toBanId
     * @param itemQuantities itemQuantities
     */
    @Override
    public void splitTable(long fromBanId, long toBanId, Map<Long, Integer> itemQuantities) {
        int totalQty = 0;
        if (itemQuantities != null) {
            for (Integer qty : itemQuantities.values()) {
                if (qty != null) {
                    totalQty += qty;
                }
            }
        }
        log.info("Split table start fromBanId={} toBanId={} items={} totalQty={}",
                fromBanId, toBanId, itemQuantities == null ? 0 : itemQuantities.size(), totalQty);
        if (fromBanId == toBanId) {
            throw new IllegalArgumentException("Không thể tách bàn với chính nó");
        }

        Ban fromBan = banRepository.findById(fromBanId).orElseThrow(() -> new IllegalArgumentException("Bàn nguồn không tồn tại"));
        Ban toBan = banRepository.findById(toBanId).orElseThrow(() -> new IllegalArgumentException("Bàn đích không tồn tại"));

        if (fromBan.getTinhTrang() != TinhTrangBan.DANG_SU_DUNG) {
            throw new IllegalStateException("Bàn nguồn không đang sử dụng");
        }
        if (toBan.getTinhTrang() != TinhTrangBan.TRONG) {
            throw new IllegalStateException("Bàn đích phải trống");
        }

        Optional<HoaDon> fromHdOpt = hoaDonRepository.findChuaThanhToanByBan(fromBanId);
        if (fromHdOpt.isEmpty()) {
            throw new IllegalStateException("Bàn nguồn không có hóa đơn");
        }
        HoaDon fromHd = fromHdOpt.get();

        if (itemQuantities == null || itemQuantities.isEmpty()) {
            throw new IllegalArgumentException("Không có món để tách");
        }

        
        HoaDon toHd = new HoaDon();
        toHd.setBan(toBan);
        toHd.setNgayGioTao(LocalDateTime.now());
        toHd.setTrangThai(TrangThaiHoaDon.MOI_TAO);
        toHd.setChiTietHoaDons(new ArrayList<>());
        toHd = hoaDonRepository.save(toHd);

        Map<Long, ChiTietHoaDon> targetItemMap = new HashMap<>();
        if (toHd.getChiTietHoaDons() != null) {
            for (ChiTietHoaDon ct : toHd.getChiTietHoaDons()) {
                if (ct.getThucDon() != null && ct.getThucDon().getMaThucDon() != null) {
                    targetItemMap.put(ct.getThucDon().getMaThucDon(), ct);
                }
            }
        }

        BigDecimal fromTotal = BigDecimal.ZERO;
        BigDecimal toTotal = BigDecimal.ZERO;

        if (fromHd.getChiTietHoaDons() != null) {
            List<ChiTietHoaDon> itemsToRemove = new ArrayList<>();

            for (ChiTietHoaDon ct : fromHd.getChiTietHoaDons()) {
                Long itemId = ct.getThucDon().getMaThucDon();
                Integer splitQty = itemQuantities.getOrDefault(itemId, 0);

                if (splitQty > 0 && splitQty < ct.getSoLuong()) {
                    
                    int remainingQty = ct.getSoLuong() - splitQty;

                    
                    ct.setSoLuong(remainingQty);
                    ct.setThanhTien(ct.getGiaTaiThoiDiemBan().multiply(new BigDecimal(remainingQty)));
                    chiTietHoaDonRepository.save(ct);
                    fromTotal = fromTotal.add(ct.getThanhTien());

                    
                    ChiTietHoaDon existing = targetItemMap.get(itemId);
                    if (existing != null) {
                        int newQty = (existing.getSoLuong() == null ? 0 : existing.getSoLuong()) + splitQty;
                        existing.setSoLuong(newQty);
                        existing.setThanhTien(existing.getGiaTaiThoiDiemBan().multiply(new BigDecimal(newQty)));
                        chiTietHoaDonRepository.save(existing);
                        toTotal = toTotal.add(ct.getGiaTaiThoiDiemBan().multiply(new BigDecimal(splitQty)));
                    } else {
                        ChiTietHoaDon newCt = new ChiTietHoaDon();
                        newCt.setHoaDon(toHd);
                        newCt.setThucDon(ct.getThucDon());
                        newCt.setSoLuong(splitQty);
                        newCt.setGiaTaiThoiDiemBan(ct.getGiaTaiThoiDiemBan());
                        newCt.setThanhTien(ct.getGiaTaiThoiDiemBan().multiply(new BigDecimal(splitQty)));
                        ChiTietHoaDon saved = chiTietHoaDonRepository.save(newCt);
                        toHd.getChiTietHoaDons().add(saved);
                        targetItemMap.put(itemId, saved);
                        toTotal = toTotal.add(saved.getThanhTien());
                    }

                } else if (splitQty >= ct.getSoLuong()) {
                    
                    ChiTietHoaDon existing = targetItemMap.get(itemId);
                    if (existing != null) {
                        int newQty = (existing.getSoLuong() == null ? 0 : existing.getSoLuong()) + ct.getSoLuong();
                        existing.setSoLuong(newQty);
                        existing.setThanhTien(existing.getGiaTaiThoiDiemBan().multiply(new BigDecimal(newQty)));
                        chiTietHoaDonRepository.save(existing);
                        toTotal = toTotal.add(ct.getThanhTien());
                    } else {
                        ChiTietHoaDon newCt = new ChiTietHoaDon();
                        newCt.setHoaDon(toHd);
                        newCt.setThucDon(ct.getThucDon());
                        newCt.setSoLuong(ct.getSoLuong());
                        newCt.setGiaTaiThoiDiemBan(ct.getGiaTaiThoiDiemBan());
                        newCt.setThanhTien(ct.getThanhTien());
                        ChiTietHoaDon saved = chiTietHoaDonRepository.save(newCt);
                        toHd.getChiTietHoaDons().add(saved);
                        targetItemMap.put(itemId, saved);
                        toTotal = toTotal.add(saved.getThanhTien());
                    }
                    itemsToRemove.add(ct);
                } else {
                    
                    fromTotal = fromTotal.add(ct.getThanhTien());
                }
            }

            
            if (!itemsToRemove.isEmpty()) {
                fromHd.getChiTietHoaDons().removeAll(itemsToRemove);
                
                chiTietHoaDonRepository.deleteAll(itemsToRemove);
            }
        }

        boolean fromEmpty = fromHd.getChiTietHoaDons() == null || fromHd.getChiTietHoaDons().isEmpty();
        if (fromEmpty) {
            hoaDonRepository.delete(fromHd);

            List<ChiTietDatBan> reservationsToMove = chiTietDatBanRepository.findByBan(fromBan);
            if (!reservationsToMove.isEmpty()) {
                for (ChiTietDatBan res : reservationsToMove) {
                    LocalDateTime time = res.getNgayGioDat();
                    if (time != null) {
                        LocalDateTime overlapStart = time.minusHours(1);
                        LocalDateTime overlapEnd = time.plusHours(1);
                        if (chiTietDatBanRepository.existsOverlappingReservation(toBan, overlapStart, overlapEnd)) {
                            throw new IllegalStateException("Bàn đích đã có khách đặt trong khung giờ này");
                        }
                    }
                }
                for (ChiTietDatBan res : reservationsToMove) {
                    ChiTietDatBan moved = new ChiTietDatBan();
                    moved.setBan(toBan);
                    moved.setTenKhach(res.getTenKhach());
                    moved.setSdt(res.getSdt());
                    moved.setNhanVien(res.getNhanVien());
                    moved.setNgayGioDat(res.getNgayGioDat());
                    chiTietDatBanRepository.save(moved);
                }
                chiTietDatBanRepository.deleteAll(reservationsToMove);
            }

            fromBan.setTinhTrang(TinhTrangBan.TRONG);
            banRepository.save(fromBan);
        } else {
            fromHd.setTongTien(fromTotal);
            hoaDonRepository.save(fromHd);
        }

        toHd.setTongTien(toTotal);
        hoaDonRepository.save(toHd);

        toBan.setTinhTrang(TinhTrangBan.DANG_SU_DUNG);
        banRepository.save(toBan);
        wsEventPublisher.publishTableEvent("TABLE_SPLIT", fromBanId);
        wsEventPublisher.publishTableEvent("TABLE_SPLIT", toBanId);
        log.info("Split table ok fromBanId={} toBanId={} fromInvoiceId={} toInvoiceId={} fromDeleted={}",
                fromBanId, toBanId, fromHd.getMaHoaDon(), toHd.getMaHoaDon(), fromEmpty);
    }

    /**
     * Cancel reservation.
     *
     * @param banId banId
     */
    @Override
    public void cancelReservation(long banId) {
        log.info("Cancel reservation start banId={}", banId);
        Ban ban = banRepository.findById(banId).orElseThrow(() -> new IllegalArgumentException("Bàn không tồn tại"));

        if (ban.getTinhTrang() != TinhTrangBan.DA_DAT) {
            throw new IllegalStateException("Chỉ hủy được bàn đã đặt");
        }

        boolean hasHoaDon = hoaDonRepository.existsByBanAndTrangThai(ban, TrangThaiHoaDon.MOI_TAO);
        if (hasHoaDon) {
            throw new IllegalStateException("Bàn đã có hóa đơn, không thể hủy");
        }

        
        chiTietDatBanRepository.deleteByBan(ban);

        ban.setTinhTrang(TinhTrangBan.TRONG);
        banRepository.save(ban);
        wsEventPublisher.publishTableEvent("RESERVATION_CANCELED", banId);
        log.info("Cancel reservation ok banId={}", banId);
    }

    private BigDecimal calculateTotal(HoaDon hoaDon) {
        if (hoaDon.getChiTietHoaDons() == null) {
            return BigDecimal.ZERO;
        }
        return hoaDon.getChiTietHoaDons().stream()
                .map(ChiTietHoaDon::getThanhTien)
                .filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BigDecimal updateInvoiceTotal(HoaDon hoaDon) {
        BigDecimal total = calculateTotal(hoaDon);
        hoaDon.setTongTien(total);
        hoaDonRepository.save(hoaDon);
        return total;
    }

    private HoaDon getOrCreateInvoice(long tableId, boolean markTableInUse) {
        Optional<HoaDon> existing = hoaDonRepository.findChuaThanhToanByBan(tableId);
        if (existing.isPresent()) {
            return existing.get();
        }
        HoaDon invoice = new HoaDon();
        banRepository.findById(tableId).ifPresent(ban -> {
            invoice.setBan(ban);
            if (markTableInUse) {
                ban.setTinhTrang(TinhTrangBan.DANG_SU_DUNG);
                banRepository.save(ban);
            }
        });
        invoice.setNgayGioTao(LocalDateTime.now());
        invoice.setTrangThai(TrangThaiHoaDon.MOI_TAO);
        HoaDon saved = hoaDonRepository.save(invoice);
        log.info("Created invoice id={} tableId={} markTableInUse={}", saved.getMaHoaDon(), tableId, markTableInUse);
        return saved;
    }

    private void validatePayment(BigDecimal tienKhach, BigDecimal total) {
        if (tienKhach.compareTo(total) < 0) {
            throw new IllegalArgumentException("Tiền khách đưa nhỏ hơn tổng");
        }
    }

    private Optional<NhanVien> findCurrentNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }
        return taiKhoanRepository.findByTenDangNhap(auth.getName())
                .flatMap(tk -> nhanVienRepository.findByTaiKhoan_MaTaiKhoan(tk.getMaTaiKhoan()));
    }

    private void assignCurrentNhanVien(HoaDon hoaDon) {
        if (hoaDon == null) return;
        Optional<NhanVien> nvOpt = findCurrentNhanVien();
        if (nvOpt.isPresent()) {
            hoaDon.setNhanVien(nvOpt.get());
            return;
        }
        try {
            NhanVien nv = getOrCreateCurrentNhanVien();
            hoaDon.setNhanVien(nv);
        } catch (Exception ex) {
            log.warn("Không thể gắn nhân viên cho hóa đơn id={} reason={}",
                    hoaDon.getMaHoaDon(), ex.getMessage());
        }
    }

    private NhanVien getOrCreateCurrentNhanVien() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("Không xác định người dùng đang đăng nhập");
        }
        String username = auth.getName();
        log.info("reserveTable invoked by username={}", username);
        Optional<TaiKhoan> tkOpt = taiKhoanRepository.findByTenDangNhap(username);
        if (tkOpt.isEmpty()) {
            throw new IllegalStateException("Không tìm thấy tài khoản cho user " + username);
        }
        TaiKhoan tk = tkOpt.get();
        log.info("Found taiKhoanId={} for username={}", tk.getMaTaiKhoan(), username);
        Optional<NhanVien> nvOpt = nhanVienRepository.findByTaiKhoan_MaTaiKhoan(tk.getMaTaiKhoan());
        if (nvOpt.isPresent()) {
            return nvOpt.get();
        }
        log.warn("No nhanVien linked to taiKhoanId={}, creating placeholder", tk.getMaTaiKhoan());
        NhanVien nv = new NhanVien();
        nv.setHoTen(username);
        nv.setSoDienThoai(null);
        nv.setDiaChi(null);
        nv.setTaiKhoan(tk);
        nv = nhanVienRepository.save(nv);
        log.info("Created placeholder nhanVienId={} for taiKhoanId={}", nv.getMaNhanVien(), tk.getMaTaiKhoan());
        return nv;
    }

    private ChiTietHoaDon findExistingDetail(HoaDon hoaDon, long thucDonId) {
        if (hoaDon.getChiTietHoaDons() == null) {
            return null;
        }
        return hoaDon.getChiTietHoaDons().stream()
                .filter(ct -> ct.getThucDon().getMaThucDon().equals(thucDonId))
                .findFirst()
                .orElse(null);
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

    /**
     * Scheduled cleanup for expired reservations.
     * Runs every minute to remove reservations older than 1 hour after scheduled time.
     */
    @org.springframework.scheduling.annotation.Scheduled(fixedRateString = "60000")
    public void cleanupExpiredReservations() {
        LocalDateTime now = LocalDateTime.now();
        List<ChiTietDatBan> all = chiTietDatBanRepository.findAll();
        for (ChiTietDatBan res : all) {
            if (res.getNgayGioDat() == null) continue;
            LocalDateTime when = res.getNgayGioDat();
            Ban b = res.getBan();
            try {
                // If reservation time has arrived (or passed) and there is no order, remove it and free table
                if (!now.isBefore(when)) {
                    boolean hasOrder = false;
                    if (b != null) {
                        Optional<HoaDon> hdOpt = hoaDonRepository.findChuaThanhToanByBan(b.getMaBan());
                        if (hdOpt.isPresent()) {
                            HoaDon hd = hdOpt.get();
                            if (hd.getChiTietHoaDons() != null && hd.getChiTietHoaDons().stream()
                                    .anyMatch(ct -> ct.getSoLuong() != null && ct.getSoLuong() > 0)) {
                                hasOrder = true;
                            }
                        }
                    }
                    if (!hasOrder) {
                        chiTietDatBanRepository.delete(res);
                        if (b != null && !chiTietDatBanRepository.existsByBan(b)) {
                            b.setTinhTrang(TinhTrangBan.TRONG);
                            banRepository.save(b);
                        }
                        log.info("Reservation removed at arrival time banId={} when={} now={}", b == null ? null : b.getMaBan(), when, now);
                        continue;
                    }
                }

                // Fallback: remove reservations that expired 1 hour after scheduled time
                LocalDateTime expiry = when.plusHours(1);
                if (expiry.isBefore(now)) {
                    chiTietDatBanRepository.delete(res);
                    if (b != null && !chiTietDatBanRepository.existsByBan(b)) {
                        b.setTinhTrang(TinhTrangBan.TRONG);
                        banRepository.save(b);
                        log.info("Expired reservation removed and table released banId={} expiredAt={}", b.getMaBan(), expiry);
                    } else if (b != null) {
                        log.info("Expired reservation removed but other reservations remain for banId={}", b.getMaBan());
                    } else {
                        log.info("Expired reservation removed for unknown ban expiredAt={}", expiry);
                    }
                }
            } catch (Exception ex) {
                log.warn("Failed to cleanup expired reservation for banId={} reason={}", b == null ? null : b.getMaBan(), ex.getMessage());
            }
        }
    }
}
