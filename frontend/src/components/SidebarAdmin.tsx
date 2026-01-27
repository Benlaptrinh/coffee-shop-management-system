import { NavLink } from "react-router-dom"
import { useAuth } from "../auth"

function linkClass(isActive: boolean) {
  return `sidebar-link${isActive ? " active" : ""}`
}

export default function SidebarAdmin() {
  const { logout } = useAuth()

  return (
    <div className="sidebar-inner flex flex-col gap-4">
      <h4 className="sidebar-title text-base font-semibold">Trang chủ</h4>
      <nav className="sidebar-nav">
        <ul className="sidebar-list">
          <li>
            <NavLink to="/admin/dashboard" className={({ isActive }) => linkClass(isActive)}>
              Trang chủ
            </NavLink>
          </li>
          <li>
            <NavLink to="/profile" className={({ isActive }) => linkClass(isActive)}>
              Trang cá nhân
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/employees" className={({ isActive }) => linkClass(isActive)}>
              Quản lý nhân viên
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/sales" className={({ isActive }) => linkClass(isActive)}>
              Quản lý bán hàng
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/equipment" className={({ isActive }) => linkClass(isActive)}>
              Quản lý trang thiết bị
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/warehouse" className={({ isActive }) => linkClass(isActive)}>
              Quản lý kho hàng
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/menu" className={({ isActive }) => linkClass(isActive)}>
              Quản lý thực đơn
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/marketing" className={({ isActive }) => linkClass(isActive)}>
              Quản lý marketing
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/budget" className={({ isActive }) => linkClass(isActive)}>
              Quản lý ngân sách
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/report" className={({ isActive }) => linkClass(isActive)}>
              Thống kê - báo cáo
            </NavLink>
          </li>
        </ul>
      </nav>
      <div className="sidebar-logout">
        <button className="sidebar-logout-btn w-full" type="button" onClick={logout}>
          Đăng xuất
        </button>
      </div>
    </div>
  )
}
