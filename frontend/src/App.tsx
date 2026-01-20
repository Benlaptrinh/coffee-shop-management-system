import { BrowserRouter, Navigate, Outlet, Route, Routes } from "react-router-dom"
import { AuthProvider, useAuth } from "./auth"
import AppLayout from "./components/AppLayout"
import RequireAuth from "./components/RequireAuth"
import RequireRole from "./components/RequireRole"
import SidebarAdmin from "./components/SidebarAdmin"
import SidebarStaff from "./components/SidebarStaff"
import Login from "./pages/Login"
import NotFound from "./pages/NotFound"
import AdminDashboard from "./pages/admin/Dashboard"
import AdminEmployeesList from "./pages/admin/EmployeesList"
import AdminEmployeesForm from "./pages/admin/EmployeesForm"
import AdminMenu from "./pages/admin/Menu"
import AdminMarketing from "./pages/admin/Marketing"
import AdminEquipment from "./pages/admin/Equipment"
import AdminWarehouse from "./pages/admin/Warehouse"
import AdminBudget from "./pages/admin/Budget"
import AdminReport from "./pages/admin/Report"
import AdminReports from "./pages/admin/Reports"
import AdminSales from "./pages/admin/Sales"
import AdminDataBackup from "./pages/admin/DataBackup"
import AdminDataRestore from "./pages/admin/DataRestore"
import AdminUsersList from "./pages/admin/UsersList"
import AdminUserForm from "./pages/admin/UserForm"
import StaffHome from "./pages/staff/Home"
import StaffSales from "./pages/staff/Sales"
import ProfileView from "./pages/profile/View"
import ProfileEdit from "./pages/profile/Edit"
import InvoicePrint from "./pages/sales/InvoicePrint"

function HomeRedirect() {
  const { token, user } = useAuth()
  if (!token) return <Navigate to="/login" replace />
  if (user?.roles.includes("ADMIN")) return <Navigate to="/admin/dashboard" replace />
  return <Navigate to="/staff/home" replace />
}

function AdminShell() {
  return (
    <RequireRole role="ADMIN">
      <AppLayout sidebar={<SidebarAdmin />}>
        <Outlet />
      </AppLayout>
    </RequireRole>
  )
}

function StaffShell() {
  return (
    <RequireRole role="NHANVIEN">
      <AppLayout sidebar={<SidebarStaff />}>
        <Outlet />
      </AppLayout>
    </RequireRole>
  )
}

export default function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <Routes>
          <Route path="/" element={<HomeRedirect />} />
          <Route path="/login" element={<Login />} />
          <Route element={<RequireAuth />}>
            <Route path="/invoice/:id" element={<InvoicePrint />} />
            <Route path="/profile" element={<ProfileView />} />
            <Route path="/profile/edit" element={<ProfileEdit />} />

            <Route path="/admin" element={<AdminShell />}>
              <Route index element={<Navigate to="dashboard" replace />} />
              <Route path="dashboard" element={<AdminDashboard />} />
              <Route path="employees" element={<AdminEmployeesList />} />
              <Route path="employees/create" element={<AdminEmployeesForm mode="create" />} />
              <Route path="employees/:id/edit" element={<AdminEmployeesForm mode="edit" />} />
              <Route path="sales" element={<AdminSales />} />
              <Route path="equipment" element={<AdminEquipment />} />
              <Route path="warehouse" element={<AdminWarehouse />} />
              <Route path="menu" element={<AdminMenu />} />
              <Route path="marketing" element={<AdminMarketing />} />
              <Route path="budget" element={<AdminBudget />} />
              <Route path="report" element={<AdminReport />} />
              <Route path="reports" element={<AdminReports />} />
              <Route path="data/backup" element={<AdminDataBackup />} />
              <Route path="data/restore" element={<AdminDataRestore />} />
              <Route path="users" element={<AdminUsersList />} />
              <Route path="users/create" element={<AdminUserForm mode="create" />} />
              <Route path="users/:id/edit" element={<AdminUserForm mode="edit" />} />
            </Route>

            <Route path="/staff" element={<StaffShell />}>
              <Route index element={<Navigate to="home" replace />} />
              <Route path="home" element={<StaffHome />} />
              <Route path="sales" element={<StaffSales />} />
            </Route>
          </Route>

          <Route path="*" element={<NotFound />} />
        </Routes>
      </BrowserRouter>
    </AuthProvider>
  )
}
