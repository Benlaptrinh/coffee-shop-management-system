import { useEffect, useState } from "react"
import api from "../../api"
import { formatNumber, toDigits } from "../../utils/format"

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

  const load = async (q?: string) => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.thucdon.list(q)
      setItems(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Failed to load menu")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(search)
  }, [search])

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    const price = Number(form.giaRaw || 0)
    if (!form.tenMon.trim() || !price) {
      setError("Missing name or price")
      return
    }
    try {
      if (editId) {
        await api.thucdon.update(editId, { tenMon: form.tenMon.trim(), giaHienTai: price })
      } else {
        await api.thucdon.create({ tenMon: form.tenMon.trim(), giaHienTai: price })
      }
      setForm({ tenMon: "", giaRaw: "" })
      setEditId(null)
      await load(search)
    } catch (err: any) {
      setError(err?.body || err?.message || "Save failed")
    }
  }

  const onEdit = (item: MenuItem) => {
    setEditId(item.maThucDon)
    setForm({ tenMon: item.tenMon, giaRaw: toDigits(String(item.giaHienTai)) })
  }

  const onDelete = async (id: number) => {
    if (!window.confirm("Delete this item?")) return
    try {
      await api.thucdon.delete(id)
      await load(search)
    } catch (err: any) {
      setError(err?.body || err?.message || "Delete failed")
    }
  }

  const onResetForm = () => {
    setEditId(null)
    setForm({ tenMon: "", giaRaw: "" })
  }

  return (
    <div className="content-wrapper">
      <h1>Danh sach thuc don</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="form-box">
        <h2>{editId ? "Chinh sua mon" : "Them mon"}</h2>
        <form onSubmit={onSubmit} noValidate>
          <div className="form-group">
            <label>Ten mon</label>
            <input value={form.tenMon} onChange={(event) => setForm((prev) => ({ ...prev, tenMon: event.target.value }))} />
          </div>
          <div className="form-group">
            <label>Gia tien</label>
            <input
              value={formatNumber(form.giaRaw)}
              inputMode="numeric"
              onChange={(event) => setForm((prev) => ({ ...prev, giaRaw: toDigits(event.target.value) }))}
            />
          </div>
          <div className="form-actions">
            <button type="submit" className="btn btn-primary">
              {editId ? "Save" : "Add"}
            </button>
            {editId ? (
              <button type="button" className="btn btn-cancel" onClick={onResetForm}>
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
            placeholder="Search menu..."
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button className="btn btn-sm" type="submit">
            Search
          </button>
          <button className="btn btn-sm btn-cancel" type="button" onClick={() => (setKeyword(""), setSearch(""))}>
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
              <th>STT</th>
              <th>Ten mon</th>
              <th className="text-right">Gia tien</th>
              <th>Hanh dong</th>
            </tr>
          </thead>
          <tbody>
            {items.map((item, index) => (
              <tr key={item.maThucDon}>
                <td>{index + 1}</td>
                <td>{item.tenMon}</td>
                <td className="text-right">{formatNumber(item.giaHienTai)}</td>
                <td className="action-buttons">
                  <button type="button" className="btn btn-sm btn-edit" onClick={() => onEdit(item)}>
                    Sua
                  </button>
                  <button type="button" className="btn btn-sm btn-delete" onClick={() => onDelete(item.maThucDon)}>
                    Xoa
                  </button>
                </td>
              </tr>
            ))}
            {items.length === 0 ? (
              <tr>
                <td colSpan={4} className="text-center text-muted">
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
