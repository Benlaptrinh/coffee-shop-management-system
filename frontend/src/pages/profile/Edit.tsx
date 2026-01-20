import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import api from "../../api"

export default function ProfileEdit() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
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
    const errors: Record<string, string> = {}
    if (!form.hoTen.trim()) errors.hoTen = "Ho va ten khong duoc de trong"
    if (!form.diaChi.trim()) errors.diaChi = "Dia chi khong duoc de trong"
    if (!form.soDienThoai.trim()) {
      errors.soDienThoai = "So dien thoai khong duoc de trong"
    } else if (!/^\d{9,11}$/.test(form.soDienThoai.trim())) {
      errors.soDienThoai = "So dien thoai phai tu 9 den 11 so"
    }
    const wantsPasswordChange = passwordNew.trim().length > 0
    const isAdmin = me?.role === "ADMIN"
    if (wantsPasswordChange && !isAdmin && !passwordCurrent.trim()) {
      errors.passwordCurrent = "Can nhap mat khau hien tai"
    }
    if (wantsPasswordChange && passwordNew.trim().length < 6) {
      errors.passwordNew = "Mat khau moi toi thieu 6 ky tu"
    }
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return
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
          <input
            value={form.hoTen}
            onChange={(event) => {
              setForm((prev) => ({ ...prev, hoTen: event.target.value }))
              if (fieldErrors.hoTen) setFieldErrors((prev) => ({ ...prev, hoTen: "" }))
            }}
          />
          {fieldErrors.hoTen ? <div className="field-error">{fieldErrors.hoTen}</div> : null}
        </div>
        <div className="form-group">
          <label>Dia chi</label>
          <input
            value={form.diaChi}
            onChange={(event) => {
              setForm((prev) => ({ ...prev, diaChi: event.target.value }))
              if (fieldErrors.diaChi) setFieldErrors((prev) => ({ ...prev, diaChi: "" }))
            }}
          />
          {fieldErrors.diaChi ? <div className="field-error">{fieldErrors.diaChi}</div> : null}
        </div>
        <div className="form-group">
          <label>So dien thoai</label>
          <input
            value={form.soDienThoai}
            onChange={(event) => {
              setForm((prev) => ({ ...prev, soDienThoai: event.target.value.replace(/\\D/g, "") }))
              if (fieldErrors.soDienThoai) setFieldErrors((prev) => ({ ...prev, soDienThoai: "" }))
            }}
          />
          {fieldErrors.soDienThoai ? <div className="field-error">{fieldErrors.soDienThoai}</div> : null}
        </div>
        <div className="form-group">
          <label>Mat khau hien tai</label>
          <input
            type="password"
            value={passwordCurrent}
            onChange={(event) => {
              setPasswordCurrent(event.target.value)
              if (fieldErrors.passwordCurrent) setFieldErrors((prev) => ({ ...prev, passwordCurrent: "" }))
            }}
            placeholder="Nhap neu muon doi mat khau"
          />
          {fieldErrors.passwordCurrent ? <div className="field-error">{fieldErrors.passwordCurrent}</div> : null}
        </div>
        <div className="form-group">
          <label>Mat khau moi (bo trong neu khong doi)</label>
          <input
            type="password"
            value={passwordNew}
            onChange={(event) => {
              setPasswordNew(event.target.value)
              if (fieldErrors.passwordNew) setFieldErrors((prev) => ({ ...prev, passwordNew: "" }))
            }}
            placeholder="Nhap mat khau moi"
          />
          {fieldErrors.passwordNew ? <div className="field-error">{fieldErrors.passwordNew}</div> : null}
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
