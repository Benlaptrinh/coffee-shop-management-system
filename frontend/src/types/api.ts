export type UserDto = {
  id: number
  username: string
  role: string
  avatar?: string | null
  enabled: boolean
}

export type NhanVienDto = {
  maNhanVien: number
  hoTen: string
  soDienThoai?: string
  diaChi?: string
  chucVu?: string
  chucVuId?: number
  taiKhoanId?: number
  enabled: boolean
}

export type InvoiceDto = {
  maHoaDon: number
  maBan: number
  tinhTrang: string
  ngayGioTao: string
  ngayThanhToan?: string
  tongTien: number
  tenNhanVien?: string
  tenKhachDat?: string
  items: Array<{
    maThucDon: number
    tenMon: string
    soLuong: number
    giaTaiThoiDiemBan: number
    thanhTien: number
  }>
}


