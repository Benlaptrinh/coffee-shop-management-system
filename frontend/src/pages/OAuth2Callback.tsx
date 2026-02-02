import { useEffect, useState } from "react"
import { useNavigate, useSearchParams } from "react-router-dom"
import { useAuth } from "../auth"

export default function OAuth2Callback() {
  const [searchParams] = useSearchParams()
  const { login } = useAuth()
  const navigate = useNavigate()
  const [processed, setProcessed] = useState(false)

  useEffect(() => {
    // Prevent multiple executions
    if (processed) return

    const token = searchParams.get("token")
    const username = searchParams.get("username")
    const roles = searchParams.get("roles")

    if (token && username && roles) {
      setProcessed(true)
      const roleList = roles.split(",")
      login({ token, username, roles: roleList })

      // Use setTimeout to navigate after state update
      setTimeout(() => {
        if (roleList.includes("ADMIN")) {
          navigate("/admin/dashboard", { replace: true })
        } else {
          navigate("/staff/home", { replace: true })
        }
      }, 100)
    }
  }, [searchParams, login, navigate, processed])

  return (
    <div className="auth">
      <div className="auth-card">
        <p>Đang xử lý đăng nhập...</p>
      </div>
    </div>
  )
}
