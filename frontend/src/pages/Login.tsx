import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import api from "../api"
import { useAuth } from "../auth"

export default function Login() {
  const { token, user, login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [fieldErrors, setFieldErrors] = useState<{ username?: string; password?: string }>({})
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    const msg = localStorage.getItem("auth_message")
    if (msg) {
      setError(msg)
      localStorage.removeItem("auth_message")
    }
    if (token && user) {
      if (user.roles.includes("ADMIN")) navigate("/admin/dashboard", { replace: true })
      else navigate("/staff/home", { replace: true })
    }
  }, [token, user, navigate])

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    const errors: { username?: string; password?: string } = {}
    if (!username.trim()) errors.username = "Username khong duoc de trong"
    if (!password.trim()) errors.password = "Password khong duoc de trong"
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return
    setError(null)
    setLoading(true)
    try {
      const resp = await api.auth.login({ username, password })
      login({ token: resp.token, username: resp.username, roles: resp.roles })
      if (resp.roles.includes("ADMIN")) navigate("/admin/dashboard", { replace: true })
      else navigate("/staff/home", { replace: true })
    } catch (err: any) {
      setError("Dang nhap that bai. Vui long thu lai.")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="auth">
      <div className="auth-card">
        <div className="auth-header">
          <h1>Login</h1>
          <p>QuanCaPhe Pro dashboard</p>
        </div>
        <form className="auth-form" onSubmit={submit}>
          <label className="auth-field">
            Username
            <input
              value={username}
              onChange={(event) => {
                setUsername(event.target.value)
                if (fieldErrors.username) setFieldErrors((prev) => ({ ...prev, username: "" }))
              }}
              autoComplete="username"
            />
            {fieldErrors.username ? <span className="field-error">{fieldErrors.username}</span> : null}
          </label>
          <label className="auth-field">
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => {
                setPassword(event.target.value)
                if (fieldErrors.password) setFieldErrors((prev) => ({ ...prev, password: "" }))
              }}
              autoComplete="current-password"
            />
            {fieldErrors.password ? <span className="field-error">{fieldErrors.password}</span> : null}
          </label>
          {error ? <p className="auth-error">{error}</p> : null}
          <button className="btn btn-primary auth-submit" type="submit" disabled={loading}>
            {loading ? "Signing in..." : "Sign in"}
          </button>
        </form>
      </div>
    </div>
  )
}
