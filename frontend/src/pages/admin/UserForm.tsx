import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import api from "../../api"

type Props = {
  mode: "create" | "edit"
}

export default function AdminUserForm({ mode }: Props) {
  const navigate = useNavigate()
  const params = useParams()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({
    username: "",
    password: "",
    role: "NHANVIEN",
    avatar: "",
    enabled: true,
  })

  useEffect(() => {
    if (mode !== "edit") return
    const id = Number(params.id)
    if (!id) return
    setLoading(true)
    api.users
      .get(id)
      .then((u) => {
        setForm({
          username: u.username,
          password: "",
          role: u.role || "NHANVIEN",
          avatar: u.avatar || "",
          enabled: u.enabled,
        })
      })
      .catch((err: any) => setError(err?.body || err?.message || "Failed to load user"))
      .finally(() => setLoading(false))
  }, [mode, params.id])

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    try {
      if (mode === "create") {
        await api.users.create({
          username: form.username.trim(),
          password: form.password,
          role: form.role,
          avatar: form.avatar || undefined,
          enabled: form.enabled,
        })
      } else {
        const id = Number(params.id)
        await api.users.update(id, {
          password: form.password || undefined,
          role: form.role,
          avatar: form.avatar || undefined,
          enabled: form.enabled,
        })
      }
      navigate("/admin/users")
    } catch (err: any) {
      setError(err?.body || err?.message || "Save failed")
    }
  }

  return (
    <div className="content-wrapper">
      <h2>{mode === "create" ? "Create User" : "Edit User"}</h2>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}
      <form className="form-box" onSubmit={onSubmit}>
        <div className="form-group">
          <label>Username</label>
          <input
            type="text"
            value={form.username}
            onChange={(event) => setForm((prev) => ({ ...prev, username: event.target.value }))}
            disabled={mode === "edit"}
          />
        </div>
        <div className="form-group">
          <label>{mode === "create" ? "Password" : "New Password (optional)"}</label>
          <input type="password" value={form.password} onChange={(event) => setForm((prev) => ({ ...prev, password: event.target.value }))} />
        </div>
        <div className="form-group">
          <label>Role</label>
          <select value={form.role} onChange={(event) => setForm((prev) => ({ ...prev, role: event.target.value }))}>
            <option value="ADMIN">ADMIN</option>
            <option value="NHANVIEN">NHANVIEN</option>
          </select>
        </div>
        <div className="form-group">
          <label>Avatar URL</label>
          <input type="text" value={form.avatar} onChange={(event) => setForm((prev) => ({ ...prev, avatar: event.target.value }))} />
        </div>
        <div className="form-group">
          <label>
            <input
              type="checkbox"
              checked={form.enabled}
              onChange={(event) => setForm((prev) => ({ ...prev, enabled: event.target.checked }))}
            />
            Enabled
          </label>
        </div>
        <div className="form-actions">
          <button className="btn btn-primary" type="submit" disabled={loading}>
            {loading ? "Saving..." : "Save"}
          </button>
          <button className="btn btn-secondary" type="button" onClick={() => navigate("/admin/users")}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
