import { useEffect, useState } from "react"
import { useNavigate, useParams } from "react-router-dom"
import api from "../../api"

type Props = {
  mode: "create" | "edit"
}

type ChucVu = {
  maChucVu: number
  tenChucVu: string
}

export default function AdminEmployeesForm({ mode }: Props) {
  const navigate = useNavigate()
  const params = useParams()
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [chucVus, setChucVus] = useState<ChucVu[]>([])
  const [form, setForm] = useState({
    hoTen: "",
    diaChi: "",
    soDienThoai: "",
    enabled: true,
    chucVuId: "",
  })

  useEffect(() => {
    api.chucvu
      .list()
      .then((data) => setChucVus(data || []))
      .catch(() => {})
  }, [])

  useEffect(() => {
    if (mode !== "edit") return
    const id = Number(params.id)
    if (!id) return
    setLoading(true)
    api.nhanvien
      .get(id)
      .then((nv) => {
        const matched = chucVus.find((cv) => cv.tenChucVu === nv.chucVu)
        setForm({
          hoTen: nv.hoTen || "",
          diaChi: nv.diaChi || "",
          soDienThoai: nv.soDienThoai || "",
          enabled: nv.enabled,
          chucVuId: matched ? String(matched.maChucVu) : "",
        })
      })
      .catch((err: any) => setError(err?.body || err?.message || "Failed to load employee"))
      .finally(() => setLoading(false))
  }, [mode, params.id, chucVus])

  const onChange = (field: string, value: string | boolean) => {
    setForm((prev) => ({ ...prev, [field]: value }))
  }

  const onSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
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
        await api.nhanvien.create(payload)
      } else {
        const id = Number(params.id)
        await api.nhanvien.update(id, payload)
      }
      navigate("/admin/employees")
    } catch (err: any) {
      setError(err?.body || err?.message || "Save failed")
    } finally {
      setLoading(false)
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
              <label>Chuc vu</label>
              <select value={form.chucVuId} onChange={(event) => onChange("chucVuId", event.target.value)}>
                <option value="">-- Select --</option>
                {chucVus.map((cv) => (
                  <option key={cv.maChucVu} value={cv.maChucVu}>
                    {cv.tenChucVu}
                  </option>
                ))}
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
