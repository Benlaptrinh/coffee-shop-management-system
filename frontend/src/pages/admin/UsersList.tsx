import { useEffect, useState } from "react"
import { Link } from "react-router-dom"
import api from "../../api"

type User = {
  id: number
  username: string
  role: string
  enabled: boolean
}

export default function AdminUsersList() {
  const [items, setItems] = useState<User[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.users.list()
      setItems(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Failed to load users")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    load()
  }, [])

  const toggle = async (user: User) => {
    try {
      await api.users.disable(user.id, !user.enabled)
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Update failed")
    }
  }

  return (
    <div className="content-wrapper">
      <h2>User List</h2>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}
      <Link className="btn btn-primary" to="/admin/users/create">
        Create user
      </Link>

      {loading ? (
        <div className="page-loading">Loading...</div>
      ) : (
        <table className="data-table table-actions">
          <thead>
            <tr>
              <th>Username</th>
              <th>Role</th>
              <th>Enabled</th>
              <th>Actions</th>
            </tr>
          </thead>
          <tbody>
            {items.map((u) => (
              <tr key={u.id}>
                <td>{u.username}</td>
                <td>{u.role}</td>
                <td>{u.enabled ? "true" : "false"}</td>
                <td className="action-buttons">
                  <Link className="btn btn-secondary btn-sm" to={`/admin/users/${u.id}/edit`}>
                    Edit
                  </Link>
                  <button className="btn btn-secondary btn-sm" type="button" onClick={() => toggle(u)}>
                    {u.enabled ? "Disable" : "Enable"}
                  </button>
                </td>
              </tr>
            ))}
            {items.length === 0 ? (
              <tr>
                <td colSpan={4} className="text-center text-muted">
                  No data
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      )}
    </div>
  )
}
