package com.example.demo.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.example.demo.entity.DonNhap;
import com.example.demo.entity.DonViTinh;
import com.example.demo.entity.DonXuat;
import com.example.demo.entity.HangHoa;
import com.example.demo.entity.NhanVien;
import com.example.demo.payload.dto.HangHoaKhoDto;
import com.example.demo.payload.form.EditHangHoaForm;
import com.example.demo.payload.form.HangHoaNhapForm;
import com.example.demo.repository.DonNhapRepository;
import com.example.demo.repository.DonViTinhRepository;
import com.example.demo.repository.DonXuatRepository;
import com.example.demo.repository.HangHoaRepository;
import com.example.demo.service.HangHoaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service implementation for Hang Hoa.
 */
@Service
public class HangHoaServiceImpl implements HangHoaService {

    private static final Logger log = LoggerFactory.getLogger(HangHoaServiceImpl.class);

    private final HangHoaRepository hangHoaRepo;
    private final DonNhapRepository donNhapRepo;
    private final DonXuatRepository donXuatRepo;
    private final DonViTinhRepository donViTinhRepo;

    /**
     * Creates HangHoaServiceImpl.
     *
     * @param hangHoaRepo hangHoaRepo
     * @param donNhapRepo donNhapRepo
     * @param donXuatRepo donXuatRepo
     * @param donViTinhRepo donViTinhRepo
     */
    public HangHoaServiceImpl(HangHoaRepository hangHoaRepo,
                              DonNhapRepository donNhapRepo,
                              DonXuatRepository donXuatRepo,
                              DonViTinhRepository donViTinhRepo) {
        this.hangHoaRepo = hangHoaRepo;
        this.donNhapRepo = donNhapRepo;
        this.donXuatRepo = donXuatRepo;
        this.donViTinhRepo = donViTinhRepo;
    }

    /**
     * Get danh sach kho.
     *
     * @return result
     */
    @Override
    public List<HangHoaKhoDto> getDanhSachKho() {
        List<HangHoa> list = hangHoaRepo.findAll();
        List<HangHoaKhoDto> result = new ArrayList<>();

        for (HangHoa hh : list) {
            HangHoaKhoDto dto = new HangHoaKhoDto();
            dto.setMaHangHoa(hh.getMaHangHoa());
            dto.setTenHangHoa(hh.getTenHangHoa());
            dto.setSoLuong(hh.getSoLuong());
            dto.setDonGia(hh.getDonGia());

            dto.setDonVi(hh.getDonViTinh() != null ? hh.getDonViTinh().getTenDonVi() : "N/A");

            dto.setNgayNhapGanNhat(donNhapRepo.findNgayNhapGanNhat(hh.getMaHangHoa()));
            dto.setNgayXuatGanNhat(donXuatRepo.findNgayXuatGanNhat(hh.getMaHangHoa()));

            result.add(dto);
        }

        return result;
    }
    /**
     * Search hang hoa.
     *
     * @param keyword keyword
     * @return result
     */
    @Override
    public List<HangHoaKhoDto> searchHangHoa(String keyword) {
        List<HangHoa> list;
        if (keyword == null || keyword.trim().isEmpty()) {
            list = hangHoaRepo.findAll();
        } else {
            list = hangHoaRepo.findByTenHangHoaContainingIgnoreCase(keyword.trim());
        }
        List<HangHoaKhoDto> result = new ArrayList<>();
        for (HangHoa hh : list) {
            HangHoaKhoDto dto = new HangHoaKhoDto();
            dto.setMaHangHoa(hh.getMaHangHoa());
            dto.setTenHangHoa(hh.getTenHangHoa());
            dto.setSoLuong(hh.getSoLuong());
            dto.setDonGia(hh.getDonGia());
            dto.setDonVi(hh.getDonViTinh() != null ? hh.getDonViTinh().getTenDonVi() : "N/A");
            dto.setNgayNhapGanNhat(donNhapRepo.findNgayNhapGanNhat(hh.getMaHangHoa()));
            dto.setNgayXuatGanNhat(donXuatRepo.findNgayXuatGanNhat(hh.getMaHangHoa()));
            result.add(dto);
        }
        return result;
    }
    /**
     * Nhap hang.
     *
     * @param form form
     * @param nhanVien nhanVien
     */
    @Override
    @Transactional
    public void nhapHang(HangHoaNhapForm form, NhanVien nhanVien) {
        if (form.getTenHangHoa() == null || form.getTenHangHoa().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên hàng hóa bắt buộc");
        }
        String tenHangHoa = normalizeTenHangHoa(form.getTenHangHoa());
        if (form.getSoLuong() == null || form.getSoLuong().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng phải > 0");
        }
        if (form.getDonGia() == null || form.getDonGia().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Đơn giá phải lớn hơn hoặc bằng 0");
        }
        String donViMoi = normalizeDonVi(form.getDonViMoi());
        if ((donViMoi == null || donViMoi.isEmpty()) && form.getDonViTinhId() == null) {
            throw new IllegalArgumentException("Đơn vị bắt buộc");
        }
        if (form.getNgayNhap() == null) {
            throw new IllegalArgumentException("Ngày nhập bắt buộc");
        }
        if (form.getNgayNhap().toLocalDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Ngày nhập không được trước hôm nay");
        }

        DonViTinh donVi = null;
        if (donViMoi != null && !donViMoi.isEmpty()) {
            donVi = donViTinhRepo.findByTenDonViIgnoreCase(donViMoi).orElse(null);
            if (donVi == null) {
                DonViTinh newDonVi = new DonViTinh();
                newDonVi.setTenDonVi(donViMoi);
                donVi = donViTinhRepo.save(newDonVi);
            }
        } else {
            donVi = donViTinhRepo.findById(form.getDonViTinhId()).orElse(null);
            if (donVi == null) {
                throw new IllegalArgumentException("Đơn vị không hợp lệ");
            }
        }

        HangHoa hangHoa = hangHoaRepo
                .findByTenHangHoaIgnoreCaseAndDonViTinh_MaDonViTinh(tenHangHoa, donVi.getMaDonViTinh())
                .orElse(null);
        boolean isNew = hangHoa == null;

        if (hangHoa == null) {
            hangHoa = new HangHoa();
            hangHoa.setTenHangHoa(tenHangHoa);
            hangHoa.setSoLuong(form.getSoLuong());
            hangHoa.setDonGia(form.getDonGia());
            hangHoa.setDonViTinh(donVi);
        } else {
            BigDecimal current = hangHoa.getSoLuong() == null ? BigDecimal.ZERO : hangHoa.getSoLuong();
            hangHoa.setSoLuong(current.add(form.getSoLuong()));
            hangHoa.setDonGia(form.getDonGia());
        }

        hangHoaRepo.save(hangHoa);

        DonNhap dn = new DonNhap();
        dn.setHangHoa(hangHoa);
        dn.setNhanVien(nhanVien);
        dn.setSoLuong(form.getSoLuong());
        dn.setNgayNhap(form.getNgayNhap());
        
        donNhapRepo.save(dn);
        Long nhanVienId = nhanVien == null ? null : nhanVien.getMaNhanVien();
        log.info("Nhap hang hangHoaId={} soLuong={} nhanVienId={} isNew={}",
                hangHoa.getMaHangHoa(), form.getSoLuong(), nhanVienId, isNew);
    }

    /**
     * Xuat hang.
     *
     * @param hangHoaId hangHoaId
     * @param soLuong soLuong
     * @param ngayXuat ngayXuat
     * @param nhanVien nhanVien
     */
    @Override
    @Transactional
    public void xuatHang(long hangHoaId, BigDecimal soLuong, LocalDateTime ngayXuat, NhanVien nhanVien) {
        if (soLuong == null || soLuong.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số lượng xuất phải > 0");
        }
        if (ngayXuat == null) {
            throw new IllegalArgumentException("Ngày xuất bắt buộc");
        }
        if (ngayXuat.toLocalDate().isBefore(java.time.LocalDate.now())) {
            throw new IllegalArgumentException("Ngày xuất không được trước hôm nay");
        }
        HangHoa hh = hangHoaRepo.findById(hangHoaId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hàng hóa"));
        if (hh.getSoLuong() == null || hh.getSoLuong().compareTo(soLuong) < 0) {
            throw new RuntimeException("Số lượng tồn kho không đủ");
        }

        
        hh.setSoLuong(hh.getSoLuong().subtract(soLuong));
        hangHoaRepo.save(hh);

        
        DonXuat dx = new DonXuat();
        dx.setHangHoa(hh);
        dx.setSoLuong(soLuong);
        dx.setNgayXuat(ngayXuat);
        dx.setNhanVien(nhanVien);
        donXuatRepo.save(dx);
        Long nhanVienId = nhanVien == null ? null : nhanVien.getMaNhanVien();
        log.info("Xuat hang hangHoaId={} soLuong={} nhanVienId={}",
                hh.getMaHangHoa(), soLuong, nhanVienId);
    }

    /**
     * Update hang hoa.
     *
     * @param form form
     */
    @Override
    @Transactional
    public void updateHangHoa(EditHangHoaForm form) {
        HangHoa hh = hangHoaRepo.findById(form.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hàng hóa"));

        DonViTinh dvt = null;
        if (form.getDonViTinhId() != null) {
            dvt = donViTinhRepo.findById(form.getDonViTinhId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn vị"));
        }
        if (dvt == null) {
            throw new IllegalArgumentException("Đơn vị bắt buộc");
        }

        if (hh.getDonViTinh() != null
                && hh.getDonViTinh().getMaDonViTinh() != null
                && !hh.getDonViTinh().getMaDonViTinh().equals(dvt.getMaDonViTinh())) {
            throw new IllegalArgumentException("Không thể đổi đơn vị. Hãy tạo mặt hàng mới");
        }

        String tenHangHoa = normalizeTenHangHoa(form.getTenHangHoa());
        boolean duplicate = hangHoaRepo.existsByTenHangHoaIgnoreCaseAndDonViTinh_MaDonViTinhAndMaHangHoaNot(
                tenHangHoa,
                dvt.getMaDonViTinh(),
                hh.getMaHangHoa()
        );
        if (duplicate) {
            throw new IllegalArgumentException("Đã tồn tại hàng hóa cùng tên và đơn vị");
        }

        hh.setTenHangHoa(tenHangHoa);
        hh.setSoLuong(form.getSoLuong());
        hh.setDonGia(form.getDonGia());
        hh.setDonViTinh(dvt);

        hangHoaRepo.save(hh);
        log.info("Updated hangHoa id={} soLuong={}", hh.getMaHangHoa(), hh.getSoLuong());
    }

    /**
     * Delete hang hoa.
     *
     * @param id id
     */
    @Override
    @Transactional
    public void deleteHangHoa(long id) {
        HangHoa hh = hangHoaRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy hàng hóa"));

        boolean hasNhap = donNhapRepo.existsByHangHoa(hh);
        boolean hasXuat = donXuatRepo.existsByHangHoa(hh);

        if (hasNhap || hasXuat) {
            throw new IllegalStateException("Không thể xóa hàng hóa đã phát sinh nhập/xuất");
        }

        hangHoaRepo.delete(hh);
        log.info("Deleted hangHoa id={}", id);
    }

    private String normalizeTenHangHoa(String tenHangHoa) {
        if (tenHangHoa == null) {
            return null;
        }
        String trimmed = tenHangHoa.trim();
        if (trimmed.isEmpty()) {
            return trimmed;
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    private String normalizeDonVi(String donVi) {
        if (donVi == null) {
            return null;
        }
        String trimmed = donVi.trim();
        if (trimmed.isEmpty()) {
            return "";
        }
        return trimmed.replaceAll("\\s+", " ");
    }
}
