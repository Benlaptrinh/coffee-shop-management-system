package com.example.demo.controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import com.example.demo.dto.ChiTieuForm;
import com.example.demo.dto.EditHangHoaForm;
import com.example.demo.dto.HangHoaKhoDTO;
import com.example.demo.dto.HangHoaNhapForm;
import com.example.demo.dto.ThuChiDTO;
import com.example.demo.dto.ThucDonForm;
import com.example.demo.dto.ThietBiForm;
import com.example.demo.dto.XuatHangForm;
import jakarta.validation.Valid;
import com.example.demo.entity.Ban;
import com.example.demo.entity.HangHoa;
import com.example.demo.entity.NhanVien;
import com.example.demo.entity.TaiKhoan;
import com.example.demo.entity.ThietBi;
import com.example.demo.entity.ThucDon;
import com.example.demo.enums.TinhTrangBan;
import com.example.demo.repository.DonViTinhRepository;
import com.example.demo.repository.HangHoaRepository;
import com.example.demo.repository.TaiKhoanRepository;
import com.example.demo.service.HangHoaService;
import com.example.demo.service.NganSachService;
import com.example.demo.service.NhanVienService;
import com.example.demo.service.SalesService;
import com.example.demo.service.ThietBiService;
import com.example.demo.service.ThucDonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * AdminPagesController
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
@RequestMapping("/admin")
public class AdminPagesController {

    private static final Logger log = LoggerFactory.getLogger(AdminPagesController.class);

    private final SalesService salesService;
    private final ThietBiService thietBiService;
    private final HangHoaService hangHoaService;
    private final DonViTinhRepository donViTinhRepository;
    private final NhanVienService nhanVienService;
    private final TaiKhoanRepository taiKhoanRepository;
    private final HangHoaRepository hangHoaRepository;
    private final ThucDonService thucDonService;
    private final NganSachService nganSachService;
    private String sidebar = "fragments/sidebar-admin";

    /**
     * Creates AdminPagesController.
     *
     * @param salesService salesService
     * @param thietBiService thietBiService
     * @param hangHoaService hangHoaService
     * @param donViTinhRepository donViTinhRepository
     * @param nhanVienService nhanVienService
     * @param taiKhoanRepository taiKhoanRepository
     * @param hangHoaRepository hangHoaRepository
     * @param thucDonService thucDonService
     * @param nganSachService nganSachService
     */
    public AdminPagesController(SalesService salesService, ThietBiService thietBiService, HangHoaService hangHoaService,
                                DonViTinhRepository donViTinhRepository, NhanVienService nhanVienService,
                                TaiKhoanRepository taiKhoanRepository, HangHoaRepository hangHoaRepository,
                                ThucDonService thucDonService, NganSachService nganSachService) {
        this.salesService = salesService;
        this.thietBiService = thietBiService;
        this.hangHoaService = hangHoaService;
        this.donViTinhRepository = donViTinhRepository;
        this.nhanVienService = nhanVienService;
        this.taiKhoanRepository = taiKhoanRepository;
        this.hangHoaRepository = hangHoaRepository;
        this.thucDonService = thucDonService;
        this.nganSachService = nganSachService;
    }

    private String usernameFromAuth(Authentication auth) {
        return auth == null ? "anonymous" : auth.getName();
    }

    /**
     * Sales.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/sales")
    public String sales(Model model, Authentication auth) {
        String username = usernameFromAuth(auth);
        List<Ban> tables = salesService.findAllTables();
        int trong = 0;
        int dangSuDung = 0;
        int daDat = 0;
        for (Ban ban : tables) {
            if (ban.getTinhTrang() == TinhTrangBan.TRONG) {
                trong++;
            } else if (ban.getTinhTrang() == TinhTrangBan.DANG_SU_DUNG) {
                dangSuDung++;
            } else if (ban.getTinhTrang() == TinhTrangBan.DA_DAT) {
                daDat++;
            }
        }
        log.info("Admin sales page user={} totalTables={} trong={} dangSuDung={} daDat={}",
                username, tables.size(), trong, dangSuDung, daDat);
        model.addAttribute("username", username);
        model.addAttribute("sidebarFragment", sidebar);
        model.addAttribute("contentFragment", "sales/index");
        model.addAttribute("tables", tables);
        return "layout/base";
    }

    /**
     * Equipment.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/equipment")
    public String equipment(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(required = false) String keyword,
                            Model model,
                            Authentication auth) {
        int pageSize = 5;
        String trimmedKeyword = keyword == null ? null : keyword.trim();
        List<ThietBi> allItems = thietBiService.findAll();
        if (trimmedKeyword != null && !trimmedKeyword.isEmpty()) {
            String lower = trimmedKeyword.toLowerCase();
            allItems.removeIf(item -> {
                String name = item.getTenThietBi();
                return name == null || !name.toLowerCase().contains(lower);
            });
        }
        allItems.sort(Comparator.comparing(
                tb -> tb.getTenThietBi() == null ? "" : tb.getTenThietBi(),
                String.CASE_INSENSITIVE_ORDER
        ));
        int totalItems = allItems.size();
        int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<ThietBi> pageItems = totalItems == 0 ? Collections.emptyList() : allItems.subList(fromIndex, toIndex);

        model.addAttribute("username", usernameFromAuth(auth));
        model.addAttribute("sidebarFragment", sidebar);
        model.addAttribute("contentFragment", "admin/equipment");
        model.addAttribute("items", pageItems);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("keyword", trimmedKeyword);
        
        if (!model.containsAttribute("thietBi")) {
            model.addAttribute("thietBi", new ThietBiForm());
        }
        model.addAttribute("minDate", LocalDate.now().format(DateTimeFormatter.ISO_DATE));
        return "layout/base";
    }

    /**
     * Warehouse.
     *
     * @param keyword keyword
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/warehouse")
    public String warehouse(@RequestParam(required = false) String keyword,
                            @RequestParam(defaultValue = "1") int page,
                            Model model,
                            Authentication auth) {
        return renderWarehousePage(model, auth, keyword, page, new HangHoaNhapForm(), new XuatHangForm(), null);
    }

    /**
     * Warehouse create form.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/warehouse/create")
    public String warehouseCreateForm(Model model, Authentication auth) {
        model.addAttribute("username", usernameFromAuth(auth));
        model.addAttribute("sidebarFragment", sidebar);
        model.addAttribute("contentFragment", "admin/kho/form");
        model.addAttribute("form", new HangHoaNhapForm());
        model.addAttribute("donViTinhs", donViTinhRepository.findAll());
        model.addAttribute("activeMenu", "warehouse");
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        model.addAttribute("minDate", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(startOfToday));
        return "layout/base";
    }

    /**
     * Warehouse create submit.
     *
     * @param form form
     * @param principal principal
     * @param ra ra
     * @return result
     */
    @PostMapping("/warehouse/create")
    public String warehouseCreateSubmit(@Valid @ModelAttribute("form") HangHoaNhapForm form,
                                        BindingResult bindingResult,
                                        Principal principal,
                                        Model model,
                                        Authentication auth,
                                        RedirectAttributes ra) {
        String donViMoi = normalizeDonVi(form.getDonViMoi());
        form.setDonViMoi(donViMoi);
        if ((donViMoi == null || donViMoi.isEmpty()) && form.getDonViTinhId() == null) {
            bindingResult.rejectValue("donViTinhId", "invalid", "Đơn vị bắt buộc");
        }
        validateWarehouseDate(form.getNgayNhap(), bindingResult, "ngayNhap", "Ngày nhập không được trước hôm nay");
        if (bindingResult.hasErrors()) {
            return renderWarehousePage(model, auth, null, 1, form, new XuatHangForm(), null);
        }
        String username = principal == null ? null : principal.getName();
        NhanVien nv = null;
        if (username != null) {
            TaiKhoan tk = taiKhoanRepository.findByTenDangNhap(username).orElse(null);
            if (tk != null) {
                nv = nhanVienService.findByTaiKhoanId(tk.getMaTaiKhoan()).orElse(null);
            }
        }

        try {
            hangHoaService.nhapHang(form, nv);
            ra.addFlashAttribute("success", "Nhập hàng thành công");
            return "redirect:/admin/warehouse";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return renderWarehousePage(model, auth, null, 1, form, new XuatHangForm(), null);
        }
    }

    /**
     * Warehouse export submit.
     *
     * @param xuatForm xuatForm
     * @param principal principal
     * @param ra ra
     * @return result
     */
    @PostMapping("/warehouse/export")
    public String warehouseExportSubmit(@Valid @ModelAttribute("xuatForm") XuatHangForm xuatForm,
                                        BindingResult bindingResult,
                                        Principal principal,
                                        Model model,
                                        Authentication auth,
                                        RedirectAttributes ra) {
        validateWarehouseDate(xuatForm.getNgayXuat(), bindingResult, "ngayXuat", "Ngày xuất không được trước hôm nay");
        if (bindingResult.hasErrors()) {
            return renderWarehousePage(model, auth, null, 1, new HangHoaNhapForm(), xuatForm, null);
        }
        String username = principal == null ? null : principal.getName();
        NhanVien nv = null;
        if (username != null) {
            TaiKhoan tk = taiKhoanRepository.findByTenDangNhap(username).orElse(null);
            if (tk != null) {
                nv = nhanVienService.findByTaiKhoanId(tk.getMaTaiKhoan()).orElse(null);
            }
        }

        try {
            hangHoaService.xuatHang(xuatForm.getHangHoaId(), xuatForm.getSoLuong(), xuatForm.getNgayXuat(), nv);
            ra.addFlashAttribute("success", "Xuất hàng thành công");
            return "redirect:/admin/warehouse";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return renderWarehousePage(model, auth, null, 1, new HangHoaNhapForm(), xuatForm, null);
        }
    }

    /**
     * Warehouse edit form.
     *
     * @param id id
     * @param model model
     * @param ra ra
     * @return result
     */
    @GetMapping("/warehouse/edit/{id}")
    public String warehouseEditForm(@PathVariable Long id,
                                    @RequestParam(required = false) String keyword,
                                    @RequestParam(defaultValue = "1") int page,
                                    Model model,
                                    Authentication auth,
                                    RedirectAttributes ra) {
        HangHoa hh = hangHoaRepository.findById(id).orElse(null);
        if (hh == null) {
            ra.addFlashAttribute("error", "Không tìm thấy hàng hóa");
            ra.addAttribute("page", page);
            if (keyword != null && !keyword.trim().isEmpty()) {
                ra.addAttribute("keyword", keyword.trim());
            }
            return "redirect:/admin/warehouse";
        }
        EditHangHoaForm form = new EditHangHoaForm();
        form.setId(hh.getMaHangHoa());
        form.setTenHangHoa(hh.getTenHangHoa());
        form.setSoLuong(hh.getSoLuong());
        form.setDonGia(hh.getDonGia());
        if (hh.getDonViTinh() != null) {
            form.setDonViTinhId(hh.getDonViTinh().getMaDonViTinh());
        }
        model.addAttribute("editForm", form);
        return renderWarehousePage(model, auth, keyword, page, new HangHoaNhapForm(), new XuatHangForm(), form);
    }

    /**
     * Warehouse edit submit.
     *
     * @param form form
     * @param ra ra
     * @return result
     */
    @PostMapping("/warehouse/edit")
    public String warehouseEditSubmit(@Valid @ModelAttribute("editForm") EditHangHoaForm form,
                                      BindingResult bindingResult,
                                      @RequestParam(required = false) String keyword,
                                      @RequestParam(defaultValue = "1") int page,
                                      Model model,
                                      Authentication auth,
                                      RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return renderWarehousePage(model, auth, keyword, page, new HangHoaNhapForm(), new XuatHangForm(), form);
        }
        try {
            hangHoaService.updateHangHoa(form);
            ra.addFlashAttribute("success", "Cập nhật hàng hóa thành công");
            ra.addAttribute("page", page);
            if (keyword != null && !keyword.trim().isEmpty()) {
                ra.addAttribute("keyword", keyword.trim());
            }
            return "redirect:/admin/warehouse";
        } catch (RuntimeException ex) {
            model.addAttribute("error", ex.getMessage());
            return renderWarehousePage(model, auth, keyword, page, new HangHoaNhapForm(), new XuatHangForm(), form);
        }
    }

    private String renderWarehousePage(Model model,
                                       Authentication auth,
                                       String keyword,
                                       int page,
                                       HangHoaNhapForm form,
                                       XuatHangForm xuatForm,
                                       EditHangHoaForm editForm) {
        int pageSize = 3;
        List<HangHoaKhoDTO> allItems = hangHoaService.searchHangHoa(keyword);
        allItems.sort(Comparator.comparing(
                item -> item.getTenHangHoa() == null ? "" : item.getTenHangHoa(),
                String.CASE_INSENSITIVE_ORDER
        ));
        int totalItems = allItems.size();
        int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<HangHoaKhoDTO> pageItems = totalItems == 0 ? Collections.emptyList() : allItems.subList(fromIndex, toIndex);

        model.addAttribute("username", usernameFromAuth(auth));
        model.addAttribute("sidebarFragment", sidebar);
        model.addAttribute("contentFragment", "admin/kho/list");
        model.addAttribute("items", pageItems);
        model.addAttribute("keyword", keyword);
        model.addAttribute("form", form);
        model.addAttribute("donViTinhs", donViTinhRepository.findAll());
        model.addAttribute("activeMenu", "warehouse");
        model.addAttribute("xuatForm", xuatForm);
        model.addAttribute("hangHoas", hangHoaService.getDanhSachKho());
        model.addAttribute("editForm", editForm);
        model.addAttribute("editMode", editForm != null);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", pageSize);
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        model.addAttribute("minDate", DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm").format(startOfToday));
        return "layout/base";
    }

    private void validateWarehouseDate(LocalDateTime date,
                                       BindingResult bindingResult,
                                       String field,
                                       String message) {
        if (date == null) {
            return;
        }
        if (date.toLocalDate().isBefore(LocalDate.now())) {
            bindingResult.rejectValue(field, "invalid", message);
        }
    }

    private String normalizeDonVi(String donVi) {
        if (donVi == null) {
            return null;
        }
        String trimmed = donVi.trim();
        if (trimmed.isEmpty()) {
            return null;
        }
        return trimmed.replaceAll("\\s+", " ");
    }

    /**
     * Warehouse delete.
     *
     * @param id id
     * @param ra ra
     * @return result
     */
    @GetMapping("/warehouse/delete/{id}")
    public String warehouseDelete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            hangHoaService.deleteHangHoa(id);
            ra.addFlashAttribute("success", "Xóa hàng hóa thành công");
        } catch (IllegalStateException ex) {
            ra.addFlashAttribute("error", ex.getMessage());
        }
        return "redirect:/admin/warehouse";
    }

    /**
     * Menu.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/menu")
    public String menu(@RequestParam(required = false) String keyword,
                       @RequestParam(defaultValue = "1") int page,
                       Model model,
                       Authentication auth) {
        return renderMenuPage(model, auth, keyword, page, new ThucDonForm(), false);
    }

    /**
     * Menu search.
     *
     * @param keyword keyword
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/menu/search")
    public String menuSearch(@RequestParam(required = false) String keyword,
                             @RequestParam(defaultValue = "1") int page,
                             Model model,
                             Authentication auth) {
        return renderMenuPage(model, auth, keyword, page, new ThucDonForm(), false);
    }

    /**
     * Menu create form.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/menu/create")
    public String menuCreateForm() {
        return "redirect:/admin/menu";
    }

    /**
     * Menu create submit.
     *
     * @param tenMon tenMon
     * @param giaTien giaTien
     * @param redirect redirect
     * @return result
     */
    @PostMapping("/menu/create")
    public String menuCreateSubmit(@Valid @ModelAttribute("menuForm") ThucDonForm form,
                                   BindingResult bindingResult,
                                   @RequestParam(required = false) String keyword,
                                   @RequestParam(defaultValue = "1") int page,
                                   Model model,
                                   Authentication auth,
                                   RedirectAttributes redirect) {
        if (bindingResult.hasErrors()) {
            return renderMenuPage(model, auth, keyword, page, form, false);
        }
        try {
            thucDonService.create(form.getTenMon(), form.getGiaTien());
            redirect.addFlashAttribute("success", "Thêm món thành công");
            return "redirect:/admin/menu";
        } catch (IllegalArgumentException e) {
            applyMenuError(bindingResult, e.getMessage());
            return renderMenuPage(model, auth, keyword, page, form, false);
        }
    }

    /**
     * Menu edit form.
     *
     * @param id id
     * @param model model
     * @param ra ra
     * @return result
     */
    @GetMapping("/menu/edit/{id}")
    public String menuEditForm(@PathVariable Long id,
                               @RequestParam(required = false) String keyword,
                               @RequestParam(defaultValue = "1") int page,
                               Model model,
                               Authentication auth,
                               RedirectAttributes ra) {
        Optional<ThucDon> opt = thucDonService.findById(id);
        if (opt.isEmpty()) {
            ra.addFlashAttribute("error", "Không tìm thấy món");
            return "redirect:/admin/menu";
        }
        ThucDon thucDon = opt.get();
        ThucDonForm form = new ThucDonForm(thucDon.getMaThucDon(), thucDon.getTenMon(), thucDon.getGiaHienTai());
        return renderMenuPage(model, auth, keyword, page, form, true);
    }

    /**
     * Menu edit submit.
     *
     * @param id id
     * @param tenMon tenMon
     * @param giaTien giaTien
     * @param redirect redirect
     * @return result
     */
    @PostMapping("/menu/edit")
    public String menuEditSubmit(@Valid @ModelAttribute("menuForm") ThucDonForm form,
                                 BindingResult bindingResult,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(defaultValue = "1") int page,
                                 Model model,
                                 Authentication auth,
                                 RedirectAttributes redirect) {
        if (form.getId() == null) {
            bindingResult.reject("invalid", "Thiếu mã món");
        }
        if (bindingResult.hasErrors()) {
            return renderMenuPage(model, auth, keyword, page, form, true);
        }
        try {
            thucDonService.update(form.getId(), form.getTenMon(), form.getGiaTien());
            redirect.addFlashAttribute("success", "Cập nhật thành công");
            return "redirect:/admin/menu";
        } catch (IllegalArgumentException e) {
            applyMenuError(bindingResult, e.getMessage());
            return renderMenuPage(model, auth, keyword, page, form, true);
        }
    }

    /**
     * Menu delete.
     *
     * @param id id
     * @param redirect redirect
     * @return result
     */
    @GetMapping("/menu/delete/{id}")
    public String menuDelete(@PathVariable Long id, RedirectAttributes redirect) {
        try {
            thucDonService.deleteById(id);
            redirect.addFlashAttribute("success", "Xóa danh mục thành công");
        } catch (IllegalArgumentException | IllegalStateException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/admin/menu";
    }

    private String renderMenuPage(Model model,
                                  Authentication auth,
                                  String keyword,
                                  int page,
                                  ThucDonForm form,
                                  boolean editMode) {
        int pageSize = 5;
        List<ThucDon> allItems = thucDonService.searchByTenMon(keyword);
        allItems.sort(Comparator.comparing(
                item -> item.getTenMon() == null ? "" : item.getTenMon(),
                String.CASE_INSENSITIVE_ORDER
        ));
        int totalItems = allItems.size();
        int totalPages = (int) Math.ceil(totalItems / (double) pageSize);
        if (totalPages < 1) {
            totalPages = 1;
        }
        int currentPage = Math.min(Math.max(page, 1), totalPages);
        int fromIndex = (currentPage - 1) * pageSize;
        int toIndex = Math.min(fromIndex + pageSize, totalItems);
        List<ThucDon> pageItems = totalItems == 0 ? Collections.emptyList() : allItems.subList(fromIndex, toIndex);

        model.addAttribute("username", usernameFromAuth(auth));
        model.addAttribute("sidebarFragment", sidebar);
        model.addAttribute("contentFragment", "admin/menu");
        model.addAttribute("menuList", pageItems);
        model.addAttribute("keyword", keyword);
        model.addAttribute("menuForm", form);
        model.addAttribute("editMode", editMode);
        model.addAttribute("page", currentPage);
        model.addAttribute("totalPages", totalPages);
        model.addAttribute("totalItems", totalItems);
        model.addAttribute("pageSize", pageSize);
        model.addAttribute("activeMenu", "menu");
        if (keyword != null && !keyword.trim().isEmpty() && totalItems == 0) {
            model.addAttribute("error", "Không có trong cơ sở dữ liệu");
        }
        return "layout/base";
    }

    private void applyMenuError(BindingResult bindingResult, String message) {
        if (message == null || message.isBlank()) {
            return;
        }
        String lower = message.toLowerCase();
        if (lower.contains("tên món")) {
            bindingResult.rejectValue("tenMon", "invalid", message);
            return;
        }
        if (lower.contains("giá")) {
            bindingResult.rejectValue("giaTien", "invalid", message);
            return;
        }
        bindingResult.reject("invalid", message);
    }

    /**
     * Budget.
     *
     * @param from from
     * @param to to
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/budget")
    public String budget(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                         Model model, Authentication auth) {
        return renderBudgetPage(model, auth, from, to, new ChiTieuForm());
    }

    /**
     * Them chi.
     *
     * @param form form
     * @param principal principal
     * @param ra ra
     * @return result
     */
    @PostMapping("/budget/expense")
    public String themChi(@Valid @ModelAttribute("chiTieuForm") ChiTieuForm form,
                          BindingResult bindingResult,
                          Principal principal,
                          Model model,
                          Authentication auth,
                          RedirectAttributes ra) {
        if (bindingResult.hasErrors()) {
            return renderBudgetPage(model, auth, null, null, form);
        }
        String username = principal == null ? null : principal.getName();
        try {
            nganSachService.themChiTieu(form, username);
            ra.addFlashAttribute("success", "Thêm chi tiêu thành công");
            return "redirect:/admin/budget";
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("invalid", ex.getMessage());
            return renderBudgetPage(model, auth, null, null, form);
        }
    }

    private String renderBudgetPage(Model model,
                                    Authentication auth,
                                    LocalDate from,
                                    LocalDate to,
                                    ChiTieuForm form) {
        model.addAttribute("username", usernameFromAuth(auth));
        model.addAttribute("sidebarFragment", sidebar);
        model.addAttribute("contentFragment", "admin/budget");
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        if (from != null && to != null) {
            List<ThuChiDTO> thuChiList = nganSachService.xemThuChi(from, to);
            model.addAttribute("thuChiList", thuChiList);
            BigDecimal totalThu = BigDecimal.ZERO;
            BigDecimal totalChi = BigDecimal.ZERO;
            LocalDate maxThuDay = null;
            LocalDate maxChiDay = null;
            BigDecimal maxThu = BigDecimal.ZERO;
            BigDecimal maxChi = BigDecimal.ZERO;
            for (ThuChiDTO row : thuChiList) {
                BigDecimal thu = row.getThu() == null ? BigDecimal.ZERO : row.getThu();
                BigDecimal chi = row.getChi() == null ? BigDecimal.ZERO : row.getChi();
                totalThu = totalThu.add(thu);
                totalChi = totalChi.add(chi);
                if (thu.compareTo(maxThu) > 0) {
                    maxThu = thu;
                    maxThuDay = row.getNgay();
                }
                if (chi.compareTo(maxChi) > 0) {
                    maxChi = chi;
                    maxChiDay = row.getNgay();
                }
            }
            BigDecimal netAmount = totalThu.subtract(totalChi);
            BigDecimal chiThuRatio = null;
            if (totalThu.compareTo(BigDecimal.ZERO) > 0) {
                chiThuRatio = totalChi.multiply(new BigDecimal("100"))
                        .divide(totalThu, 2, RoundingMode.HALF_UP);
            }
            model.addAttribute("totalThu", totalThu);
            model.addAttribute("totalChi", totalChi);
            model.addAttribute("netAmount", netAmount);
            model.addAttribute("chiThuRatio", chiThuRatio);
            model.addAttribute("maxThuDay", maxThuDay);
            model.addAttribute("maxChiDay", maxChiDay);
            model.addAttribute("maxThu", maxThu);
            model.addAttribute("maxChi", maxChi);
        }
        model.addAttribute("chiTieuForm", form);
        model.addAttribute("activeMenu", "budget");
        return "layout/base";
    }

    /**
     * Reports.
     *
     * @param model model
     * @param auth auth
     * @return result
     */
    @GetMapping("/reports")
    public String reports(Model model, Authentication auth) {
        model.addAttribute("username", usernameFromAuth(auth));
        model.addAttribute("sidebarFragment", sidebar);
        model.addAttribute("contentFragment", "admin/reports");
        return "layout/base";
    }
}
