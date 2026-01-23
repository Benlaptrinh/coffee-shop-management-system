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
          setError("Không tìm thấy hồ sơ nhân viên")
          return
        }
        setEmployeeId(match.maNhanVien)
        setTaiKhoanId(match.taiKhoanId || null)
        setEnabled(match.enabled)
        setForm({
          hoTen: match.hoTen || "",
          diaChi: match.diaChi || "",
          soDienThoai: match.soDienThoai || "",
        })
        let resolvedChucVuId = match.chucVuId ?? null
        if (!resolvedChucVuId && match.chucVu && me.role === "ADMIN") {
          try {
            const chucVus = await api.chucvu.list()
            const found = chucVus.find((cv) => cv.tenChucVu === match.chucVu)
            resolvedChucVuId = found ? found.maChucVu : null
          } catch {
            resolvedChucVuId = null
          }
        }
        setChucVuId(resolvedChucVuId)
      } catch (err: any) {
        setError(err?.body || err?.message || "Tải hồ sơ thất bại")
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
    if (!form.hoTen.trim()) errors.hoTen = "Họ và tên không được để trống"
    if (!form.diaChi.trim()) errors.diaChi = "Địa chỉ không được để trống"
    if (!form.soDienThoai.trim()) {
      errors.soDienThoai = "Số điện thoại không được để trống"
    } else if (!/^\d{9,11}$/.test(form.soDienThoai.trim())) {
      errors.soDienThoai = "Số điện thoại phải từ 9 đến 11 số"
    }
    const wantsPasswordChange = passwordNew.trim().length > 0
    if (wantsPasswordChange && !passwordCurrent.trim()) {
      errors.passwordCurrent = "Cần nhập mật khẩu hiện tại"
    }
    if (wantsPasswordChange && passwordNew.trim().length < 6) {
      errors.passwordNew = "Mật khẩu mới tối thiểu 6 ký tự"
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
        // Always use change-password endpoint (require current password)
        await api.users.changePassword({ oldPassword: passwordCurrent.trim(), newPassword: passwordNew.trim() })
      }
      navigate("/profile")
    } catch (err: any) {
      // If change-password failed due to wrong current password, show field error under input (Vietnamese)
      const status = err?.status
      const body = typeof err?.body === "string" ? err.body : String(err?.body || "")
      if (wantsPasswordChange && status === 400 && body.toLowerCase().includes("old password")) {
        setFieldErrors((prev) => ({ ...prev, passwordCurrent: "Mật khẩu hiện tại không đúng" }))
        return
      }
      setError(err?.body || err?.message || "Lưu thất bại")
    }
  }

  return (
    <div className="content-wrapper">
      <h2>Chỉnh sửa thông tin cá nhân</h2>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}
      {loading ? <div className="page-loading">Đang tải...</div> : null}
      <form className="form-box" onSubmit={onSubmit} noValidate>
        <div className="form-group">
          <label>Họ và tên</label>
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
          <label>Địa chỉ</label>
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
          <label>Số điện thoại</label>
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
          <label>Mật khẩu hiện tại</label>
          <input
            type="password"
            value={passwordCurrent}
            onChange={(event) => {
              setPasswordCurrent(event.target.value)
              if (fieldErrors.passwordCurrent) setFieldErrors((prev) => ({ ...prev, passwordCurrent: "" }))
            }}
            placeholder="Nhập nếu muốn đổi mật khẩu"
          />
          {fieldErrors.passwordCurrent ? <div className="field-error">{fieldErrors.passwordCurrent}</div> : null}
        </div>
        <div className="form-group">
          <label>Mật khẩu mới (bỏ trống nếu không đổi)</label>
          <input
            type="password"
            value={passwordNew}
            onChange={(event) => {
              setPasswordNew(event.target.value)
              if (fieldErrors.passwordNew) setFieldErrors((prev) => ({ ...prev, passwordNew: "" }))
            }}
            placeholder="Nhập mật khẩu mới"
          />
          {fieldErrors.passwordNew ? <div className="field-error">{fieldErrors.passwordNew}</div> : null}
        </div>
        <div className="form-actions form-actions--equal">
          <button className="btn btn-primary" type="submit" disabled={loading || !employeeId}>
            Lưu
          </button>
          <button className="btn btn-secondary" type="button" onClick={() => navigate("/profile")}>
            Hủy
          </button>
        </div>
      </form>
    </div>
  )
}
