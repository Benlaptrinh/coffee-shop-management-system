import { useEffect, useMemo, useState } from "react"
import api from "../../api"
import { formatDate } from "../../utils/format"
import Pagination from "../../components/Pagination"

type Promotion = {
  maKhuyenMai: number
  tenKhuyenMai: string
  ngayBatDau: string
  ngayKetThuc: string
  giaTriGiam: number
}

export default function AdminMarketing() {
  const [items, setItems] = useState<Promotion[]>([])
  const [keyword, setKeyword] = useState("")
  const [editId, setEditId] = useState<number | null>(null)
  const [form, setForm] = useState({
    tenKhuyenMai: "",
    ngayBatDau: "",
    ngayKetThuc: "",
    giaTriGiam: "",
  })
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [page, setPage] = useState(1)
  const pageSize = 10

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.khuyenmai.list()
      setItems(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Failed to load promotions")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const filteredItems = useMemo(() => {
    if (!keyword.trim()) return items
    const lower = keyword.trim().toLowerCase()
    return items.filter((item) => item.tenKhuyenMai.toLowerCase().includes(lower))
  }, [items, keyword])

  useEffect(() => {
    setPage(1)
  }, [keyword])

  useEffect(() => {
    const totalPages = Math.max(1, Math.ceil(filteredItems.length / pageSize))
    if (page > totalPages) setPage(totalPages)
  }, [filteredItems.length, page, pageSize])

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    const payload = {
      tenKhuyenMai: form.tenKhuyenMai.trim(),
      ngayBatDau: form.ngayBatDau,
      ngayKetThuc: form.ngayKetThuc,
      giaTriGiam: Number(form.giaTriGiam || 0),
    }
    const errors: Record<string, string> = {}
    if (!payload.tenKhuyenMai) errors.tenKhuyenMai = "Ten khuyen mai khong duoc de trong"
    if (!payload.ngayBatDau) errors.ngayBatDau = "Ngay bat dau khong duoc de trong"
    if (!payload.ngayKetThuc) errors.ngayKetThuc = "Ngay ket thuc khong duoc de trong"
    if (!payload.giaTriGiam) {
      errors.giaTriGiam = "Phan tram giam gia khong hop le"
    } else if (payload.giaTriGiam < 1 || payload.giaTriGiam > 100) {
      errors.giaTriGiam = "Phan tram giam gia tu 1 den 100"
    }
    if (payload.ngayBatDau && payload.ngayKetThuc) {
      const from = new Date(payload.ngayBatDau)
      const to = new Date(payload.ngayKetThuc)
      if (!Number.isNaN(from.getTime()) && !Number.isNaN(to.getTime()) && from > to) {
        errors.ngayKetThuc = "Ngay ket thuc khong duoc truoc ngay bat dau"
      }
    }
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return
    try {
      if (editId) await api.khuyenmai.update(editId, payload)
      else await api.khuyenmai.create(payload)
      setEditId(null)
      setForm({ tenKhuyenMai: "", ngayBatDau: "", ngayKetThuc: "", giaTriGiam: "" })
      setFieldErrors({})
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Save failed")
    }
  }

  const onEdit = (item: Promotion) => {
    setEditId(item.maKhuyenMai)
    setForm({
      tenKhuyenMai: item.tenKhuyenMai,
      ngayBatDau: item.ngayBatDau,
      ngayKetThuc: item.ngayKetThuc,
      giaTriGiam: String(item.giaTriGiam),
    })
    setFieldErrors({})
  }

  const onDelete = async (item: Promotion) => {
    if (!window.confirm(`Xoa khuyen mai "${item.tenKhuyenMai}"?`)) return
    setError(null)
    try {
      await api.khuyenmai.delete(item.maKhuyenMai)
      if (editId === item.maKhuyenMai) {
        setEditId(null)
        setForm({ tenKhuyenMai: "", ngayBatDau: "", ngayKetThuc: "", giaTriGiam: "" })
      }
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Delete failed")
    }
  }

  const onReset = () => {
    setEditId(null)
    setForm({ tenKhuyenMai: "", ngayBatDau: "", ngayKetThuc: "", giaTriGiam: "" })
    setFieldErrors({})
  }

  return (
    <div className="content-wrapper">
      <h1>Danh sach khuyen mai</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="form-box">
        <h2>{editId ? "Chinh sua khuyen mai" : "Them khuyen mai"}</h2>
        <form onSubmit={onSubmit} noValidate>
          <div className="form-group">
            <label>Ten khuyen mai</label>
            <input
              value={form.tenKhuyenMai}
              onChange={(event) => {
                setForm((prev) => ({ ...prev, tenKhuyenMai: event.target.value }))
                if (fieldErrors.tenKhuyenMai) setFieldErrors((prev) => ({ ...prev, tenKhuyenMai: "" }))
              }}
            />
            {fieldErrors.tenKhuyenMai ? <div className="field-error">{fieldErrors.tenKhuyenMai}</div> : null}
          </div>
          <div className="form-group">
            <label>Ngay bat dau</label>
            <input
              type="date"
              value={form.ngayBatDau}
              onChange={(event) => {
                setForm((prev) => ({ ...prev, ngayBatDau: event.target.value }))
                if (fieldErrors.ngayBatDau) setFieldErrors((prev) => ({ ...prev, ngayBatDau: "" }))
              }}
            />
            {fieldErrors.ngayBatDau ? <div className="field-error">{fieldErrors.ngayBatDau}</div> : null}
          </div>
          <div className="form-group">
            <label>Ngay ket thuc</label>
            <input
              type="date"
              value={form.ngayKetThuc}
              onChange={(event) => {
                setForm((prev) => ({ ...prev, ngayKetThuc: event.target.value }))
                if (fieldErrors.ngayKetThuc) setFieldErrors((prev) => ({ ...prev, ngayKetThuc: "" }))
              }}
            />
            {fieldErrors.ngayKetThuc ? <div className="field-error">{fieldErrors.ngayKetThuc}</div> : null}
          </div>
          <div className="form-group">
            <label>% giam gia</label>
            <input
              type="number"
              min={1}
              max={100}
              value={form.giaTriGiam}
              onChange={(event) => {
                setForm((prev) => ({ ...prev, giaTriGiam: event.target.value }))
                if (fieldErrors.giaTriGiam) setFieldErrors((prev) => ({ ...prev, giaTriGiam: "" }))
              }}
            />
            {fieldErrors.giaTriGiam ? <div className="field-error">{fieldErrors.giaTriGiam}</div> : null}
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {editId ? "Save" : "Add"}
            </button>
            {editId ? (
              <button type="button" className="btn btn-cancel" onClick={onReset}>
                Cancel
              </button>
            ) : null}
          </div>
        </form>
      </div>

      <div className="action-bar">
        <form className="search-form" onSubmit={(event) => event.preventDefault()}>
          <input
            type="text"
            name="keyword"
            placeholder="Search promotions..."
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button className="btn btn-sm" type="button" onClick={() => setKeyword(keyword.trim())}>
            Search
          </button>
          <button className="btn btn-sm btn-cancel" type="button" onClick={() => setKeyword("")}>
            Reset
          </button>
        </form>
      </div>

      {loading ? (
        <div className="page-loading">Loading...</div>
      ) : (
        <table className="data-table table-actions">
          <thead>
            <tr>
              <th>Ten khuyen mai</th>
              <th>Ngay bat dau</th>
              <th>Ngay ket thuc</th>
              <th className="text-right">% giam gia</th>
              <th>Hanh dong</th>
            </tr>
          </thead>
          <tbody>
            {filteredItems.slice((page - 1) * pageSize, page * pageSize).map((km) => (
              <tr key={km.maKhuyenMai}>
                <td>{km.tenKhuyenMai}</td>
                <td>{formatDate(km.ngayBatDau)}</td>
                <td>{formatDate(km.ngayKetThuc)}</td>
                <td className="text-right">{km.giaTriGiam}%</td>
                <td className="action-buttons">
                  <button type="button" className="btn btn-sm btn-edit" onClick={() => onEdit(km)}>
                    Sua
                  </button>
                  <button type="button" className="btn btn-sm btn-delete" onClick={() => onDelete(km)}>
                    Xoa
                  </button>
                </td>
              </tr>
            ))}
            {filteredItems.length === 0 ? (
              <tr>
                <td colSpan={5} className="text-center text-muted">
                  No data
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
