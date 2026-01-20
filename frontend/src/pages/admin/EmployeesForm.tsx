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
  const [chucVuLoading, setChucVuLoading] = useState(false)
  const [chucVuError, setChucVuError] = useState<string | null>(null)
  const [chucVuNewName, setChucVuNewName] = useState("")
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

  useEffect(() => {
    setChucVuLoading(true)
    setChucVuError(null)
    api.chucvu
      .list()
      .then((data) => setChucVus(data || []))
      .catch((err: any) => setChucVuError(err?.body || err?.message || "Failed to load chuc vu"))
      .finally(() => setChucVuLoading(false))
  }, [])

  useEffect(() => {
    if (mode !== "edit") return
    const id = Number(params.id)
    if (!id) return
    setLoading(true)
    api.nhanvien
      .get(id)
      .then(async (nv) => {
        const matched = chucVus.find((cv) => cv.tenChucVu === nv.chucVu)
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
      .catch((err: any) => setError(err?.body || err?.message || "Failed to load employee"))
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
  }

  const onSubmit = async (event: FormEvent<HTMLFormElement>) => {
    event.preventDefault()
    setError(null)
    const isCreate = mode === "create"
    const wantsAccount = account.username.trim() !== "" || account.password.trim() !== ""
    if (isCreate && (!account.username.trim() || !account.password.trim())) {
      setError("Username va password bat buoc khi tao tai khoan")
      return
    }
    if (!isCreate && wantsAccount && (!account.username.trim() || !account.password.trim()) && !hasAccount) {
      setError("Username va password bat buoc khi tao tai khoan")
      return
    }
    const payload: any = {
      hoTen: form.hoTen,
      diaChi: form.diaChi,
      soDienThoai: form.soDienThoai,
      enabled: form.enabled,
    }
    if (form.chucVuId) {
      payload.chucVu = { maChucVu: Number(form.chucVuId) }
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
          payload.taiKhoan = { maTaiKhoan: newUserId }
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
            payload.taiKhoan = { maTaiKhoan: newUserId }
          }
        }
        await api.nhanvien.update(id, payload)
      }
      navigate("/admin/employees")
    } catch (err: any) {
      setError(err?.body || err?.message || "Save failed")
    } finally {
      setLoading(false)
    }
  }

  const onCreateChucVu = async () => {
    const name = chucVuNewName.trim()
    if (!name) return
    setChucVuError(null)
    try {
      await api.chucvu.create({ tenChucVu: name })
      const data = await api.chucvu.list()
      setChucVus(data || [])
      const matched = (data || []).find((cv) => cv.tenChucVu === name)
      if (matched) {
        setForm((prev) => ({ ...prev, chucVuId: String(matched.maChucVu) }))
      }
      setChucVuNewName("")
    } catch (err: any) {
      setChucVuError(err?.body || err?.message || "Failed to create chuc vu")
    }
  }

  return (
    <div className="content-wrapper">
      <h1>{mode === "create" ? "Them nhan vien" : "Chinh sua nhan vien"}</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="form-box">
        <form onSubmit={onSubmit} noValidate>
          <div className="form-section">
            <h3>Thong tin ca nhan</h3>
            <div className="form-group">
              <label>Ho ten</label>
              <input value={form.hoTen} onChange={(event) => onChange("hoTen", event.target.value)} />
            </div>
            <div className="form-group">
              <label>Dia chi</label>
              <input value={form.diaChi} onChange={(event) => onChange("diaChi", event.target.value)} />
            </div>
            <div className="form-group">
              <label>Vai tro</label>
              <select value={form.role} onChange={(event) => onChange("role", event.target.value)}>
                <option value="NHANVIEN">NHANVIEN</option>
                <option value="ADMIN">ADMIN</option>
              </select>
            </div>
            <div className="form-group">
              <label>So dien thoai</label>
              <input
                value={form.soDienThoai}
                onChange={(event) => onChange("soDienThoai", event.target.value.replace(/\D/g, ""))}
              />
            </div>
            <div className="form-group">
              <label>Trang thai</label>
              <div className="radio-group">
                <label>
                  <input
                    type="radio"
                    name="enabled"
                    checked={form.enabled === true}
                    onChange={() => onChange("enabled", true)}
                  />
                  Hoat dong
                </label>
                <label>
                  <input
                    type="radio"
                    name="enabled"
                    checked={form.enabled === false}
                    onChange={() => onChange("enabled", false)}
                  />
                  Khong hoat dong
                </label>
              </div>
            </div>
          </div>

          <div className="form-section">
            <h3>{mode === "create" ? "Tai khoan (bat buoc)" : "Tai khoan (tuy chon)"}</h3>
            <div className="form-group">
              <label>Username</label>
              <input
                value={account.username}
                onChange={(event) => setAccount((prev) => ({ ...prev, username: event.target.value }))}
                readOnly={hasAccount}
                placeholder={hasAccount ? "" : "Nhap username"}
              />
            </div>
            <div className="form-group">
              <label>{hasAccount ? "Mat khau moi (bo trong neu giu nguyen)" : "Password"}</label>
              <input
                type="password"
                value={account.password}
                onChange={(event) => setAccount((prev) => ({ ...prev, password: event.target.value }))}
                placeholder={hasAccount ? "Nhap neu muon doi mat khau" : "Nhap mat khau"}
              />
            </div>
          </div>

          <div className="form-actions form-actions--equal">
            <button type="submit" className="btn btn-primary" disabled={loading}>
              {loading ? "Saving..." : "Save"}
            </button>
            <button type="button" className="btn btn-cancel" onClick={() => navigate("/admin/employees")}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
