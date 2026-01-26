// @ts-ignore
import axios from "axios"
import type { UserDto, NhanVienDto } from "./types/api"

const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api"
const AUTH_MESSAGE_KEY = "auth_message"

const instance = axios.create({
  baseURL: API_BASE,
  headers: { "Content-Type": "application/json" },
  timeout: 15000,
})

let token: string | null = localStorage.getItem("token")

function setToken(t: string | null) {
  token = t
  if (t) localStorage.setItem("token", t)
  else localStorage.removeItem("token")
  instance.defaults.headers.common["Authorization"] = t ? `Bearer ${t}` : undefined
}

instance.interceptors.request.use((config: any) => {
  if (token) config.headers = { ...config.headers, Authorization: `Bearer ${token}` }
  return config
})

instance.interceptors.response.use(
  (res: any) => res,
  (err: any) => {
    const res = err.response
    // Do NOT perform direct navigation here — emit event for UI to handle
    if (res && (res.status === 401 || res.status === 403)) {
      // store a message for UI if needed
      localStorage.setItem(AUTH_MESSAGE_KEY, "Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.")
      // emit unauthorized event
      apiEvents.emit("unauthorized")
    }
    return Promise.reject(err)
  }
)

// simple event emitter for api events (unauthorized)
const apiEvents = {
  handlers: {} as Record<string, Array<() => void>>,
  on(event: string, fn: () => void) {
    if (!this.handlers[event]) this.handlers[event] = []
    this.handlers[event].push(fn)
    return () => {
      this.handlers[event] = this.handlers[event].filter((h) => h !== fn)
    }
  },
  emit(event: string) {
    ;(this.handlers[event] || []).forEach((fn) => {
      try { fn() } catch (_) {}
    })
  },
}

async function request<T = any>(path: string, options: any = {}): Promise<T> {
  const method = (options.method || "GET").toUpperCase()
  const url = path
  const config: any = { method, url }
  if (options.body) config.data = options.body
  if (options.params) config.params = options.params
  const res = await instance.request(config)
  return res.data
}

export default {
  setToken,
  auth: {
    login: (payload: { username: string; password: string }) =>
      request<{ token: string; tokenType: string; expiresIn: number; username: string; roles: string[] }>(
        "/auth/login",
        { method: "POST", body: payload }
      ),
  },
  users: {
    me: () => request<UserDto>("/users/me"),
    list: () => request<UserDto[]>("/users"),
    get: (id: number) => request<UserDto>(`/users/${id}`),
    create: (payload: { username: string; password: string; role: string; avatar?: string; enabled?: boolean }) =>
      request("/users", { method: "POST", body: payload }),
    update: (id: number, payload: { password?: string; role?: string; avatar?: string; enabled?: boolean }) =>
      request(`/users/${id}`, { method: "PUT", body: payload }),
    disable: (id: number, enabled?: boolean) =>
      request(`/users/${id}/disable`, { method: "PATCH", body: enabled === undefined ? undefined : { enabled } }),
    changePassword: (payload: { oldPassword: string; newPassword: string }) =>
      request("/users/me/change-password", { method: "POST", body: payload }),
  },
  nhanvien: {
    list: (params?: { q?: string }) =>
      request<NhanVienDto[]>("/nhanvien", { method: "GET", params }),
    get: (id: number) =>
      request<{ maNhanVien: number; hoTen: string; soDienThoai?: string; diaChi?: string; chucVu?: string; chucVuId?: number; taiKhoanId?: number; enabled: boolean }>(
        `/nhanvien/${id}`
      ),
    create: (payload: any) => request("/nhanvien", { method: "POST", body: payload }),
    update: (id: number, payload: any) => request(`/nhanvien/${id}`, { method: "PUT", body: payload }),
    delete: (id: number) => request(`/nhanvien/${id}`, { method: "DELETE" }),
  },
  thucdon: {
    list: (q?: string) =>
      request<Array<{ maThucDon: number; tenMon: string; giaHienTai: number; loaiMon?: string }>>(
        "/thucdon" + (q ? `?q=${encodeURIComponent(q)}` : "")
      ),
    get: (id: number) =>
      request<{ maThucDon: number; tenMon: string; giaHienTai: number; loaiMon?: string }>(`/thucdon/${id}`),
    create: (payload: { tenMon: string; giaHienTai: number }) => request("/thucdon", { method: "POST", body: payload }),
    update: (id: number, payload: { tenMon: string; giaHienTai: number }) =>
      request(`/thucdon/${id}`, { method: "PUT", body: payload }),
    delete: (id: number) => request(`/thucdon/${id}`, { method: "DELETE" }),
  },
  khuyenmai: {
    list: () => request<Array<{ maKhuyenMai: number; tenKhuyenMai: string; ngayBatDau: string; ngayKetThuc: string; giaTriGiam: number }>>("/khuyenmai"),
    get: (id: number) => request<{ id: number; tenKhuyenMai: string; ngayBatDau: string; ngayKetThuc: string; giaTriGiam: number }>(`/khuyenmai/${id}`),
    create: (payload: { tenKhuyenMai: string; ngayBatDau: string; ngayKetThuc: string; giaTriGiam: number }) =>
      request("/khuyenmai", { method: "POST", body: payload }),
    update: (id: number, payload: { tenKhuyenMai: string; ngayBatDau: string; ngayKetThuc: string; giaTriGiam: number }) =>
      request(`/khuyenmai/${id}`, { method: "PUT", body: payload }),
    delete: (id: number) => request(`/khuyenmai/${id}`, { method: "DELETE" }),
  },
  report: {
    finance: (from: string, to: string) =>
      request<Array<{ ngay: string; thu: number; chi: number }>>(
        `/report/finance?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`
      ),
    sales: (from: string, to: string) =>
      request<Array<{ ngay: string; soHoaDon: number; doanhThu: number }>>(
        `/report/sales?from=${encodeURIComponent(from)}&to=${encodeURIComponent(to)}`
      ),
    staff: () => request<Array<{ trangThai: string; soLuong: number }>>("/report/staff"),
  },
  thietbi: {
    list: () => request<Array<{ maThietBi: number; tenThietBi: string; soLuong: number; donGiaMua: number; ngayMua: string; ghiChu?: string }>>("/thietbi"),
    get: (id: number) =>
      request<{ maThietBi: number; tenThietBi: string; soLuong: number; donGiaMua: number; ngayMua: string; ghiChu?: string }>(`/thietbi/${id}`),
    create: (payload: any) => request("/thietbi", { method: "POST", body: payload }),
    update: (id: number, payload: any) => request(`/thietbi/${id}`, { method: "PUT", body: payload }),
    delete: (id: number) => request(`/thietbi/${id}`, { method: "DELETE" }),
  },
  hanghoa: {
    kho: (q?: string) =>
      request<
        Array<{
          maHangHoa: number
          tenHangHoa: string
          soLuong: number
          donVi: string
          donGia: number
          ngayNhapGanNhat?: string
          ngayXuatGanNhat?: string
        }>
      >("/hanghoa/kho" + (q ? `?q=${encodeURIComponent(q)}` : "")),
    nhap: (payload: any) => request("/hanghoa/nhap", { method: "POST", body: payload }),
    xuat: (payload: { hangHoaId: number; soLuong: number; ngayXuat: string }) =>
      request(`/hanghoa/xuat?hangHoaId=${payload.hangHoaId}&soLuong=${payload.soLuong}&ngayXuat=${encodeURIComponent(payload.ngayXuat)}`, { method: "POST" }),
    update: (payload: any) => request("/hanghoa", { method: "PUT", body: payload }),
    delete: (id: number) => request(`/hanghoa/${id}`, { method: "DELETE" }),
  },
  donvitinh: {
    list: () => request<Array<{ maDonViTinh: number; tenDonVi: string }>>("/donvitinh"),
    create: (payload: { tenDonVi: string }) => request("/donvitinh", { method: "POST", body: payload }),
    delete: (id: number) => request(`/donvitinh/${id}`, { method: "DELETE" }),
  },
  chucvu: {
    list: () => request<Array<{ maChucVu: number; tenChucVu: string; luong?: string }>>("/chucvu"),
    create: (payload: { tenChucVu: string }) => request("/chucvu", { method: "POST", body: payload }),
  },
  chitieu: {
    report: (from: string, to: string) =>
      request<Array<{ ngay: string; thu: number; chi: number }>>(`/chitieu/report?from=${from}&to=${to}`),
    create: (payload: { ngayChi: string; tenKhoanChi: string; soTien: number }) => request("/chitieu", { method: "POST", body: payload }),
  },
  sales: {
    tables: () => request<Array<{ maBan: number; tenBan: string; tinhTrang: string }>>("/sales/tables"),
    table: (id: number) =>
      request<{
        table: { maBan: number; tenBan: string; tinhTrang: string }
        reservation: any | null
        invoice: {
          maHoaDon: number
          maBan: number
          tinhTrang: string
          ngayGioTao: string
          ngayThanhToan?: string
          tongTien: number
          tenNhanVien?: string
          tenKhachDat?: string
          items: Array<{ maThucDon: number; tenMon: string; soLuong: number; giaTaiThoiDiemBan: number; thanhTien: number }>
        } | null
      }>(`/sales/tables/${id}`),
    menu: () => request<Array<{ maThucDon: number; tenMon: string; giaHienTai: number; loaiMon?: string }>>("/sales/menu"),
    menuSelection: (tableId: number, params: Record<string, string>) =>
      request(`/sales/tables/${tableId}/menu-selection`, { method: "POST", body: { params } }),
    addItem: (tableId: number, payload: { thucDonId: number; soLuong: number }) =>
      request(`/sales/tables/${tableId}/items`, { method: "POST", body: payload }),
    pay: (tableId: number, payload: { amountPaid: number; releaseTable?: boolean }) =>
      request(`/sales/tables/${tableId}/pay`, { method: "POST", body: payload }),
    cancel: (tableId: number) => request(`/sales/tables/${tableId}/cancel`, { method: "POST" }),
    reserve: (tableId: number, payload: { tenKhach: string; sdt: string; ngayGio: string }) =>
      request(`/sales/tables/${tableId}/reserve`, { method: "POST", body: payload }),
    move: (payload: { fromBanId: number; toBanId: number }) => request("/sales/move", { method: "POST", body: payload }),
    merge: (payload: { targetBanId: number; sourceBanId: number }) => request("/sales/merge", { method: "POST", body: payload }),
    split: (payload: { toBanId: number; items: Array<{ fromBanId: number; thucDonId: number; soLuong: number }> }) =>
      request("/sales/split", { method: "POST", body: payload }),
    invoice: (id: number) =>
      request<{
        maHoaDon: number
        maBan: number
        tinhTrang: string
        ngayGioTao: string
        ngayThanhToan?: string
        tongTien: number
        tenNhanVien?: string
        tenKhachDat?: string
        items: Array<{ maThucDon: number; tenMon: string; soLuong: number; giaTaiThoiDiemBan: number; thanhTien: number }>
      }>(`/sales/invoices/${id}`),
    cancelReservation: (tableId: number) => request(`/sales/tables/${tableId}/cancel-reservation`, { method: "POST" }),
  },
}
