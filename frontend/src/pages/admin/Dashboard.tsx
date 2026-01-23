import { useAuth } from "../../auth"

export default function AdminDashboard() {
  const { user } = useAuth()

  return (
    <div className="content-wrapper">
      <h1>Xin chào{user ? `, ${user.username}` : ""}!</h1>
      <p>Bảng điều khiển admin sẵn sàng.</p>
    </div>
  )
}
