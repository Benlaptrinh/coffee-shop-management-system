import { useAuth } from "../auth"

export default function Header() {
  const { user } = useAuth()

  return (
    <div className="header-inner">
      <div className="header-row flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <div className="header-brand flex items-center gap-2 text-lg font-semibold text-white">
          <span className="font-serif tracking-wide">QuanCaPhe Pro</span>
        </div>
        {user ? <div className="header-meta text-sm text-white/80">Đã đăng nhập: {user.username}</div> : null}
      </div>
    </div>
  )
}
