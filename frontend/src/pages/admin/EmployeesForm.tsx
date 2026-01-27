import { useEffect, useState, type FormEvent } from "react"
import { useNavigate, useParams } from "react-router-dom"
import api from "../../api"

type Props = {
  mode: "create" | "edit"
}

export default function AdminEmployeesForm({ mode }: Props) {
  const navigate = useNavigate()
  const params = useParams()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [chucVus, setChucVus] = useState<Array<{ maChucVu: number; tenChucVu: string }>>([])
  const [pendingChucVuName, setPendingChucVuName] = useState<string | null>(null)
  const [taiKhoanId, setTaiKhoanId] = useState<number | null>(null)
  const [hasAccount, setHasAccount] = useState(false)
  const [form, setForm] = useState({
    hoTen: "",
    diaChi: "",
    soDienThoai: "",
    enabled: true,
    chucVuId: "",
    role: "NHANVIEN",
  })
  const [account, setAccount] = useState({
    username: "",
    password: "",
  })
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})

  useEffect(() => {
    api.chucvu
      .list()
      .then((data) => setChucVus(data || []))
      .catch(() => {
        setChucVus([])
      })
  }, [])

  useEffect(() => {
    if (mode !== "edit") return
    const id = Number(params.id)
    if (!id) return
    setLoading(true)
    api.nhanvien
      .get(id)
      .then(async (nv) => {
        const matched = nv.chucVuId
          ? chucVus.find((cv) => cv.maChucVu === nv.chucVuId)
          : chucVus.find((cv) => cv.tenChucVu === nv.chucVu)
        setForm({
          hoTen: nv.hoTen || "",
          diaChi: nv.diaChi || "",
          soDienThoai: nv.soDienThoai || "",
          enabled: nv.enabled,
          chucVuId: matched ? String(matched.maChucVu) : "",
          role: "NHANVIEN",
        })
        if (!matched && nv.chucVu) {
          setPendingChucVuName(nv.chucVu)
        }
        if (nv.taiKhoanId) {
          setTaiKhoanId(nv.taiKhoanId)
          setHasAccount(true)
          try {
            const user = await api.users.get(nv.taiKhoanId)
            setAccount({ username: user.username || "", password: "" })
            setForm((prev) => ({ ...prev, role: user.role || "NHANVIEN" }))
          } catch {
            setAccount({ username: "", password: "" })
          }
        } else {
          setTaiKhoanId(null)
          setHasAccount(false)
          setAccount({ username: "", password: "" })
        }
      })
      .catch((err: any) => setError(err?.body || err?.message || "Tải nhân viên thất bại"))
      .finally(() => setLoading(false))
  }, [mode, params.id, chucVus])

  useEffect(() => {
    if (!pendingChucVuName || form.chucVuId) return
    const matched = chucVus.find((cv) => cv.tenChucVu === pendingChucVuName)
    if (matched) {
      setForm((prev) => ({ ...prev, chucVuId: String(matched.maChucVu) }))
      setPendingChucVuName(null)
    }
  }, [pendingChucVuName, chucVus, form.chucVuId])

  const onChange = (field: string, value: string | boolean) => {
    setForm((prev) => ({ ...prev, [field]: value }))
    if (fieldErrors[field]) {
      setFieldErrors((prev) => ({ ...prev, [field]: "" }))
    }
  }

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    const errors: Record<string, string> = {}
    const isCreate = mode === "create"
    const wantsAccount = account.username.trim() !== "" || account.password.trim() !== ""
    if (!form.hoTen.trim()) errors.hoTen = "Họ tên không được để trống"
    if (!form.diaChi.trim()) errors.diaChi = "Địa chỉ không được để trống"
    if (!form.soDienThoai.trim()) {
      errors.soDienThoai = "Số điện thoại không được để trống"
    } else if (!/^\d{9,11}$/.test(form.soDienThoai.trim())) {
      errors.soDienThoai = "Số điện thoại phải từ 9 đến 11 số"
    }
    if (isCreate && !account.username.trim()) errors.username = "Tên đăng nhập không được để trống"
    if (isCreate && !account.password.trim()) errors.password = "Mật khẩu không được để trống"
    if (!isCreate && wantsAccount && !hasAccount && !account.username.trim()) {
      errors.username = "Tên đăng nhập không được để trống"
    }
    if (!isCreate && wantsAccount && !hasAccount && !account.password.trim()) {
      errors.password = "Mật khẩu không được để trống"
    }
    setFieldErrors(errors)
    if (Object.keys(errors).length > 0) return
    const payload: any = {
      hoTen: form.hoTen,
      diaChi: form.diaChi,
      soDienThoai: form.soDienThoai,
      enabled: form.enabled,
    }
    if (form.chucVuId) {
      payload.chucVuId = Number(form.chucVuId)
    }
    if (hasAccount && taiKhoanId) {
      payload.taiKhoanId = taiKhoanId
    }
    setLoading(true)
    try {
      if (mode === "create") {
        let newUserId: number | null = null
        const created: any = await api.users.create({
          username: account.username.trim(),
          password: account.password.trim(),
          role: form.role,
          enabled: form.enabled,
        })
        newUserId = created?.id || null
        if (newUserId) {
          payload.taiKhoanId = newUserId
        }
        await api.nhanvien.create(payload)
      } else {
        const id = Number(params.id)
        if (hasAccount && taiKhoanId) {
          const updatePayload: any = { role: form.role }
          if (account.password.trim()) {
            updatePayload.password = account.password.trim()
          }
          await api.users.update(taiKhoanId, updatePayload)
        } else if (!hasAccount && account.username.trim() && account.password.trim()) {
          const created: any = await api.users.create({
            username: account.username.trim(),
            password: account.password.trim(),
            role: form.role,
            enabled: form.enabled,
          })
          const newUserId = created?.id || null
          if (newUserId) {
            payload.taiKhoanId = newUserId
          }
        }
        await api.nhanvien.update(id, payload)
      }
      navigate("/admin/employees")
    } catch (err: any) {
      setError(err?.body || err?.message || "Lưu thất bại")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="content-wrapper">
      <h1>{mode === "create" ? "Thêm nhân viên" : "Chỉnh sửa nhân viên"}</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="form-box">
        <form onSubmit={onSubmit} noValidate>
          <div className="form-section">
            <h3>Thông tin cá nhân</h3>
            <div className="form-group">
              <label>Họ tên</label>
              <input value={form.hoTen} onChange={(event) => onChange("hoTen", event.target.value)} />
              {fieldErrors.hoTen ? <div className="field-error">{fieldErrors.hoTen}</div> : null}
            </div>
            <div className="form-group">
              <label>Địa chỉ</label>
              <input value={form.diaChi} onChange={(event) => onChange("diaChi", event.target.value)} />
              {fieldErrors.diaChi ? <div className="field-error">{fieldErrors.diaChi}</div> : null}
            </div>
            <div className="form-group">
              <label>Vai trò</label>
              <select value={form.role} onChange={(event) => onChange("role", event.target.value)}>
                <option value="NHANVIEN">Nhân viên</option>
                <option value="ADMIN">Quản trị</option>
              </select>
            </div>
            <div className="form-group">
              <label>Số điện thoại</label>
              <input
                value={form.soDienThoai}
                onChange={(event) => onChange("soDienThoai", event.target.value.replace(/\D/g, ""))}
              />
              {fieldErrors.soDienThoai ? <div className="field-error">{fieldErrors.soDienThoai}</div> : null}
            </div>
            <div className="form-group">
              <label>Trạng thái</label>
              <div className="radio-group">
                <label>
                  <input
                    type="radio"
                    name="enabled"
                    checked={form.enabled === true}
                    onChange={() => onChange("enabled", true)}
                  />
                  Hoạt động
                </label>
                <label>
                  <input
                    type="radio"
                    name="enabled"
                    checked={form.enabled === false}
                    onChange={() => onChange("enabled", false)}
                  />
                  Không hoạt động
                </label>
              </div>
            </div>
          </div>

          <div className="form-section">
            <h3>{mode === "create" ? "Tài khoản (bắt buộc)" : "Tài khoản (tùy chọn)"}</h3>
            <div className="form-group">
              <label>Tên đăng nhập</label>
              <input
                value={account.username}
                onChange={(event) => {
                  setAccount((prev) => ({ ...prev, username: event.target.value }))
                  if (fieldErrors.username) setFieldErrors((prev) => ({ ...prev, username: "" }))
                }}
                readOnly={hasAccount}
                placeholder={hasAccount ? "" : "Nhập tên đăng nhập"}
              />
              {fieldErrors.username ? <div className="field-error">{fieldErrors.username}</div> : null}
            </div>
            <div className="form-group">
              <label>{hasAccount ? "Mật khẩu mới (bỏ trống nếu giữ nguyên)" : "Mật khẩu"}</label>
              <input
                type="password"
                value={account.password}
                onChange={(event) => {
                  setAccount((prev) => ({ ...prev, password: event.target.value }))
                  if (fieldErrors.password) setFieldErrors((prev) => ({ ...prev, password: "" }))
                }}
                placeholder={hasAccount ? "Nhập nếu muốn đổi mật khẩu" : "Nhập mật khẩu"}
              />
              {fieldErrors.password ? <div className="field-error">{fieldErrors.password}</div> : null}
            </div>
          </div>

          <div className="form-actions form-actions--equal">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? "Đang lưu..." : "Lưu"}
            </button>
            <button type="button" className="btn btn-cancel" onClick={() => navigate("/admin/employees")}>
              Hủy
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
