import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import api from "../../api"

export default function ProfileEdit() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [employeeId, setEmployeeId] = useState<number | null>(null)
  const [taiKhoanId, setTaiKhoanId] = useState<number | null>(null)
  const [chucVuId, setChucVuId] = useState<number | null>(null)
  const [enabled, setEnabled] = useState(true)
  const [me, setMe] = useState<{ id: number; role: string } | null>(null)
  const [form, setForm] = useState({
    hoTen: "",
    diaChi: "",
    soDienThoai: "",
  })
  const [passwordCurrent, setPasswordCurrent] = useState("")
  const [passwordNew, setPasswordNew] = useState("")

  useEffect(() => {
    let active = true
    const load = async () => {
      setLoading(true)
      try {
        const me = await api.users.me()
        if (!active) return
        setMe({ id: me.id, role: me.role })
        const list = await api.nhanvien.list()
        if (!active) return
        const match = list.find((nv) => nv.taiKhoanId === me.id)
        if (!match) {
          setError("Employee profile not found")
          return
        }
        setEmployeeId(match.maNhanVien)
        setTaiKhoanId(match.taiKhoanId || null)
        setEnabled(match.enabled)
        const chucVus = await api.chucvu.list()
        const found = chucVus.find((cv) => cv.tenChucVu === match.chucVu)
        setChucVuId(found ? found.maChucVu : null)
        setForm({
          hoTen: match.hoTen || "",
          diaChi: match.diaChi || "",
          soDienThoai: match.soDienThoai || "",
        })
      } catch (err: any) {
        setError(err?.body || err?.message || "Failed to load profile")
      } finally {
        if (active) setLoading(false)
      }
    }
    load()
    return () => {
      active = false
    }
  }, [])

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!employeeId) return
    setError(null)
    const wantsPasswordChange = passwordNew.trim().length > 0
    const isAdmin = me?.role === "ADMIN"
    if (wantsPasswordChange && !isAdmin && !passwordCurrent.trim()) {
      setError("Current password is required to change password")
      return
    }
    const payload: any = {
      hoTen: form.hoTen.trim(),
      diaChi: form.diaChi.trim(),
      soDienThoai: form.soDienThoai.trim(),
      enabled,
    }
    if (chucVuId) payload.chucVu = { maChucVu: chucVuId }
    if (taiKhoanId) payload.taiKhoan = { maTaiKhoan: taiKhoanId }
    try {
      await api.nhanvien.update(employeeId, payload)
      if (wantsPasswordChange) {
        if (isAdmin && me?.id) {
          await api.users.update(me.id, { password: passwordNew.trim() })
        } else {
          await api.users.changePassword({ oldPassword: passwordCurrent.trim(), newPassword: passwordNew.trim() })
        }
      }
      navigate("/profile")
    } catch (err: any) {
      setError(err?.body || err?.message || "Save failed")
    }
  }

  return (
    <div className="content-wrapper">
      <h2>Chinh sua thong tin ca nhan</h2>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}
      {loading ? <div className="page-loading">Loading...</div> : null}
      <form className="form-box" onSubmit={onSubmit} noValidate>
        <div className="form-group">
          <label>Ho va ten</label>
          <input value={form.hoTen} onChange={(event) => setForm((prev) => ({ ...prev, hoTen: event.target.value }))} />
        </div>
        <div className="form-group">
          <label>Dia chi</label>
          <input value={form.diaChi} onChange={(event) => setForm((prev) => ({ ...prev, diaChi: event.target.value }))} />
        </div>
        <div className="form-group">
          <label>So dien thoai</label>
          <input
            value={form.soDienThoai}
            onChange={(event) => setForm((prev) => ({ ...prev, soDienThoai: event.target.value.replace(/\\D/g, "") }))}
          />
        </div>
        <div className="form-group">
          <label>Mat khau hien tai</label>
          <input
            type="password"
            value={passwordCurrent}
            onChange={(event) => setPasswordCurrent(event.target.value)}
            placeholder="Nhap neu muon doi mat khau"
          />
        </div>
        <div className="form-group">
          <label>Mat khau moi (bo trong neu khong doi)</label>
          <input
            type="password"
            value={passwordNew}
            onChange={(event) => setPasswordNew(event.target.value)}
            placeholder="Nhap mat khau moi"
          />
        </div>
        <div className="form-actions form-actions--equal">
          <button className="btn btn-primary" type="submit" disabled={loading || !employeeId}>
            Save
          </button>
          <button className="btn btn-secondary" type="button" onClick={() => navigate("/profile")}>
            Cancel
          </button>
        </div>
      </form>
    </div>
  )
}
