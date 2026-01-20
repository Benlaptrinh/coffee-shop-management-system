import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import api from "../api"
import { useAuth } from "../auth"

export default function Login() {
  const { token, user, login } = useAuth()
  const navigate = useNavigate()
  const [username, setUsername] = useState("")
  const [password, setPassword] = useState("")
  const [error, setError] = useState<string | null>(null)
  const [loading, setLoading] = useState(false)

  useEffect(() => {
    if (token && user) {
      if (user.roles.includes("ADMIN")) navigate("/admin/dashboard", { replace: true })
      else navigate("/staff/home", { replace: true })
    }
  }, [token, user, navigate])

  const submit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    setLoading(true)
    try {
      const resp = await api.auth.login({ username, password })
      login({ token: resp.token, username: resp.username, roles: resp.roles })
      if (resp.roles.includes("ADMIN")) navigate("/admin/dashboard", { replace: true })
      else navigate("/staff/home", { replace: true })
    } catch (err: any) {
      setError(err?.body || err?.message || "Login failed")
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
            <input value={username} onChange={(event) => setUsername(event.target.value)} autoComplete="username" />
          </label>
          <label className="auth-field">
            Password
            <input
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
            />
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
