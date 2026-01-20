import { useAuth } from "../auth"

export default function Header() {
  const { user } = useAuth()

  return (
    <div className="header-inner">
      <div className="header-row">
        <div className="header-brand">
          <span>QuanCaPhe Pro</span>
        </div>
        {user ? <div className="header-meta">Signed in: {user.username}</div> : null}
      </div>
    </div>
  )
}
