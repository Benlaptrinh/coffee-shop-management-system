import { useEffect, useMemo, useState } from "react"
import api from "../../api"
import { formatDateTime, formatNumber, toDigits } from "../../utils/format"

type Unit = { maDonViTinh: number; tenDonVi: string }
type StockItem = {
  maHangHoa: number
  tenHangHoa: string
  soLuong: number
  donVi: string
  donGia: number
  ngayNhapGanNhat?: string
  ngayXuatGanNhat?: string
}

export default function AdminWarehouse() {
  const [items, setItems] = useState<StockItem[]>([])
  const [units, setUnits] = useState<Unit[]>([])
  const [keyword, setKeyword] = useState("")
  const [search, setSearch] = useState("")
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [editItem, setEditItem] = useState<StockItem | null>(null)

  const [nhapForm, setNhapForm] = useState({
    tenHangHoa: "",
    soLuongRaw: "",
    donGiaRaw: "",
    donViTinhId: "",
    ngayNhap: "",
  })
  const [xuatForm, setXuatForm] = useState({
    hangHoaId: "",
    soLuongRaw: "",
    ngayXuat: "",
  })
  const [editForm, setEditForm] = useState({
    id: "",
    tenHangHoa: "",
    soLuongRaw: "",
    donGiaRaw: "",
    donViTinhId: "",
  })

  const load = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.hanghoa.kho(search || undefined)
      setItems(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Failed to load warehouse")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    api.donvitinh
      .list()
      .then((data) => setUnits(data))
      .catch(() => {})
  }, [])

  useEffect(() => {
    load()
  }, [search])

  const filteredItems = useMemo(() => items, [items])

  const onNhapSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    const payload = {
      tenHangHoa: nhapForm.tenHangHoa.trim(),
      soLuong: Number(nhapForm.soLuongRaw || 0),
      donViTinhId: nhapForm.donViTinhId ? Number(nhapForm.donViTinhId) : null,
      donGia: Number(nhapForm.donGiaRaw || 0),
      ngayNhap: nhapForm.ngayNhap,
    }
    if (!payload.tenHangHoa || !payload.soLuong || !payload.donGia || !payload.ngayNhap) {
      setError("Missing required fields")
      return
    }
    try {
      await api.hanghoa.nhap(payload)
      setNhapForm({ tenHangHoa: "", soLuongRaw: "", donGiaRaw: "", donViTinhId: "", ngayNhap: "" })
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Nhap hang failed")
    }
  }

  const onXuatSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    const payload = {
      hangHoaId: Number(xuatForm.hangHoaId || 0),
      soLuong: Number(xuatForm.soLuongRaw || 0),
      ngayXuat: xuatForm.ngayXuat,
    }
    if (!payload.hangHoaId || !payload.soLuong || !payload.ngayXuat) {
      setError("Missing required fields")
      return
    }
    try {
      await api.hanghoa.xuat(payload)
      setXuatForm({ hangHoaId: "", soLuongRaw: "", ngayXuat: "" })
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Xuat hang failed")
    }
  }

  const onEditSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!editItem) return
    setError(null)
    const payload = {
      id: Number(editForm.id),
      tenHangHoa: editForm.tenHangHoa.trim(),
      soLuong: Number(editForm.soLuongRaw || 0),
      donViTinhId: editForm.donViTinhId ? Number(editForm.donViTinhId) : null,
      donGia: Number(editForm.donGiaRaw || 0),
    }
    if (!payload.tenHangHoa || !payload.soLuong || !payload.donViTinhId || !payload.donGia) {
      setError("Missing required fields")
      return
    }
    try {
      await api.hanghoa.update(payload)
      setEditItem(null)
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Update failed")
    }
  }

  const onEdit = (item: StockItem) => {
    const matchUnit = units.find((u) => u.tenDonVi === item.donVi)
    setEditItem(item)
    setEditForm({
      id: String(item.maHangHoa),
      tenHangHoa: item.tenHangHoa,
      soLuongRaw: toDigits(String(item.soLuong)),
      donGiaRaw: toDigits(String(item.donGia)),
      donViTinhId: matchUnit ? String(matchUnit.maDonViTinh) : "",
    })
  }

  const onDelete = async (id: number) => {
    if (!window.confirm("Delete this item?")) return
    try {
      await api.hanghoa.delete(id)
      await load()
    } catch (err: any) {
      setError(err?.body || err?.message || "Delete failed")
    }
  }

  return (
    <div className="content-wrapper">
      <h1>Kho hang</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="warehouse-panels">
        {!editItem ? (
          <div className="form-box">
            <h2 style={{ margin: "0 0 10px" }}>Nhap hang hoa</h2>
            <form onSubmit={onNhapSubmit} noValidate>
              <div className="form-group">
                <label>Ten hang</label>
                <input value={nhapForm.tenHangHoa} onChange={(event) => setNhapForm((prev) => ({ ...prev, tenHangHoa: event.target.value }))} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>So luong</label>
                  <input
                    inputMode="numeric"
                    value={formatNumber(nhapForm.soLuongRaw)}
                    onChange={(event) => setNhapForm((prev) => ({ ...prev, soLuongRaw: toDigits(event.target.value) }))}
                  />
                </div>
                <div className="form-group">
                  <label>Don gia</label>
                  <input
                    inputMode="numeric"
                    value={formatNumber(nhapForm.donGiaRaw)}
                    onChange={(event) => setNhapForm((prev) => ({ ...prev, donGiaRaw: toDigits(event.target.value) }))}
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Don vi</label>
                  <select value={nhapForm.donViTinhId} onChange={(event) => setNhapForm((prev) => ({ ...prev, donViTinhId: event.target.value }))}>
                    <option value="">-- Select --</option>
                    {units.map((unit) => (
                      <option key={unit.maDonViTinh} value={unit.maDonViTinh}>
                        {unit.tenDonVi}
                      </option>
                    ))}
                  </select>
                </div>
                <div className="form-group">
                  <label>Ngay nhap</label>
                  <input type="datetime-local" value={nhapForm.ngayNhap} onChange={(event) => setNhapForm((prev) => ({ ...prev, ngayNhap: event.target.value }))} />
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">
                  Nhap hang
                </button>
              </div>
            </form>
          </div>
        ) : (
          <div className="form-box">
            <h2 style={{ margin: "0 0 10px" }}>Chinh sua hang hoa</h2>
            <form onSubmit={onEditSubmit} noValidate>
              <div className="form-group">
                <label>Ten hang</label>
                <input value={editForm.tenHangHoa} onChange={(event) => setEditForm((prev) => ({ ...prev, tenHangHoa: event.target.value }))} />
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>So luong</label>
                  <input
                    inputMode="numeric"
                    value={formatNumber(editForm.soLuongRaw)}
                    onChange={(event) => setEditForm((prev) => ({ ...prev, soLuongRaw: toDigits(event.target.value) }))}
                  />
                </div>
                <div className="form-group">
                  <label>Don gia</label>
                  <input
                    inputMode="numeric"
                    value={formatNumber(editForm.donGiaRaw)}
                    onChange={(event) => setEditForm((prev) => ({ ...prev, donGiaRaw: toDigits(event.target.value) }))}
                  />
                </div>
              </div>
              <div className="form-row">
                <div className="form-group">
                  <label>Don vi</label>
                  <select value={editForm.donViTinhId} onChange={(event) => setEditForm((prev) => ({ ...prev, donViTinhId: event.target.value }))}>
                    <option value="">-- Select --</option>
                    {units.map((unit) => (
                      <option key={unit.maDonViTinh} value={unit.maDonViTinh}>
                        {unit.tenDonVi}
                      </option>
                    ))}
                  </select>
                </div>
              </div>
              <div className="form-actions">
                <button type="submit" className="btn btn-primary">
                  Save
                </button>
                <button type="button" className="btn btn-cancel" onClick={() => setEditItem(null)}>
                  Cancel
                </button>
              </div>
            </form>
          </div>
        )}

        <div className="form-box">
          <h2 style={{ margin: "0 0 10px" }}>Xuat hang hoa</h2>
          <form onSubmit={onXuatSubmit} noValidate>
            <div className="form-group">
              <label>Hang hoa</label>
              <select value={xuatForm.hangHoaId} onChange={(event) => setXuatForm((prev) => ({ ...prev, hangHoaId: event.target.value }))}>
                <option value="">-- Select --</option>
                {items.map((item) => (
                  <option key={item.maHangHoa} value={item.maHangHoa}>
                    {item.tenHangHoa} | {item.donVi} (Ton: {formatNumber(item.soLuong)})
                  </option>
                ))}
              </select>
            </div>
            <div className="form-row">
              <div className="form-group">
                <label>So luong xuat</label>
                <input
                  inputMode="numeric"
                  value={formatNumber(xuatForm.soLuongRaw)}
                  onChange={(event) => setXuatForm((prev) => ({ ...prev, soLuongRaw: toDigits(event.target.value) }))}
                />
              </div>
              <div className="form-group">
                <label>Ngay xuat</label>
                <input type="datetime-local" value={xuatForm.ngayXuat} onChange={(event) => setXuatForm((prev) => ({ ...prev, ngayXuat: event.target.value }))} />
              </div>
            </div>
            <div className="form-actions" style={{ marginTop: 8 }}>
              <button type="submit" className="btn btn-delete">
                Xuat hang
              </button>
            </div>
          </form>
        </div>
      </div>

      <form className="mb-3" style={{ marginTop: 12 }} onSubmit={(event) => (event.preventDefault(), setSearch(keyword.trim()))}>
        <div style={{ display: "flex", gap: 8, alignItems: "center" }}>
          <input
            type="text"
            name="keyword"
            placeholder="Search..."
            value={keyword}
            onChange={(event) => setKeyword(event.target.value)}
          />
          <button className="btn btn-secondary" type="submit">
            Search
          </button>
          <button className="btn btn-outline-secondary" type="button" onClick={() => (setKeyword(""), setSearch(""))}>
            Reset
          </button>
        </div>
      </form>

      {loading ? (
        <div className="page-loading">Loading...</div>
      ) : (
        <table className="data-table table-actions">
          <thead>
            <tr>
              <th>Ten hang</th>
              <th>So luong</th>
              <th>Don vi</th>
              <th className="text-right">Don gia</th>
              <th>Ngay nhap gan nhat</th>
              <th>Ngay xuat gan nhat</th>
              <th className="text-center">Hanh dong</th>
            </tr>
          </thead>
          <tbody>
            {filteredItems.map((item) => (
              <tr key={item.maHangHoa}>
                <td>{item.tenHangHoa}</td>
                <td>{formatNumber(item.soLuong)}</td>
                <td>{item.donVi}</td>
                <td className="text-right">{formatNumber(item.donGia)}</td>
                <td>{formatDateTime(item.ngayNhapGanNhat)}</td>
                <td>{formatDateTime(item.ngayXuatGanNhat)}</td>
                <td className="action-buttons">
                  <button type="button" className="btn btn-sm btn-edit" onClick={() => onEdit(item)}>
                    Sua
                  </button>
                  <button type="button" className="btn btn-sm btn-delete" onClick={() => onDelete(item.maHangHoa)}>
                    Xoa
                  </button>
                </td>
              </tr>
            ))}
            {filteredItems.length === 0 ? (
              <tr>
                <td colSpan={7} className="text-center text-muted">
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
