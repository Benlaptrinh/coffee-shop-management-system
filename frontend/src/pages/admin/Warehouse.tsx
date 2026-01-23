import { useEffect, useMemo, useState } from "react"
import api from "../../api"
import { formatDateTime, formatDecimal, formatNumber, normalizeDecimal, toDigits } from "../../utils/format"
import Pagination from "../../components/Pagination"

type Unit = { maDonViTinh: number; tenDonVi: string }
type StockItem = {
  maHangHoa: number
  tenHangHoa: string
  soLuong: number
  donVi: string
  donGia: number
  ngayNhapGanNhat?: string
  ngayXuatGanNhat?: string
}

export default function AdminWarehouse() {
  const [items, setItems] = useState<StockItem[]>([])
  const [units, setUnits] = useState<Unit[]>([])
  const [keyword, setKeyword] = useState("")
  const [search, setSearch] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [editItem, setEditItem] = useState<StockItem | null>(null)
  const [page, setPage] = useState(1)
  const pageSize = 4

  const [nhapForm, setNhapForm] = useState({
    tenHangHoa: "",
    soLuongRaw: "",
    donGiaRaw: "",
    donViTinhId: "",
    ngayNhap: "",
  })
  const [xuatForm, setXuatForm] = useState({
    hangHoaId: "",
    soLuongRaw: "",
    ngayXuat: "",
  })
  const [editForm, setEditForm] = useState({
    id: "",
    tenHangHoa: "",
    soLuongRaw: "",
    donGiaRaw: "",
    donViTinhId: "",
  })
  const [nhapErrors, setNhapErrors] = useState<Record<string, string>>({})
  const [xuatErrors, setXuatErrors] = useState<Record<string, string>>({})
  const [editErrors, setEditErrors] = useState<Record<string, string>>({})

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.hanghoa.kho(search || undefined)
      setItems(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Tải kho hàng thất bại")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    api.donvitinh
      .list()
      .then((data) => setUnits(data))
      .catch(() => {})
  }, [])

  useEffect(() => {
    load()
  }, [search])

  const filteredItems = useMemo(() => items, [items])

  useEffect(() => {
    setPage(1)
  }, [search])

  useEffect(() => {
    const totalPages = Math.max(1, Math.ceil(filteredItems.length / pageSize))
    if (page > totalPages) setPage(totalPages)
  }, [filteredItems.length, page, pageSize])

  const onNhapSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    const payload = {
      tenHangHoa: nhapForm.tenHangHoa.trim(),
      soLuong: Number(nhapForm.soLuongRaw || 0),
      donViTinhId: nhapForm.donViTinhId ? Number(nhapForm.donViTinhId) : null,
      donGia: Number(nhapForm.donGiaRaw || 0),
      ngayNhap: nhapForm.ngayNhap,
    }
    const errors: Record<string, string> = {}
    if (!payload.tenHangHoa) errors.tenHangHoa = "Tên hàng không được để trống"
    if (!Number.isFinite(payload.soLuong) || payload.soLuong <= 0) errors.soLuong = "Số lượng phải lớn hơn 0"
    if (!payload.donGia || payload.donGia < 1) errors.donGia = "Đơn giá không hợp lệ"
    if (!payload.donViTinhId) errors.donViTinhId = "Vui lòng chọn đơn vị"
    if (!payload.ngayNhap) errors.ngayNhap = "Ngày nhập không được để trống"
    setNhapErrors(errors)
    if (Object.keys(errors).length > 0) return
    try {
      await api.hanghoa.nhap(payload)
      setNhapForm({ tenHangHoa: "", soLuongRaw: "", donGiaRaw: "", donViTinhId: "", ngayNhap: "" })
      setNhapErrors({})
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Nhập hàng thất bại")
    }
  }

  const onXuatSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    const payload = {
      hangHoaId: Number(xuatForm.hangHoaId || 0),
      soLuong: Number(xuatForm.soLuongRaw || 0),
      ngayXuat: xuatForm.ngayXuat,
    }
    const errors: Record<string, string> = {}
    if (!payload.hangHoaId) errors.hangHoaId = "Vui lòng chọn hàng hóa"
    if (!Number.isFinite(payload.soLuong) || payload.soLuong <= 0) errors.soLuong = "Số lượng phải lớn hơn 0"
    if (!payload.ngayXuat) errors.ngayXuat = "Ngày xuất không được để trống"
    setXuatErrors(errors)
    if (Object.keys(errors).length > 0) return
    try {
      await api.hanghoa.xuat(payload)
      setXuatForm({ hangHoaId: "", soLuongRaw: "", ngayXuat: "" })
      setXuatErrors({})
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Xuất hàng thất bại")
    }
  }

  const onEditSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!editItem) return
    setError(null)
    const payload = {
      id: Number(editForm.id),
      tenHangHoa: editForm.tenHangHoa.trim(),
      soLuong: Number(editForm.soLuongRaw || 0),
      donViTinhId: editForm.donViTinhId ? Number(editForm.donViTinhId) : null,
      donGia: Number(editForm.donGiaRaw || 0),
    }
    const errors: Record<string, string> = {}
    if (!payload.tenHangHoa) errors.tenHangHoa = "Tên hàng không được để trống"
    if (!Number.isFinite(payload.soLuong) || payload.soLuong <= 0) errors.soLuong = "Số lượng phải lớn hơn 0"
    if (!payload.donViTinhId) errors.donViTinhId = "Vui lòng chọn đơn vị"
    if (!payload.donGia || payload.donGia < 1) errors.donGia = "Đơn giá không hợp lệ"
    setEditErrors(errors)
    if (Object.keys(errors).length > 0) return
    try {
      await api.hanghoa.update(payload)
      setEditItem(null)
      setEditErrors({})
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Cập nhật thất bại")
    }
  }

  const onEdit = (item: StockItem) => {
    const matchUnit = units.find((u) => u.tenDonVi === item.donVi)
    setEditItem(item)
    setEditForm({
      id: String(item.maHangHoa),
      tenHangHoa: item.tenHangHoa,
      soLuongRaw: item.soLuong == null ? "" : String(item.soLuong),
      donGiaRaw: toDigits(String(item.donGia)),
      donViTinhId: matchUnit ? String(matchUnit.maDonViTinh) : "",
    })
    setEditErrors({})
  }

  const onDelete = async (id: number) => {
    if (!window.confirm("Xóa mục này?")) return
    try {
      await api.hanghoa.delete(id)
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Xóa thất bại")
    }
  }

  return (
    <div className="content-wrapper">
      <h1>Kho hàng</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="warehouse-panels">
        {!editItem ? (
          <div className="form-box">
            <h2 style={{ margin: "0 0 10px" }}>Nhập hàng hóa</h2>
            <form onSubmit={onNhapSubmit} noValidate>
              <div className="form-group">
                <label>Tên hàng</label>
                <input
                  value={nhapForm.tenHangHoa}
                  onChange={(event) => {
                    setNhapForm((prev) => ({ ...prev, tenHangHoa: event.target.value }))
                    if (nhapErrors.tenHangHoa) setNhapErrors((prev) => ({ ...prev, tenHangHoa: "" }))
                  }}
                />
                {nhapErrors.tenHangHoa ? <div className="field-error">{nhapErrors.tenHangHoa}</div> : null}
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Số lượng</label>
                  <input
                    inputMode="decimal"
                    value={formatDecimal(nhapForm.soLuongRaw)}
                    onChange={(event) => {
                      setNhapForm((prev) => ({ ...prev, soLuongRaw: normalizeDecimal(event.target.value) }))
                      if (nhapErrors.soLuong) setNhapErrors((prev) => ({ ...prev, soLuong: "" }))
                    }}
                  />
                  {nhapErrors.soLuong ? <div className="field-error">{nhapErrors.soLuong}</div> : null}
                </div>
                <div className="form-group">
                  <label>Đơn giá</label>
                  <input
                    inputMode="numeric"
                    value={formatNumber(nhapForm.donGiaRaw)}
                    onChange={(event) => {
                      setNhapForm((prev) => ({ ...prev, donGiaRaw: toDigits(event.target.value) }))
                      if (nhapErrors.donGia) setNhapErrors((prev) => ({ ...prev, donGia: "" }))
                    }}
                  />
                  {nhapErrors.donGia ? <div className="field-error">{nhapErrors.donGia}</div> : null}
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Đơn vị</label>
                  <select
                    value={nhapForm.donViTinhId}
                    onChange={(event) => {
                      setNhapForm((prev) => ({ ...prev, donViTinhId: event.target.value }))
                      if (nhapErrors.donViTinhId) setNhapErrors((prev) => ({ ...prev, donViTinhId: "" }))
                    }}
                  >
                    <option value="">-- Chọn --</option>
                    {units.map((unit) => (
                      <option key={unit.maDonViTinh} value={unit.maDonViTinh}>
                        {unit.tenDonVi}
                      </option>
                    ))}
                  </select>
                  {nhapErrors.donViTinhId ? <div className="field-error">{nhapErrors.donViTinhId}</div> : null}
                </div>
                <div className="form-group">
                  <label>Ngày nhập</label>
                  <input
                    type="datetime-local"
                    value={nhapForm.ngayNhap}
                    onChange={(event) => {
                      setNhapForm((prev) => ({ ...prev, ngayNhap: event.target.value }))
                      if (nhapErrors.ngayNhap) setNhapErrors((prev) => ({ ...prev, ngayNhap: "" }))
                    }}
                  />
                  {nhapErrors.ngayNhap ? <div className="field-error">{nhapErrors.ngayNhap}</div> : null}
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">
                  Nhập hàng
                </button>
              </div>
            </form>
          </div>
        ) : (
          <div className="form-box">
            <h2 style={{ margin: "0 0 10px" }}>Chỉnh sửa hàng hóa</h2>
            <form onSubmit={onEditSubmit} noValidate>
              <div className="form-group">
                <label>Tên hàng</label>
                <input
                  value={editForm.tenHangHoa}
                  onChange={(event) => {
                    setEditForm((prev) => ({ ...prev, tenHangHoa: event.target.value }))
                    if (editErrors.tenHangHoa) setEditErrors((prev) => ({ ...prev, tenHangHoa: "" }))
                  }}
                />
                {editErrors.tenHangHoa ? <div className="field-error">{editErrors.tenHangHoa}</div> : null}
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Số lượng</label>
                  <input
                    inputMode="decimal"
                    value={formatDecimal(editForm.soLuongRaw)}
                    onChange={(event) => {
                      setEditForm((prev) => ({ ...prev, soLuongRaw: normalizeDecimal(event.target.value) }))
                      if (editErrors.soLuong) setEditErrors((prev) => ({ ...prev, soLuong: "" }))
                    }}
                  />
                  {editErrors.soLuong ? <div className="field-error">{editErrors.soLuong}</div> : null}
                </div>
                <div className="form-group">
                  <label>Đơn giá</label>
                  <input
                    inputMode="numeric"
                    value={formatNumber(editForm.donGiaRaw)}
                    onChange={(event) => {
                      setEditForm((prev) => ({ ...prev, donGiaRaw: toDigits(event.target.value) }))
                      if (editErrors.donGia) setEditErrors((prev) => ({ ...prev, donGia: "" }))
                    }}
                  />
                  {editErrors.donGia ? <div className="field-error">{editErrors.donGia}</div> : null}
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Đơn vị</label>
                  <select
                    value={editForm.donViTinhId}
                    onChange={(event) => {
                      setEditForm((prev) => ({ ...prev, donViTinhId: event.target.value }))
                      if (editErrors.donViTinhId) setEditErrors((prev) => ({ ...prev, donViTinhId: "" }))
                    }}
                  >
                    <option value="">-- Chọn --</option>
                    {units.map((unit) => (
                      <option key={unit.maDonViTinh} value={unit.maDonViTinh}>
                        {unit.tenDonVi}
                      </option>
                    ))}
                  </select>
                  {editErrors.donViTinhId ? <div className="field-error">{editErrors.donViTinhId}</div> : null}
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">
                  Lưu
                </button>
                <button
                  type="button"
                  className="btn btn-cancel"
                  onClick={() => {
                    setEditItem(null)
                    setEditErrors({})
                  }}
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        )}

        <div className="form-box">
          <h2 style={{ margin: "0 0 10px" }}>Xuất hàng hóa</h2>
          <form onSubmit={onXuatSubmit} noValidate>
            <div className="form-group">
              <label>Hàng hóa</label>
              <select
                value={xuatForm.hangHoaId}
                onChange={(event) => {
                  setXuatForm((prev) => ({ ...prev, hangHoaId: event.target.value }))
                  if (xuatErrors.hangHoaId) setXuatErrors((prev) => ({ ...prev, hangHoaId: "" }))
                }}
              >
                <option value="">-- Chọn --</option>
                {items.map((item) => (
                  <option key={item.maHangHoa} value={item.maHangHoa}>
                    {item.tenHangHoa} | {item.donVi} (Tồn: {formatDecimal(item.soLuong)})
                  </option>
                ))}
              </select>
              {xuatErrors.hangHoaId ? <div className="field-error">{xuatErrors.hangHoaId}</div> : null}
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>Số lượng xuất</label>
                  <input
                    inputMode="decimal"
                    value={formatDecimal(xuatForm.soLuongRaw)}
                    onChange={(event) => {
                      setXuatForm((prev) => ({ ...prev, soLuongRaw: normalizeDecimal(event.target.value) }))
                      if (xuatErrors.soLuong) setXuatErrors((prev) => ({ ...prev, soLuong: "" }))
                    }}
                  />
                {xuatErrors.soLuong ? <div className="field-error">{xuatErrors.soLuong}</div> : null}
              </div>
              <div className="form-group">
                <label>Ngày xuất</label>
                <input
                  type="datetime-local"
                  value={xuatForm.ngayXuat}
                  onChange={(event) => {
                    setXuatForm((prev) => ({ ...prev, ngayXuat: event.target.value }))
                    if (xuatErrors.ngayXuat) setXuatErrors((prev) => ({ ...prev, ngayXuat: "" }))
                  }}
                />
                {xuatErrors.ngayXuat ? <div className="field-error">{xuatErrors.ngayXuat}</div> : null}
              </div>
            </div>
            <div className="form-actions" style={{ marginTop: 8 }}>
              <button type="submit" className="btn btn-delete">
                Xuất hàng
              </button>
            </div>
          </form>
        </div>
      </div>

      <form className="mb-3" style={{ marginTop: 12 }} onSubmit={(event) => (event.preventDefault(), setSearch(keyword.trim()))}>
        <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
          <input
            type="text"
            name="keyword"
            placeholder="Tìm kiếm..."
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button className="btn btn-secondary" type="submit">
            Tìm kiếm
          </button>
          <button className="btn btn-outline-secondary" type="button" onClick={() => (setKeyword(""), setSearch(""))}>
            Đặt lại
          </button>
        </div>
      </form>

      {loading ? (
        <div className="page-loading">Đang tải...</div>
      ) : (
        <table className="data-table table-actions">
          <thead>
            <tr>
              <th>Tên hàng</th>
              <th>Số lượng</th>
              <th>Đơn vị</th>
              <th className="text-right">Đơn giá</th>
              <th>Ngày nhập gần nhất</th>
              <th>Ngày xuất gần nhất</th>
              <th className="text-center">Hành động</th>
            </tr>
          </thead>
          <tbody>
            {filteredItems.slice((page - 1) * pageSize, page * pageSize).map((item) => (
              <tr key={item.maHangHoa}>
                <td>{item.tenHangHoa}</td>
                <td>{formatDecimal(item.soLuong)}</td>
                <td>{item.donVi}</td>
                <td className="text-right">{formatNumber(item.donGia)}</td>
                <td>{formatDateTime(item.ngayNhapGanNhat)}</td>
                <td>{formatDateTime(item.ngayXuatGanNhat)}</td>
                <td className="action-buttons">
                  <button type="button" className="btn btn-sm btn-edit" onClick={() => onEdit(item)}>
                    Sửa
                  </button>
                  <button type="button" className="btn btn-sm btn-delete" onClick={() => onDelete(item.maHangHoa)}>
                    Xóa
                  </button>
                </td>
              </tr>
            ))}
            {filteredItems.length === 0 ? (
              <tr>
                <td colSpan={7} className="text-center text-muted">
                  Không có dữ liệu
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      )}
      {filteredItems.length > 0 ? (
        <Pagination page={page} pageSize={pageSize} total={filteredItems.length} onPageChange={setPage} />
      ) : null}
    </div>
  )
}
