package com.example.demo.service.impl;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.example.demo.entity.ChiTieu;
import com.example.demo.entity.HoaDon;
import com.example.demo.entity.TaiKhoan;
import com.example.demo.payload.dto.ThuChiDto;
import com.example.demo.payload.form.ChiTieuForm;
import com.example.demo.repository.ChiTieuRepository;
import com.example.demo.repository.HoaDonRepository;
import com.example.demo.repository.TaiKhoanRepository;
import com.example.demo.service.NganSachService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Ngan Sach.
 */
@Service
@Transactional
public class NganSachServiceImpl implements NganSachService {

    private static final Logger log = LoggerFactory.getLogger(NganSachServiceImpl.class);

    private final ChiTieuRepository chiTieuRepo;
    private final HoaDonRepository hoaDonRepo;
    private final TaiKhoanRepository taiKhoanRepo;

    /**
     * Creates NganSachServiceImpl.
     *
     * @param chiTieuRepo chiTieuRepo
     * @param hoaDonRepo hoaDonRepo
     * @param taiKhoanRepo taiKhoanRepo
     */
    public NganSachServiceImpl(ChiTieuRepository chiTieuRepo,
                               HoaDonRepository hoaDonRepo,
                               TaiKhoanRepository taiKhoanRepo) {
        this.chiTieuRepo = chiTieuRepo;
        this.hoaDonRepo = hoaDonRepo;
        this.taiKhoanRepo = taiKhoanRepo;
    }

    /**
     * Xem thu chi.
     *
     * @param from from
     * @param to to
     * @return result
     */
    @Override
    public List<ThuChiDto> xemThuChi(LocalDate from, LocalDate to) {
        Map<LocalDate, BigDecimal> thuMap = new HashMap<>();
        Map<LocalDate, BigDecimal> chiMap = new HashMap<>();

        // THU từ hóa đơn
        List<HoaDon> hoadons = hoaDonRepo.findByNgayThanhToanBetween(
                from.atStartOfDay(),
                to.atTime(23, 59, 59)
        );
        for (HoaDon hd : hoadons) {
            LocalDate ngay = hd.getNgayThanhToan().toLocalDate();
            thuMap.merge(ngay, hd.getTongTien() == null ? BigDecimal.ZERO : hd.getTongTien(), BigDecimal::add);
        }

        // CHI từ chi_tieu
        List<ChiTieu> chitieus = chiTieuRepo.findByNgayChiBetween(from, to);
        for (ChiTieu ct : chitieus) {
            chiMap.merge(ct.getNgayChi(), ct.getSoTien() == null ? BigDecimal.ZERO : ct.getSoTien(), BigDecimal::add);
        }

        Set<LocalDate> allDays = new HashSet<>();
        allDays.addAll(thuMap.keySet());
        allDays.addAll(chiMap.keySet());

        List<LocalDate> sorted = new ArrayList<>(allDays);
        Collections.sort(sorted);

        List<ThuChiDto> result = new ArrayList<>();
        for (LocalDate d : sorted) {
            result.add(new ThuChiDto(
                    d,
                    thuMap.getOrDefault(d, BigDecimal.ZERO),
                    chiMap.getOrDefault(d, BigDecimal.ZERO)
            ));
        }
        log.info("ThuChi summary from {} to {} rows={}", from, to, result.size());
        return result;
    }

    /**
     * Them chi tieu.
     *
     * @param form form
     * @param username username
     */
    @Override
    public void themChiTieu(ChiTieuForm form, String username) {
        if (form == null) {
            throw new IllegalArgumentException("Dữ liệu không hợp lệ");
        }
        if (form.getTenKhoanChi() == null || form.getTenKhoanChi().isBlank()) {
            throw new IllegalArgumentException("Khoản chi bắt buộc");
        }
        if (form.getNgayChi() == null) {
            throw new IllegalArgumentException("Ngày chi bắt buộc");
        }
        if (form.getSoTien() == null || form.getSoTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền phải lớn hơn 0");
        }
        TaiKhoan tk = taiKhoanRepo.findByTenDangNhap(username).orElseThrow(() -> new IllegalArgumentException("Tài khoản không tồn tại"));

        ChiTieu ct = new ChiTieu();
        ct.setTenKhoanChi(form.getTenKhoanChi());
        ct.setSoTien(form.getSoTien());
        ct.setNgayChi(form.getNgayChi());
        ct.setTaiKhoan(tk);

        ChiTieu saved = chiTieuRepo.save(ct);
        Long taiKhoanId = tk.getMaTaiKhoan();
        log.info("Created chiTieu id={} taiKhoanId={} soTien={}",
                saved.getMaChiTieu(), taiKhoanId, saved.getSoTien());
    }
}
