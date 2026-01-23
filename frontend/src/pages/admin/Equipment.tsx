import { useEffect, useState } from "react"
import api from "../../api"
import { formatDate, formatNumber, toDigits } from "../../utils/format"
import Pagination from "../../components/Pagination"

type Equipment = {
  maThietBi: number
  tenThietBi: string
  soLuong: number
  donGiaMua: number
  ngayMua: string
  ghiChu?: string
}

export default function AdminEquipment() {
  const [items, setItems] = useState<Equipment[]>([])
  const [keyword, setKeyword] = useState("")
  const [search, setSearch] = useState("")
  const [editId, setEditId] = useState<number | null>(null)
  const [form, setForm] = useState({
    tenThietBi: "",
    ngayMua: "",
    soLuong: "1",
    donGiaRaw: "",
    ghiChu: "",
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [page, setPage] = useState(1)
  const pageSize = 5

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.thietbi.list()
      setItems(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Tải thiết bị thất bại")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    const payload = {
      tenThietBi: form.tenThietBi.trim(),
      ngayMua: form.ngayMua,
      soLuong: Number(form.soLuong || 0),
      donGiaMua: Number(form.donGiaRaw || 0),
      ghiChu: form.ghiChu,
    }
    const errors: Record<string, string> = {}
    if (!payload.tenThietBi) errors.tenThietBi = "Tên thiết bị không được để trống"
    if (!payload.ngayMua) errors.ngayMua = "Ngày mua không được để trống"
    if (!payload.soLuong || payload.soLuong < 1) errors.soLuong = "Số lượng phải lớn hơn 0"
    if (!payload.donGiaMua || payload.donGiaMua < 1) errors.donGiaRaw = "Đơn giá không hợp lệ"
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return
    try {
      if (editId) await api.thietbi.update(editId, payload)
      else await api.thietbi.create(payload)
      setEditId(null)
      setForm({ tenThietBi: "", ngayMua: "", soLuong: "1", donGiaRaw: "", ghiChu: "" })
      setFieldErrors({})
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Lưu thất bại")
    }
  }

  const onEdit = (item: Equipment) => {
    setEditId(item.maThietBi)
    setForm({
      tenThietBi: item.tenThietBi,
      ngayMua: item.ngayMua,
      soLuong: String(item.soLuong),
      donGiaRaw: toDigits(String(item.donGiaMua)),
      ghiChu: item.ghiChu || "",
    })
    setFieldErrors({})
  }

  const onDelete = async (id: number) => {
    if (!window.confirm("Xóa mục này?")) return
    try {
      await api.thietbi.delete(id)
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Xóa thất bại")
    }
  }

  const filtered = items.filter((item) => (search ? item.tenThietBi.toLowerCase().includes(search.toLowerCase()) : true))

  useEffect(() => {
    setPage(1)
  }, [search])

  useEffect(() => {
    const totalPages = Math.max(1, Math.ceil(filtered.length / pageSize))
    if (page > totalPages) setPage(totalPages)
  }, [filtered.length, page, pageSize])

  return (
    <div className="content-wrapper">
      <h1>Quản lý thiết bị</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="form-box">
        <h2>{editId ? "Chỉnh sửa thiết bị" : "Thêm thiết bị"}</h2>
        <form onSubmit={onSubmit} noValidate>
          <div className="form-row">
            <div className="form-group">
              <label>Tên thiết bị</label>
              <input
                value={form.tenThietBi}
                onChange={(event) => {
                  setForm((prev) => ({ ...prev, tenThietBi: event.target.value }))
                  if (fieldErrors.tenThietBi) setFieldErrors((prev) => ({ ...prev, tenThietBi: "" }))
                }}
              />
              {fieldErrors.tenThietBi ? <div className="field-error">{fieldErrors.tenThietBi}</div> : null}
            </div>
            <div className="form-group">
              <label>Ngày mua</label>
              <input
                type="date"
                value={form.ngayMua}
                onChange={(event) => {
                  setForm((prev) => ({ ...prev, ngayMua: event.target.value }))
                  if (fieldErrors.ngayMua) setFieldErrors((prev) => ({ ...prev, ngayMua: "" }))
                }}
              />
              {fieldErrors.ngayMua ? <div className="field-error">{fieldErrors.ngayMua}</div> : null}
            </div>
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>Số lượng</label>
              <input
                type="number"
                min={1}
                max={10}
                value={form.soLuong}
                onChange={(event) => {
                  setForm((prev) => ({ ...prev, soLuong: event.target.value }))
                  if (fieldErrors.soLuong) setFieldErrors((prev) => ({ ...prev, soLuong: "" }))
                }}
              />
              {fieldErrors.soLuong ? <div className="field-error">{fieldErrors.soLuong}</div> : null}
            </div>
            <div className="form-group">
              <label>Đơn giá mua</label>
              <input
                inputMode="numeric"
                value={formatNumber(form.donGiaRaw)}
                onChange={(event) => {
                  setForm((prev) => ({ ...prev, donGiaRaw: toDigits(event.target.value) }))
                  if (fieldErrors.donGiaRaw) setFieldErrors((prev) => ({ ...prev, donGiaRaw: "" }))
                }}
              />
              {fieldErrors.donGiaRaw ? <div className="field-error">{fieldErrors.donGiaRaw}</div> : null}
            </div>
          </div>
          <div className="form-group">
            <label>Ghi chú</label>
            <textarea rows={3} value={form.ghiChu} onChange={(event) => setForm((prev) => ({ ...prev, ghiChu: event.target.value }))} />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {editId ? "Lưu" : "Thêm"}
            </button>
            {editId ? (
              <button type="button" className="btn btn-cancel" onClick={() => setEditId(null)}>
                Hủy
              </button>
            ) : null}
          </div>
        </form>
      </div>

      <div className="action-bar">
        <form className="search-form" onSubmit={(event) => (event.preventDefault(), setSearch(keyword.trim()))}>
          <input
            type="text"
            name="keyword"
            placeholder="Tìm thiết bị..."
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button className="btn btn-sm" type="submit">
            Tìm kiếm
          </button>
          <button
            className="btn btn-sm btn-cancel"
            type="button"
            onClick={() => {
              setKeyword("")
              setSearch("")
            }}
          >
            Đặt lại
          </button>
        </form>
      </div>

      {loading ? (
        <div className="page-loading">Đang tải...</div>
      ) : (
        <table className="data-table table-actions">
          <thead>
            <tr>
              <th>Tên</th>
              <th>Ngày mua</th>
              <th>Số lượng</th>
              <th className="text-right">Đơn giá</th>
              <th className="text-right">Tổng</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {filtered.slice((page - 1) * pageSize, page * pageSize).map((item) => (
              <tr key={item.maThietBi}>
                <td>{item.tenThietBi}</td>
                <td>{formatDate(item.ngayMua)}</td>
                <td>{item.soLuong}</td>
                <td className="text-right">{formatNumber(item.donGiaMua)}</td>
                <td className="text-right">{formatNumber(item.donGiaMua * item.soLuong)}</td>
                <td className="action-buttons">
                  <button type="button" className="btn btn-sm btn-edit" onClick={() => onEdit(item)}>
                    Sửa
                  </button>
                  <button type="button" className="btn btn-sm btn-delete" onClick={() => onDelete(item.maThietBi)}>
                    Xóa
                  </button>
                </td>
              </tr>
            ))}
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center text-muted">
                  Không có dữ liệu
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      )}
      {filtered.length > 0 ? <Pagination page={page} pageSize={pageSize} total={filtered.length} onPageChange={setPage} /> : null}
    </div>
  )
}
