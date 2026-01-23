import { NavLink } from "react-router-dom"
import { useAuth } from "../auth"

function linkClass(isActive: boolean) {
  return `sidebar-link${isActive ? " active" : ""}`
}

export default function SidebarStaff() {
  const { logout } = useAuth()

  return (
    <div className="sidebar-inner">
      <h4 className="sidebar-title">Trang chủ</h4>
      <nav className="sidebar-nav">
        <ul className="sidebar-list">
          <li>
            <NavLink to="/staff/home" className={({ isActive }) => linkClass(isActive)}>
              Trang chủ
            </NavLink>
          </li>
          <li>
            <NavLink to="/profile" className={({ isActive }) => linkClass(isActive)}>
              Trang cá nhân
            </NavLink>
          </li>
          <li>
            <NavLink to="/staff/sales" className={({ isActive }) => linkClass(isActive)}>
              Quản lý bán hàng
            </NavLink>
          </li>
        </ul>
      </nav>
      <div className="sidebar-logout">
        <button className="sidebar-logout-btn" type="button" onClick={logout}>
          Đăng xuất
        </button>
      </div>
    </div>
  )
}
