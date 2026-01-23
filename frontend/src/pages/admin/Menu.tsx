import { useEffect, useState } from "react"
import api from "../../api"
import { formatNumber, toDigits } from "../../utils/format"
import Pagination from "../../components/Pagination"

type MenuItem = {
  maThucDon: number
  tenMon: string
  giaHienTai: number
}

export default function AdminMenu() {
  const [items, setItems] = useState<MenuItem[]>([])
  const [keyword, setKeyword] = useState("")
  const [search, setSearch] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [editId, setEditId] = useState<number | null>(null)
  const [form, setForm] = useState({ tenMon: "", giaRaw: "" })
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [page, setPage] = useState(1)
  const pageSize = 8

  const load = async (q?: string) => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.thucdon.list(q)
      setItems(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Tải thực đơn thất bại")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(search)
  }, [search])

  useEffect(() => {
    setPage(1)
  }, [search])

  useEffect(() => {
    const totalPages = Math.max(1, Math.ceil(items.length / pageSize))
    if (page > totalPages) setPage(totalPages)
  }, [items.length, page, pageSize])

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    const price = Number(form.giaRaw || 0)
    const errors: Record<string, string> = {}
    if (!form.tenMon.trim()) errors.tenMon = "Tên món không được để trống"
    if (!price) errors.giaRaw = "Giá tiền không hợp lệ"
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return
    try {
      if (editId) {
        await api.thucdon.update(editId, { tenMon: form.tenMon.trim(), giaHienTai: price })
      } else {
        await api.thucdon.create({ tenMon: form.tenMon.trim(), giaHienTai: price })
      }
      setForm({ tenMon: "", giaRaw: "" })
      setEditId(null)
      setFieldErrors({})
      await load(search)
    } catch (err: any) {
      setError(err?.body || err?.message || "Lưu thất bại")
    }
  }

  const onEdit = (item: MenuItem) => {
    setEditId(item.maThucDon)
    setForm({ tenMon: item.tenMon, giaRaw: toDigits(String(item.giaHienTai)) })
    setFieldErrors({})
  }

  const onDelete = async (id: number) => {
    if (!window.confirm("Xóa mục này?")) return
    try {
      await api.thucdon.delete(id)
      await load(search)
    } catch (err: any) {
      setError(err?.body || err?.message || "Xóa thất bại")
    }
  }

  const onResetForm = () => {
    setEditId(null)
    setForm({ tenMon: "", giaRaw: "" })
    setFieldErrors({})
  }

  return (
    <div className="content-wrapper">
      <h1>Danh sách thực đơn</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="form-box">
        <h2>{editId ? "Chỉnh sửa món" : "Thêm món"}</h2>
        <form onSubmit={onSubmit} noValidate>
          <div className="form-group">
            <label>Tên món</label>
            <input
              value={form.tenMon}
              onChange={(event) => {
                setForm((prev) => ({ ...prev, tenMon: event.target.value }))
                if (fieldErrors.tenMon) setFieldErrors((prev) => ({ ...prev, tenMon: "" }))
              }}
            />
            {fieldErrors.tenMon ? <div className="field-error">{fieldErrors.tenMon}</div> : null}
          </div>
          <div className="form-group">
            <label>Giá tiền</label>
            <input
              value={formatNumber(form.giaRaw)}
              inputMode="numeric"
              onChange={(event) => {
                setForm((prev) => ({ ...prev, giaRaw: toDigits(event.target.value) }))
                if (fieldErrors.giaRaw) setFieldErrors((prev) => ({ ...prev, giaRaw: "" }))
              }}
            />
            {fieldErrors.giaRaw ? <div className="field-error">{fieldErrors.giaRaw}</div> : null}
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {editId ? "Lưu" : "Thêm"}
            </button>
            {editId ? (
              <button type="button" className="btn btn-cancel" onClick={onResetForm}>
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
            placeholder="Tìm thực đơn..."
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button className="btn btn-sm" type="submit">
            Tìm kiếm
          </button>
          <button className="btn btn-sm btn-cancel" type="button" onClick={() => (setKeyword(""), setSearch(""))}>
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
              <th>STT</th>
              <th>Tên món</th>
              <th className="text-right">Giá tiền</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {items.slice((page - 1) * pageSize, page * pageSize).map((item, index) => (
              <tr key={item.maThucDon}>
                <td>{(page - 1) * pageSize + index + 1}</td>
                <td>{item.tenMon}</td>
                <td className="text-right">{formatNumber(item.giaHienTai)}</td>
                <td className="action-buttons">
                  <button type="button" className="btn btn-sm btn-edit" onClick={() => onEdit(item)}>
                    Sửa
                  </button>
                  <button type="button" className="btn btn-sm btn-delete" onClick={() => onDelete(item.maThucDon)}>
                    Xóa
                  </button>
                </td>
              </tr>
            ))}
            {items.length === 0 ? (
              <tr>
                <td colSpan={4} className="text-center text-muted">
                  Không có dữ liệu
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      )}
      {items.length > 0 ? <Pagination page={page} pageSize={pageSize} total={items.length} onPageChange={setPage} /> : null}
    </div>
  )
}
