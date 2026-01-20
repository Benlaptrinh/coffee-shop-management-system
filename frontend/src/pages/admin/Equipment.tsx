import { useEffect, useState } from "react"
import api from "../../api"
import { formatDate, formatNumber, toDigits } from "../../utils/format"

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

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.thietbi.list()
      setItems(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Failed to load equipment")
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
    if (!payload.tenThietBi || !payload.ngayMua || !payload.soLuong || !payload.donGiaMua) {
      setError("Missing required fields")
      return
    }
    try {
      if (editId) await api.thietbi.update(editId, payload)
      else await api.thietbi.create(payload)
      setEditId(null)
      setForm({ tenThietBi: "", ngayMua: "", soLuong: "1", donGiaRaw: "", ghiChu: "" })
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Save failed")
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
  }

  const onDelete = async (id: number) => {
    if (!window.confirm("Delete this item?")) return
    try {
      await api.thietbi.delete(id)
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Delete failed")
    }
  }

  const filtered = items.filter((item) => (search ? item.tenThietBi.toLowerCase().includes(search.toLowerCase()) : true))

  return (
    <div className="content-wrapper">
      <h1>Quan ly thiet bi</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="form-box">
        <h2>{editId ? "Chinh sua thiet bi" : "Them thiet bi"}</h2>
        <form onSubmit={onSubmit} noValidate>
          <div className="form-group">
            <label>Ten thiet bi</label>
            <input value={form.tenThietBi} onChange={(event) => setForm((prev) => ({ ...prev, tenThietBi: event.target.value }))} />
          </div>
          <div className="form-group">
            <label>Ngay mua</label>
            <input type="date" value={form.ngayMua} onChange={(event) => setForm((prev) => ({ ...prev, ngayMua: event.target.value }))} />
          </div>
          <div className="form-row">
            <div className="form-group">
              <label>So luong</label>
              <input
                type="number"
                min={1}
                max={10}
                value={form.soLuong}
                onChange={(event) => setForm((prev) => ({ ...prev, soLuong: event.target.value }))}
              />
            </div>
            <div className="form-group">
              <label>Don gia mua</label>
              <input
                inputMode="numeric"
                value={formatNumber(form.donGiaRaw)}
                onChange={(event) => setForm((prev) => ({ ...prev, donGiaRaw: toDigits(event.target.value) }))}
              />
            </div>
          </div>
          <div className="form-group">
            <label>Ghi chu</label>
            <textarea rows={3} value={form.ghiChu} onChange={(event) => setForm((prev) => ({ ...prev, ghiChu: event.target.value }))} />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {editId ? "Save" : "Add"}
            </button>
            {editId ? (
              <button type="button" className="btn btn-cancel" onClick={() => setEditId(null)}>
                Cancel
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
            placeholder="Search equipment..."
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button className="btn btn-sm" type="submit">
            Search
          </button>
          <button
            className="btn btn-sm btn-cancel"
            type="button"
            onClick={() => {
              setKeyword("")
              setSearch("")
            }}
          >
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
              <th>Ten</th>
              <th>Ngay mua</th>
              <th>So luong</th>
              <th className="text-right">Don gia</th>
              <th className="text-right">Tong</th>
              <th>Hanh dong</th>
            </tr>
          </thead>
          <tbody>
            {filtered.map((item) => (
              <tr key={item.maThietBi}>
                <td>{item.tenThietBi}</td>
                <td>{formatDate(item.ngayMua)}</td>
                <td>{item.soLuong}</td>
                <td className="text-right">{formatNumber(item.donGiaMua)}</td>
                <td className="text-right">{formatNumber(item.donGiaMua * item.soLuong)}</td>
                <td className="action-buttons">
                  <button type="button" className="btn btn-sm btn-edit" onClick={() => onEdit(item)}>
                    Sua
                  </button>
                  <button type="button" className="btn btn-sm btn-delete" onClick={() => onDelete(item.maThietBi)}>
                    Xoa
                  </button>
                </td>
              </tr>
            ))}
            {filtered.length === 0 ? (
              <tr>
                <td colSpan={6} className="text-center text-muted">
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
