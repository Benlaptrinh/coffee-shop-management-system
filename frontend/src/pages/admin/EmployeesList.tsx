import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import api from "../../api"
import Pagination from "../../components/Pagination"

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
  const [page, setPage] = useState(1)
  const pageSize = 10

  const load = async (q?: string) => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.nhanvien.list(q ? { q } : undefined)
      setItems(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Tải danh sách nhân viên thất bại")
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

  const onSubmit = (event: React.FormEvent) => {
    event.preventDefault()
    setSearch(keyword.trim())
  }

  const onReset = () => {
    setKeyword("")
    setSearch("")
  }

  const onDelete = async (id: number) => {
    if (!window.confirm("Xóa nhân viên này?")) return
    try {
      await api.nhanvien.delete(id)
      await load(search)
    } catch (err: any) {
      setError(err?.body || err?.message || "Xóa thất bại")
    }
  }

  return (
    <div className="content-wrapper">
      <h1>Danh sách nhân viên</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="action-bar">
        <Link className="btn btn-primary" to="/admin/employees/create">
          + Thêm nhân viên
        </Link>
        <form className="search-form" onSubmit={onSubmit}>
          <input
            type="text"
            name="q"
            placeholder="Tìm theo tên"
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button type="submit" className="btn btn-sm">
            Tìm kiếm
          </button>
          <button type="button" className="btn btn-sm btn-cancel" onClick={onReset}>
            Đặt lại
          </button>
        </form>
      </div>

      {loading ? (
        <div className="page-loading">Đang tải...</div>
      ) : (
        <table className="data-table table-wide table-actions">
          <thead>
            <tr>
              <th>Họ tên</th>
              <th>Số điện thoại</th>
              <th>Chức vụ</th>
              <th>Trạng thái</th>
              <th>Hành động</th>
            </tr>
          </thead>
          <tbody>
            {items.slice((page - 1) * pageSize, page * pageSize).map((nv) => (
              <tr key={nv.maNhanVien}>
                <td>{nv.hoTen}</td>
                <td>{nv.soDienThoai || "-"}</td>
                <td>{nv.chucVu || "-"}</td>
                <td>{nv.enabled ? "Hoạt động" : "Không hoạt động"}</td>
                <td className="action-buttons">
                  <Link className="btn btn-sm btn-edit" to={`/admin/employees/${nv.maNhanVien}/edit`}>
                    Sửa
                  </Link>
                  <button className="btn btn-sm btn-delete" type="button" onClick={() => onDelete(nv.maNhanVien)}>
                    Xóa
                  </button>
                </td>
              </tr>
            ))}
            {items.length === 0 ? (
              <tr>
                <td className="text-center text-muted" colSpan={5}>
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
