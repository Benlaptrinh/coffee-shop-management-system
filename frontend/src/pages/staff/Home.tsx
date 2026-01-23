import { useAuth } from "../../auth"

export default function StaffHome() {
  const { user } = useAuth()
  return (
    <div className="content-wrapper">
      <h1>Xin chào{user ? `, ${user.username}` : ""}!</h1>
    </div>
  )
}
