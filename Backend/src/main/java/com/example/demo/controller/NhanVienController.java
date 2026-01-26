package com.example.demo.controller;

import java.util.List;
import java.util.stream.Collectors;

import com.example.demo.entity.NhanVien;
import com.example.demo.entity.ChucVu;
import com.example.demo.entity.TaiKhoan;
import com.example.demo.payload.dto.NhanVienDto;
import com.example.demo.payload.request.NhanVienRequest;
import com.example.demo.repository.ChucVuRepository;
import com.example.demo.repository.TaiKhoanRepository;
import com.example.demo.service.NhanVienService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
/**
 * REST controller for Nhan Vien.
 */
@RestController
@RequestMapping("/api/nhanvien")
public class NhanVienController {

    private final NhanVienService nhanVienService;
    private final ChucVuRepository chucVuRepository;
    private final TaiKhoanRepository taiKhoanRepository;
    /**
     * Creates a new Nhan Vien Controller.
     * @param nhanVienService nhan vien service
     */
    public NhanVienController(NhanVienService nhanVienService,
                              ChucVuRepository chucVuRepository,
                              TaiKhoanRepository taiKhoanRepository) {
        this.nhanVienService = nhanVienService;
        this.chucVuRepository = chucVuRepository;
        this.taiKhoanRepository = taiKhoanRepository;
    }

    private static NhanVienDto toDto(NhanVien nv) {
        if (nv == null) return null;
        NhanVienDto d = new NhanVienDto();
        d.setMaNhanVien(nv.getMaNhanVien());
        d.setHoTen(nv.getHoTen());
        d.setSoDienThoai(nv.getSoDienThoai());
        d.setDiaChi(nv.getDiaChi());
        d.setChucVuId(nv.getChucVu() == null ? null : nv.getChucVu().getMaChucVu());
        String chucVu = nv.getChucVu() == null ? null : nv.getChucVu().getTenChucVu();
        if (chucVu == null || chucVu.isBlank()) {
            if (nv.getTaiKhoan() != null && nv.getTaiKhoan().getQuyenHan() != null) {
                String role = nv.getTaiKhoan().getQuyenHan().name();
                if ("ADMIN".equals(role)) {
                    chucVu = "Quản trị";
                } else if ("NHANVIEN".equals(role)) {
                    chucVu = "Nhân viên";
                } else {
                    chucVu = role;
                }
            }
        }
        d.setChucVu(chucVu);
        d.setTaiKhoanId(nv.getTaiKhoan() == null ? null : nv.getTaiKhoan().getMaTaiKhoan());
        d.setEnabled(nv.getEnabled() == null ? true : nv.getEnabled());
        return d;
    }
    /**
     * Lists items.
     * @param q q
     * @return response entity
     */
    @GetMapping
    public ResponseEntity<List<NhanVienDto>> list(@RequestParam(value = "q", required = false) String q) {
        List<NhanVien> list = (q == null || q.isBlank()) ? nhanVienService.findAll() : nhanVienService.findByHoTenContaining(q);
        return ResponseEntity.ok(list.stream().map(NhanVienController::toDto).collect(Collectors.toList()));
    }
    /**
     * Gets an item.
     * @param id id
     * @return response entity
     */
    @GetMapping("/{id}")
    public ResponseEntity<NhanVienDto> get(@PathVariable long id) {
        return nhanVienService.findById(id).map(NhanVienController::toDto).map(ResponseEntity::ok)
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy nhân viên"));
    }
    /**
     * Creates a new entry.
     * @param nv nv
     * @return response entity
     */
    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody NhanVienRequest req) {
        NhanVien nv = new NhanVien();
        applyRequest(nv, req);
        nhanVienService.save(nv);
        return ResponseEntity.status(201).build();
    }
    /**
     * Updates the entry.
     * @param id id
     * @param nv nv
     * @return response entity
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable long id, @Valid @RequestBody NhanVienRequest req) {
        NhanVien existing = nhanVienService.findById(id)
                .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy nhân viên"));
        existing.setMaNhanVien(id);
        applyRequest(existing, req);
        nhanVienService.save(existing);
        return ResponseEntity.ok().build();
    }
    /**
     * Deletes the entry.
     * @param id id
     * @return response entity
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable long id) {
        nhanVienService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private void applyRequest(NhanVien target, NhanVienRequest req) {
        target.setHoTen(req.getHoTen());
        target.setSoDienThoai(req.getSoDienThoai());
        target.setDiaChi(req.getDiaChi());
        target.setEnabled(req.getEnabled() == null ? true : req.getEnabled());
        target.setLuong(req.getLuong());

        if (req.getChucVuId() != null) {
            ChucVu chucVu = chucVuRepository.findById(req.getChucVuId())
                    .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy chức vụ"));
            target.setChucVu(chucVu);
        } else {
            target.setChucVu(null);
        }

        if (req.getTaiKhoanId() != null) {
            TaiKhoan taiKhoan = taiKhoanRepository.findById(req.getTaiKhoanId())
                    .orElseThrow(() -> new java.util.NoSuchElementException("Không tìm thấy tài khoản"));
            target.setTaiKhoan(taiKhoan);
        } else {
            target.setTaiKhoan(null);
        }
    }
}
