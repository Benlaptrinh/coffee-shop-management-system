import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import api from "../../api"

type Employee = {
  maNhanVien: number
  hoTen: string
  soDienThoai?: string
  diaChi?: string
  chucVu?: string
  taiKhoanId?: number
  enabled: boolean
}

export default function AdminEmployeesList() {
  const [items, setItems] = useState<Employee[]>([])
  const [keyword, setKeyword] = useState("")
  const [search, setSearch] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = async (q?: string) => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.nhanvien.list(q)
      setItems(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Failed to load employees")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load(search)
  }, [search])

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    setSearch(keyword.trim())
  }

  const onReset = () => {
    setKeyword("")
    setSearch("")
  }

  const onDelete = async (id: number) => {
    if (!window.confirm("Delete this employee?")) return
    try {
      await api.nhanvien.delete(id)
      await load(search)
    } catch (err: any) {
      setError(err?.body || err?.message || "Delete failed")
    }
  }

  return (
    <div className="content-wrapper">
      <h1>Danh sach nhan vien</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="action-bar">
        <Link className="btn btn-primary" to="/admin/employees/create">
          + Them nhan vien
        </Link>
        <form className="search-form" onSubmit={onSubmit}>
          <input
            type="text"
            name="q"
            placeholder="Search by name"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button type="submit" className="btn btn-sm">
            Search
          </button>
          <button type="button" className="btn btn-sm btn-cancel" onClick={onReset}>
            Reset
          </button>
        </form>
      </div>

      {loading ? (
        <div className="page-loading">Loading...</div>
      ) : (
        <table className="data-table table-wide table-actions">
          <thead>
            <tr>
              <th>Ho ten</th>
              <th>So dien thoai</th>
              <th>Chuc vu</th>
              <th>Trang thai</th>
              <th>Hanh dong</th>
            </tr>
          </thead>
          <tbody>
            {items.map((nv) => (
              <tr key={nv.maNhanVien}>
                <td>{nv.hoTen}</td>
                <td>{nv.soDienThoai || "-"}</td>
                <td>{nv.chucVu || "-"}</td>
                <td>{nv.enabled ? "Hoat dong" : "Khong hoat dong"}</td>
                <td className="action-buttons">
                  <Link className="btn btn-sm btn-edit" to={`/admin/employees/${nv.maNhanVien}/edit`}>
                    Sua
                  </Link>
                  <button className="btn btn-sm btn-delete" type="button" onClick={() => onDelete(nv.maNhanVien)}>
                    Xoa
                  </button>
                </td>
              </tr>
            ))}
            {items.length === 0 ? (
              <tr>
                <td className="text-center text-muted" colSpan={5}>
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
