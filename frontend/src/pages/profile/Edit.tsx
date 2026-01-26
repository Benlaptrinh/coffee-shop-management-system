import { useEffect, useState } from "react"
import { useNavigate } from "react-router-dom"
import api from "../../api"
import { useForm } from "react-hook-form"
import { zodResolver } from "@hookform/resolvers/zod"
import * as z from "zod"

export default function ProfileEdit() {
  const navigate = useNavigate()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [employeeId, setEmployeeId] = useState<number | null>(null)
  const [taiKhoanId, setTaiKhoanId] = useState<number | null>(null)
  const [chucVuId, setChucVuId] = useState<number | null>(null)
  const [enabled, setEnabled] = useState(true)
  const [, setMe] = useState<{ id: number; role: string } | null>(null)
  const [passwordServerError, setPasswordServerError] = useState<string | null>(null)

  const schema = z.object({
    hoTen: z.string().min(1, "Họ và tên không được để trống"),
    diaChi: z.string().min(1, "Địa chỉ không được để trống"),
    soDienThoai: z.string().regex(/^\d{9,11}$/, "Số điện thoại phải từ 9 đến 11 số"),
    passwordCurrent: z.string().optional(),
    passwordNew: z.string().optional(),
  })
  type FormSchema = z.infer<typeof schema>

  const { register, handleSubmit, reset, formState: { errors } } = useForm<FormSchema>({
    resolver: zodResolver(schema),
    defaultValues: {
      hoTen: "",
      diaChi: "",
      soDienThoai: "",
      passwordCurrent: "",
      passwordNew: "",
    },
  })

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
        reset({
          hoTen: match.hoTen || "",
          diaChi: match.diaChi || "",
          soDienThoai: match.soDienThoai || "",
          passwordCurrent: "",
          passwordNew: "",
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

  const onSubmit = handleSubmit(async (data) => {
    if (!employeeId) return
    setError(null)
    setFieldErrors({})
    setPasswordServerError(null)

    const wantsPasswordChange = (data.passwordNew || "").trim().length > 0
    if (wantsPasswordChange && (data.passwordCurrent || "").trim().length === 0) {
      setFieldErrors({ passwordCurrent: "Cần nhập mật khẩu hiện tại" })
      return
    }
    if (wantsPasswordChange && (data.passwordNew || "").trim().length > 0 && (data.passwordNew || "").trim().length < 6) {
      setFieldErrors({ passwordNew: "Mật khẩu mới tối thiểu 6 ký tự" })
      return
    }

    const payload: any = {
      hoTen: data.hoTen.trim(),
      diaChi: data.diaChi.trim(),
      soDienThoai: data.soDienThoai.trim(),
      enabled,
    }
    if (chucVuId) payload.chucVuId = chucVuId
    if (taiKhoanId) payload.taiKhoanId = taiKhoanId
    try {
      await api.nhanvien.update(employeeId, payload)
      if (wantsPasswordChange) {
        await api.users.changePassword({ oldPassword: (data.passwordCurrent || "").trim(), newPassword: (data.passwordNew || "").trim() })
      }
      navigate("/profile")
    } catch (err: any) {
      const status = err?.status
      const body = typeof err?.body === "string" ? err.body : String(err?.body || "")
      if (wantsPasswordChange && status === 400 && body.toLowerCase().includes("old password")) {
        setFieldErrors((prev) => ({ ...prev, passwordCurrent: "Mật khẩu hiện tại không đúng" }))
        return
      }
      setError(err?.body || err?.message || "Lưu thất bại")
    }
  })

  return (
    <div className="content-wrapper">
      <h2>Chỉnh sửa thông tin cá nhân</h2>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}
      {loading ? <div className="page-loading">Đang tải...</div> : null}
      <form className="form-box" onSubmit={onSubmit} noValidate>
        <div className="form-group">
          <label>Họ và tên</label>
          <input {...register("hoTen")} />
          {errors.hoTen ? <div className="field-error">{String(errors.hoTen.message)}</div> : null}
        </div>
        <div className="form-group">
          <label>Địa chỉ</label>
          <input {...register("diaChi")} />
          {errors.diaChi ? <div className="field-error">{String(errors.diaChi.message)}</div> : null}
        </div>
        <div className="form-group">
          <label>Số điện thoại</label>
          <input {...register("soDienThoai")} />
          {errors.soDienThoai ? <div className="field-error">{String(errors.soDienThoai.message)}</div> : null}
        </div>
        <div className="form-group">
          <label>Mật khẩu hiện tại</label>
          <input type="password" {...register("passwordCurrent")} placeholder="Nhập nếu muốn đổi mật khẩu" />
          {fieldErrors.passwordCurrent ? <div className="field-error">{fieldErrors.passwordCurrent}</div> : null}
          {passwordServerError ? <div className="field-error">{passwordServerError}</div> : null}
        </div>
        <div className="form-group">
          <label>Mật khẩu mới (bỏ trống nếu không đổi)</label>
          <input type="password" {...register("passwordNew")} placeholder="Nhập mật khẩu mới" />
          {errors.passwordNew ? <div className="field-error">{String(errors.passwordNew.message)}</div> : null}
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
