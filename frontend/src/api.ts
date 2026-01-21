const API_BASE = import.meta.env.VITE_API_BASE_URL || "http://localhost:8080/api"
const API_ROOT = API_BASE.replace(/\/api\/?$/, "")
const AUTH_MESSAGE_KEY = "auth_message"

let token: string | null = localStorage.getItem("token")

function setToken(t: string | null) {
  token = t
  if (t) localStorage.setItem("token", t)
  else localStorage.removeItem("token")
}

type RequestOptions = Omit<RequestInit, "body"> & { body?: any }

async function request<T>(path: string, options: RequestOptions = {}): Promise<T> {
  return requestUrl<T>(API_BASE + path, options)
}

async function requestRoot<T>(path: string, options: RequestOptions = {}): Promise<T> {
  return requestUrl<T>(API_ROOT + path, options)
}

async function requestUrl<T>(url: string, options: RequestOptions = {}): Promise<T> {
  const headers = new Headers(options.headers || {})
  if (token) headers.set("Authorization", `Bearer ${token}`)

  let body = options.body
  const isFormData = typeof FormData !== "undefined" && body instanceof FormData
  if (body !== undefined && body !== null && !isFormData) {
    if (typeof body === "object") {
      body = JSON.stringify(body)
    }
    if (!headers.has("Content-Type")) headers.set("Content-Type", "application/json")
  }

  const res = await fetch(url, { ...options, headers, body })
  const txt = await res.text()
  let data: any = null
  try {
    data = txt ? JSON.parse(txt) : null
  } catch (err) {
    data = txt
  }
  if (!res.ok) {
    const msg = (data && (data.message || data.error)) || res.statusText
    const error = new Error(typeof msg === "string" ? msg : JSON.stringify(msg))
    ;(error as any).status = res.status
    ;(error as any).body = typeof data === "string" ? data : JSON.stringify(data)
    if ((res.status === 401 || res.status === 403) && token) {
      localStorage.setItem(AUTH_MESSAGE_KEY, "Phien dang nhap het han. Vui long dang nhap lai.")
      setToken(null)
      localStorage.removeItem("auth_user")
      if (window.location.pathname !== "/login") {
        window.location.replace("/login")
      }
    }
    throw error
  }
  return data as T
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
    me: () => request<{ id: number; username: string; role: string; avatar: string | null; enabled: boolean }>("/users/me"),
    list: () => request<Array<{ id: number; username: string; role: string; avatar: string | null; enabled: boolean }>>("/users"),
    get: (id: number) => request<{ id: number; username: string; role: string; avatar: string | null; enabled: boolean }>(`/users/${id}`),
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
    list: (q?: string) =>
      request<Array<{ maNhanVien: number; hoTen: string; soDienThoai?: string; diaChi?: string; chucVu?: string; taiKhoanId?: number; enabled: boolean }>>(
        "/nhanvien" + (q ? `?q=${encodeURIComponent(q)}` : "")
      ),
    get: (id: number) =>
      request<{ maNhanVien: number; hoTen: string; soDienThoai?: string; diaChi?: string; chucVu?: string; taiKhoanId?: number; enabled: boolean }>(
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
    cancelReservation: (tableId: number) => requestRoot(`/sales/ban/${tableId}/cancel-reservation`, { method: "POST" }),
  },
}
