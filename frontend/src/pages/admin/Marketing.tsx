import { useEffect, useMemo, useState } from "react"
import api from "../../api"
import { formatDate } from "../../utils/format"

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

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    const payload = {
      tenKhuyenMai: form.tenKhuyenMai.trim(),
      ngayBatDau: form.ngayBatDau,
      ngayKetThuc: form.ngayKetThuc,
      giaTriGiam: Number(form.giaTriGiam || 0),
    }
    if (!payload.tenKhuyenMai || !payload.ngayBatDau || !payload.ngayKetThuc || !payload.giaTriGiam) {
      setError("Missing required fields")
      return
    }
    try {
      if (editId) await api.khuyenmai.update(editId, payload)
      else await api.khuyenmai.create(payload)
      setEditId(null)
      setForm({ tenKhuyenMai: "", ngayBatDau: "", ngayKetThuc: "", giaTriGiam: "" })
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
  }

  const onReset = () => {
    setEditId(null)
    setForm({ tenKhuyenMai: "", ngayBatDau: "", ngayKetThuc: "", giaTriGiam: "" })
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
            <input value={form.tenKhuyenMai} onChange={(event) => setForm((prev) => ({ ...prev, tenKhuyenMai: event.target.value }))} />
          </div>
          <div className="form-group">
            <label>Ngay bat dau</label>
            <input type="date" value={form.ngayBatDau} onChange={(event) => setForm((prev) => ({ ...prev, ngayBatDau: event.target.value }))} />
          </div>
          <div className="form-group">
            <label>Ngay ket thuc</label>
            <input type="date" value={form.ngayKetThuc} onChange={(event) => setForm((prev) => ({ ...prev, ngayKetThuc: event.target.value }))} />
          </div>
          <div className="form-group">
            <label>% giam gia</label>
            <input
              type="number"
              min={1}
              max={100}
              value={form.giaTriGiam}
              onChange={(event) => setForm((prev) => ({ ...prev, giaTriGiam: event.target.value }))}
            />
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
            {filteredItems.map((km) => (
              <tr key={km.maKhuyenMai}>
                <td>{km.tenKhuyenMai}</td>
                <td>{formatDate(km.ngayBatDau)}</td>
                <td>{formatDate(km.ngayKetThuc)}</td>
                <td className="text-right">{km.giaTriGiam}%</td>
                <td className="action-buttons">
                  <button type="button" className="btn btn-sm btn-edit" onClick={() => onEdit(km)}>
                    Sua
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
    </div>
  )
}
