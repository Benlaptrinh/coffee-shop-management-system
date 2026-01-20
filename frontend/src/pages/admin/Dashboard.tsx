import { useAuth } from "../../auth"

export default function AdminDashboard() {
  const { user } = useAuth()

  return (
    <div className="content-wrapper">
      <h1>Xin chao{user ? `, ${user.username}` : ""}!</h1>
      <p>Admin dashboard ready.</p>
    </div>
  )
}
