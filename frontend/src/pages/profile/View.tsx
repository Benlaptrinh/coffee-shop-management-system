import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import api from "../../api"
import { useAuth } from "../../auth"

type User = { id: number; username: string; role: string; avatar?: string | null }
type Employee = { maNhanVien: number; hoTen: string; diaChi?: string; soDienThoai?: string; chucVu?: string; taiKhoanId?: number }

export default function ProfileView() {
  const { logout } = useAuth()
  const [user, setUser] = useState<User | null>(null)
  const [employee, setEmployee] = useState<Employee | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    let active = true
    const load = async () => {
      setLoading(true)
      try {
        const me = await api.users.me()
        if (!active) return
        setUser(me)
        const list = await api.nhanvien.list()
        if (!active) return
        const match = list.find((nv) => nv.taiKhoanId === me.id)
        setEmployee(match || null)
      } catch (err) {
        setUser(null)
        setEmployee(null)
      } finally {
        if (active) setLoading(false)
      }
    }
    load()
    return () => {
      active = false
    }
  }, [])

  const avatarSrc = user?.avatar || "/img/default-avatar.png"
  const roleLabel = employee?.chucVu
    || (user?.role === "ADMIN" ? "Quản trị" : user?.role === "NHANVIEN" ? "Nhân viên" : user?.role || "")

  return (
    <div className="content-wrapper">
      <h1>Trang cá nhân</h1>
      {loading ? <div className="page-loading">Đang tải...</div> : null}

      <div className="profile-card">
        <div className="profile-header">
          <div className="avatar">
            <img src={avatarSrc} alt="avatar" />
          </div>
          <div className="profile-main">
            <h2>{employee?.hoTen || user?.username || "-"}</h2>
            <p className="muted">{roleLabel}</p>
          </div>
        </div>

        <div className="profile-section">
          <h3>Thông tin cá nhân</h3>
          <div className="info-row">
            <span>Địa chỉ</span>
            <span>{employee?.diaChi || "-"}</span>
          </div>
          <div className="info-row">
            <span>Số điện thoại</span>
            <span>{employee?.soDienThoai || "-"}</span>
          </div>
        </div>

        <div className="profile-section">
          <h3>Tài khoản</h3>
          <div className="info-row">
            <span>Username</span>
            <span>{user?.username || "-"}</span>
          </div>
        </div>

        <div className="action-bar">
          <Link className="btn btn-edit" to="/profile/edit">
            Chỉnh sửa
          </Link>
          <button className="btn btn-delete" type="button" onClick={logout}>
            Đăng xuất
          </button>
        </div>
      </div>
    </div>
  )
}
