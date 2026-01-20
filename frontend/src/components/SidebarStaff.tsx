import { NavLink } from "react-router-dom"
import { useAuth } from "../auth"

function linkClass(isActive: boolean) {
  return `sidebar-link${isActive ? " active" : ""}`
}

export default function SidebarStaff() {
  const { logout } = useAuth()

  return (
    <div className="sidebar-inner">
      <h4 className="sidebar-title">Trang chu</h4>
      <nav className="sidebar-nav">
        <ul className="sidebar-list">
          <li>
            <NavLink to="/staff/home" className={({ isActive }) => linkClass(isActive)}>
              Trang chu
            </NavLink>
          </li>
          <li>
            <NavLink to="/profile" className={({ isActive }) => linkClass(isActive)}>
              Trang ca nhan
            </NavLink>
          </li>
          <li>
            <NavLink to="/staff/sales" className={({ isActive }) => linkClass(isActive)}>
              Quan ly ban hang
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
