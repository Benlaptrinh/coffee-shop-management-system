import { NavLink } from "react-router-dom"
import { useAuth } from "../auth"

function linkClass(isActive: boolean) {
  return `sidebar-link${isActive ? " active" : ""}`
}

export default function SidebarAdmin() {
  const { logout } = useAuth()

  return (
    <div className="sidebar-inner">
      <h4 className="sidebar-title">Trang chu</h4>
      <nav className="sidebar-nav">
        <ul className="sidebar-list">
          <li>
            <NavLink to="/admin/dashboard" className={({ isActive }) => linkClass(isActive)}>
              Trang chu
            </NavLink>
          </li>
          <li>
            <NavLink to="/profile" className={({ isActive }) => linkClass(isActive)}>
              Trang ca nhan
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/employees" className={({ isActive }) => linkClass(isActive)}>
              Quan ly nhan vien
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/sales" className={({ isActive }) => linkClass(isActive)}>
              Quan ly ban hang
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/equipment" className={({ isActive }) => linkClass(isActive)}>
              Quan ly trang thiet bi
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/warehouse" className={({ isActive }) => linkClass(isActive)}>
              Quan ly kho hang
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/menu" className={({ isActive }) => linkClass(isActive)}>
              Quan ly thuc don
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/marketing" className={({ isActive }) => linkClass(isActive)}>
              Quan ly marketing
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/budget" className={({ isActive }) => linkClass(isActive)}>
              Quan ly ngan sach
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/report" className={({ isActive }) => linkClass(isActive)}>
              Thong ke - bao cao
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/data/backup" className={({ isActive }) => linkClass(isActive)}>
              Data backup
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/data/restore" className={({ isActive }) => linkClass(isActive)}>
              Data restore
            </NavLink>
          </li>
          <li>
            <NavLink to="/admin/users" className={({ isActive }) => linkClass(isActive)}>
              User management
            </NavLink>
          </li>
        </ul>
      </nav>
      <div className="sidebar-logout">
        <button className="sidebar-logout-btn" type="button" onClick={logout}>
          Logout
        </button>
      </div>
    </div>
  )
}
