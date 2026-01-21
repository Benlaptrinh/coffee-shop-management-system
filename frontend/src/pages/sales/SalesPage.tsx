import { useEffect, useMemo, useState } from "react"
import api from "../../api"
import Modal from "../../components/Modal"
import { formatDateTime, formatNumber, toDigits } from "../../utils/format"

type Table = { maBan: number; tenBan: string; tinhTrang: string }
type MenuItem = { maThucDon: number; tenMon: string; giaHienTai: number }
type InvoiceItem = {
  maThucDon: number
  tenMon: string
  soLuong: number
  giaTaiThoiDiemBan: number
  thanhTien: number
}
type Invoice = {
  maHoaDon: number
  maBan: number
  tinhTrang: string
  ngayGioTao: string
  ngayThanhToan?: string
  tongTien: number
  tenNhanVien?: string
  tenKhachDat?: string
  items: InvoiceItem[]
}
type TableDetail = {
  table: Table
  reservation: any | null
  invoice: Invoice | null
}

type ModalType = "view" | "menu" | "payment" | "reserve" | "move" | "merge" | "split"

export default function SalesPage() {
  const [tables, setTables] = useState<Table[]>([])
  const [selectedId, setSelectedId] = useState<number | null>(null)
  const [detail, setDetail] = useState<TableDetail | null>(null)
  const [menuItems, setMenuItems] = useState<MenuItem[]>([])
  const [menuQty, setMenuQty] = useState<Record<number, string>>({})
  const [modal, setModal] = useState<ModalType | null>(null)
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)

  const [reserveForm, setReserveForm] = useState({ tenKhach: "", sdt: "", ngayGio: "" })
  const [reserveErrors, setReserveErrors] = useState<Record<string, string>>({})

  const [paymentRaw, setPaymentRaw] = useState("")
  const [printInvoice, setPrintInvoice] = useState(false)

  const [moveTo, setMoveTo] = useState("")
  const [mergeSource, setMergeSource] = useState("")
  const [splitTo, setSplitTo] = useState("")
  const [splitQty, setSplitQty] = useState<Record<number, string>>({})

  const selectedTable = useMemo(() => tables.find((t) => t.maBan === selectedId) || null, [tables, selectedId])

  const loadTables = async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.sales.tables()
      setTables(data)
    } catch (err: any) {
      setError(err?.body || err?.message || "Failed to load tables")
    } finally {
      setLoading(false)
    }
  }

  const loadDetail = async (id: number) => {
    const data = await api.sales.table(id)
    setDetail(data)
    return data
  }

  useEffect(() => {
    loadTables()
  }, [])

  const isBusy = selectedTable?.tinhTrang === "DANG_SU_DUNG"
  const isReserved = selectedTable?.tinhTrang === "DA_DAT"

  const openModal = async (type: ModalType) => {
    if (!selectedId) return
    setModal(type)
    if (type === "menu") {
      const [menu, tableDetail] = await Promise.all([api.sales.menu(), loadDetail(selectedId)])
      setMenuItems(menu)
      const qtyMap: Record<number, string> = {}
      if (tableDetail.invoice?.items) {
        tableDetail.invoice.items.forEach((it) => {
          qtyMap[it.maThucDon] = String(it.soLuong)
        })
      }
      setMenuQty(qtyMap)
    }
    if (type === "view" || type === "payment" || type === "split") {
      const tableDetail = await loadDetail(selectedId)
      if (type === "payment") {
        setPaymentRaw(toDigits(String(tableDetail.invoice?.tongTien || "")))
        setPrintInvoice(false)
      }
      if (type === "split") {
        const next: Record<number, string> = {}
        tableDetail.invoice?.items?.forEach((it) => {
          next[it.maThucDon] = "0"
        })
        setSplitQty(next)
      }
    }
    if (type === "reserve") {
      setReserveForm({ tenKhach: "", sdt: "", ngayGio: "" })
      setReserveErrors({})
    }
    if (type === "move") {
      const empty = tables.filter((t) => t.tinhTrang === "TRONG" && t.maBan !== selectedId)
      setMoveTo(empty[0] ? String(empty[0].maBan) : "")
    }
    if (type === "merge") {
      const busy = tables.filter((t) => t.tinhTrang === "DANG_SU_DUNG" && t.maBan !== selectedId)
      setMergeSource(busy[0] ? String(busy[0].maBan) : "")
    }
    if (type === "split") {
      const empty = tables.filter((t) => t.tinhTrang === "TRONG" && t.maBan !== selectedId)
      setSplitTo(empty[0] ? String(empty[0].maBan) : "")
    }
  }

  const closeModal = () => setModal(null)

  const onMenuSubmit = async (event: React.FormEvent) => {
    event.preventDefault()
    if (!selectedId) return
    const params: Record<string, string> = {}
    menuItems.forEach((item) => {
      const raw = menuQty[item.maThucDon] ?? ""
      const normalized = toDigits(raw)
      const qty = normalized ? Number(normalized) : 0
      params[`qty_${item.maThucDon}`] = String(qty)
    })
    try {
      await api.sales.menuSelection(selectedId, params)
      closeModal()
      await loadTables()
      await loadDetail(selectedId)
    } catch (err: any) {
      setError(err?.body || err?.message || "Save menu failed")
    }
  }

  const onReserveSubmit = async () => {
    if (!selectedId) return
    const errors: Record<string, string> = {}
    if (!reserveForm.tenKhach.trim()) errors.tenKhach = "Required"
    if (!reserveForm.sdt.trim()) errors.sdt = "Required"
    if (!reserveForm.ngayGio) errors.ngayGio = "Required"
    setReserveErrors(errors)
    if (Object.keys(errors).length) return
    try {
      await api.sales.reserve(selectedId, reserveForm)
      closeModal()
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Reserve failed")
    }
  }

  const onPaymentSubmit = async () => {
    if (!selectedId) return
    const amount = Number(toDigits(paymentRaw || "0"))
    if (!amount) {
      setError("Amount is required")
      return
    }
    const invoiceId = detail?.invoice?.maHoaDon
    try {
      await api.sales.pay(selectedId, { amountPaid: amount, releaseTable: true })
      closeModal()
      await loadTables()
      if (printInvoice && invoiceId) {
        window.open(`/invoice/${invoiceId}`, "_blank")
      }
    } catch (err: any) {
      setError(err?.body || err?.message || "Payment failed")
    }
  }

  const onCancelInvoice = async () => {
    if (!selectedId) return
    if (!window.confirm("Cancel this invoice?")) return
    try {
      await api.sales.cancel(selectedId)
      closeModal()
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Cancel failed")
    }
  }

  const onCancelReservation = async () => {
    if (!selectedId) return
    if (!window.confirm("Cancel this reservation?")) return
    try {
      await api.sales.cancelReservation(selectedId)
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Cancel reservation failed")
    }
  }

  const onMove = async () => {
    if (!selectedId || !moveTo) return
    try {
      await api.sales.move({ fromBanId: selectedId, toBanId: Number(moveTo) })
      closeModal()
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Move failed")
    }
  }

  const onMerge = async () => {
    if (!selectedId || !mergeSource) return
    try {
      await api.sales.merge({ targetBanId: selectedId, sourceBanId: Number(mergeSource) })
      closeModal()
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Merge failed")
    }
  }

  const onSplit = async () => {
    if (!selectedId || !splitTo) return
    const items = Object.entries(splitQty)
      .map(([id, qty]) => ({ thucDonId: Number(id), soLuong: Number(toDigits(qty || "0")) }))
      .filter((it) => it.soLuong > 0)
      .map((it) => ({ ...it, fromBanId: selectedId }))
    if (!items.length) {
      setError("Select at least one item to split")
      return
    }
    try {
      await api.sales.split({ toBanId: Number(splitTo), items })
      closeModal()
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Split failed")
    }
  }

  const renderViewModal = () => {
    if (!detail) return null
    const invoice = detail.invoice
    const reservation = detail.reservation
    const reserved = !!reservation
    const isNewInvoice = invoice?.tinhTrang === "MOI_TAO"
    const isPaid = invoice?.tinhTrang === "DA_THANH_TOAN"

    if (!invoice && reserved) {
      return (
        <div className="modal-card modal-card--medium">
          <h3>Xem ban {detail.table.maBan}</h3>
          <p className="modal-hint">Ban da dat</p>
          <table className="modal-table">
            <tbody>
              <tr>
                <th>Khach</th>
                <td>{reservation?.tenKhach || "-"}</td>
              </tr>
              <tr>
                <th>So dien thoai</th>
                <td>{reservation?.sdt || "-"}</td>
              </tr>
              <tr>
                <th>Gio den</th>
                <td>{reservation?.ngayGioDat ? formatDateTime(reservation.ngayGioDat) : "-"}</td>
              </tr>
            </tbody>
          </table>
          <div className="modal-actions">
            <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
              Close
            </button>
          </div>
        </div>
      )
    }

    if (!invoice && !reserved) {
      return (
        <div className="modal-card modal-card--compact">
          <h3>Xem ban {detail.table.maBan}</h3>
          <p className="modal-hint">Chua goi mon</p>
          <div className="modal-actions">
            <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
              Close
            </button>
          </div>
        </div>
      )
    }

    return (
      <div className="modal-card modal-card--medium">
        <h3>Xem ban {detail.table.maBan}</h3>
        {reservation ? (
          <>
            <p className="modal-hint">Khach dat truoc</p>
            <table className="modal-table">
              <tbody>
                <tr>
                  <th>Khach</th>
                  <td>{reservation?.tenKhach || "-"}</td>
                </tr>
                <tr>
                  <th>So dien thoai</th>
                  <td>{reservation?.sdt || "-"}</td>
                </tr>
                <tr>
                  <th>Gio den</th>
                  <td>{reservation?.ngayGioDat ? formatDateTime(reservation.ngayGioDat) : "-"}</td>
                </tr>
              </tbody>
            </table>
          </>
        ) : null}
        <table className="modal-table">
          <thead>
            <tr>
              <th>Ten mon</th>
              <th>So luong</th>
              <th>Don gia</th>
              <th>Thanh tien</th>
            </tr>
          </thead>
          <tbody>
            {invoice?.items?.map((it) => (
              <tr key={it.maThucDon}>
                <td>{it.tenMon}</td>
                <td className="text-center">{it.soLuong}</td>
                <td className="text-right">{formatNumber(it.giaTaiThoiDiemBan)}</td>
                <td className="text-right">{formatNumber(it.thanhTien)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan={3} className="text-right">
                <strong>TONG:</strong>
              </td>
              <td className="text-right">
                <strong>{formatNumber(invoice?.tongTien || 0)}</strong>
              </td>
            </tr>
          </tfoot>
        </table>
        <div className="modal-actions">
          <button type="button" className="btn btn-sm" onClick={() => openModal("menu")}>
            Chon thuc don
          </button>
          <button type="button" className="btn btn-sm btn-primary" onClick={() => openModal("payment")}>
            Thanh toan
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
            Close
          </button>
          {isNewInvoice ? (
            <button type="button" className="btn btn-sm btn-delete" onClick={onCancelInvoice}>
              Huy hoa don
            </button>
          ) : null}
          {isPaid && invoice?.maHoaDon ? (
            <button type="button" className="btn btn-sm btn-secondary" onClick={() => window.open(`/invoice/${invoice.maHoaDon}`, "_blank")}>
              In hoa don
            </button>
          ) : null}
        </div>
      </div>
    )
  }

  const renderMenuModal = () => {
    return (
      <div className="modal-card modal-card--wide">
        <h3>Chon mon - Ban {selectedId}</h3>
        <form className="modal-form" onSubmit={onMenuSubmit}>
          <table className="modal-table">
            <thead>
              <tr>
                <th>Ten mon</th>
                <th className="text-right">Gia</th>
                <th>So luong</th>
              </tr>
            </thead>
            <tbody>
              {menuItems.map((item) => (
                <tr key={item.maThucDon}>
                  <td>{item.tenMon}</td>
                  <td className="text-right">{formatNumber(item.giaHienTai)}</td>
                  <td className="text-center">
                    <input
                      inputMode="numeric"
                      value={menuQty[item.maThucDon] ?? ""}
                      onChange={(event) => {
                        const digits = toDigits(event.target.value)
                        const normalized = digits === "" ? "" : digits.replace(/^0+(?=\d)/, "")
                        setMenuQty((prev) => ({ ...prev, [item.maThucDon]: normalized }))
                      }}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="modal-actions">
            <button type="submit" className="btn btn-sm btn-primary">
              Save
            </button>
            <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
              Cancel
            </button>
          </div>
        </form>
      </div>
    )
  }

  const renderPaymentModal = () => {
    const invoice = detail?.invoice
    if (!invoice) {
      return (
        <div className="modal-card modal-card--compact">
          <h3>Thanh toan - Ban {selectedId}</h3>
          <p className="modal-hint">Khong co hoa don</p>
          <div className="modal-actions">
            <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
              Close
            </button>
          </div>
        </div>
      )
    }
    return (
      <div className="modal-card modal-card--medium">
        <h3>Thanh toan - Ban {selectedId}</h3>
        <table className="modal-table">
          <thead>
            <tr>
              <th>Ten mon</th>
              <th>So luong</th>
              <th>Don gia</th>
              <th>Thanh tien</th>
            </tr>
          </thead>
          <tbody>
            {invoice.items.map((it) => (
              <tr key={it.maThucDon}>
                <td>{it.tenMon}</td>
                <td className="text-center">{it.soLuong}</td>
                <td className="text-right">{formatNumber(it.giaTaiThoiDiemBan)}</td>
                <td className="text-right">{formatNumber(it.thanhTien)}</td>
              </tr>
            ))}
          </tbody>
          <tfoot>
            <tr>
              <td colSpan={3} className="text-right">
                <strong>TONG:</strong>
              </td>
              <td className="text-right">
                <strong>{formatNumber(invoice.tongTien)}</strong>
              </td>
            </tr>
          </tfoot>
        </table>
        <div className="modal-form">
          <div className="form-group">
            <label>Tien khach dua</label>
            <input
              inputMode="numeric"
              value={formatNumber(paymentRaw)}
              onChange={(event) => setPaymentRaw(toDigits(event.target.value))}
            />
          </div>
        </div>
        <label className="modal-check">
          <input type="checkbox" checked={printInvoice} onChange={(event) => setPrintInvoice(event.target.checked)} />
          Print invoice
        </label>
        <div className="modal-actions">
          <button type="button" className="btn btn-sm btn-primary" onClick={onPaymentSubmit}>
            Confirm
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
            Cancel
          </button>
        </div>
      </div>
    )
  }

  const renderReserveModal = () => {
    return (
      <div className="modal-card modal-card--compact">
        <h3>Dat ban {selectedId}</h3>
        <div className="modal-form">
          <div className="form-group">
            <label>Ten khach</label>
            <input value={reserveForm.tenKhach} onChange={(event) => setReserveForm((prev) => ({ ...prev, tenKhach: event.target.value }))} />
            {reserveErrors.tenKhach ? <div className="field-error">{reserveErrors.tenKhach}</div> : null}
          </div>
          <div className="form-group">
            <label>SDT</label>
            <input
              value={reserveForm.sdt}
              onChange={(event) => setReserveForm((prev) => ({ ...prev, sdt: event.target.value.replace(/\\D/g, "") }))}
            />
            {reserveErrors.sdt ? <div className="field-error">{reserveErrors.sdt}</div> : null}
          </div>
          <div className="form-group">
            <label>Ngay gio den</label>
            <input type="datetime-local" value={reserveForm.ngayGio} onChange={(event) => setReserveForm((prev) => ({ ...prev, ngayGio: event.target.value }))} />
            {reserveErrors.ngayGio ? <div className="field-error">{reserveErrors.ngayGio}</div> : null}
          </div>
        </div>
        <div className="modal-actions">
          <button type="button" className="btn btn-sm btn-primary" onClick={onReserveSubmit}>
            Dat ban
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
            Close
          </button>
        </div>
      </div>
    )
  }

  const renderMoveModal = () => {
    const empty = tables.filter((t) => t.tinhTrang === "TRONG" && t.maBan !== selectedId)
    return (
      <div className="modal-card modal-card--compact">
        <h3>Chuyen ban {selectedId}</h3>
        <div className="modal-form">
          <div className="form-group">
            <label>Chon ban can chuyen den</label>
            <select value={moveTo} onChange={(event) => setMoveTo(event.target.value)}>
              <option value="">-- Select --</option>
              {empty.map((t) => (
                <option key={t.maBan} value={t.maBan}>
                  {t.tenBan}
                </option>
              ))}
            </select>
          </div>
        </div>
        <div className="modal-actions">
          <button type="button" className="btn btn-sm btn-primary" onClick={onMove} disabled={!moveTo}>
            Chuyen
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
            Cancel
          </button>
        </div>
      </div>
    )
  }

  const renderMergeModal = () => {
    const busy = tables.filter((t) => t.tinhTrang === "DANG_SU_DUNG" && t.maBan !== selectedId)
    return (
      <div className="modal-card modal-card--compact">
        <h3>Gop ban vao {selectedId}</h3>
        {busy.length === 0 ? (
          <div className="modal-hint">
            <em>No table to merge</em>
          </div>
        ) : (
          <>
            <div className="modal-form">
              <div className="form-group">
                <label>Chon ban nguon</label>
                <select value={mergeSource} onChange={(event) => setMergeSource(event.target.value)}>
                  <option value="">-- Select --</option>
                  {busy.map((t) => (
                    <option key={t.maBan} value={t.maBan}>
                      {t.tenBan}
                    </option>
                  ))}
                </select>
              </div>
            </div>
            <div className="modal-actions">
              <button type="button" className="btn btn-sm btn-primary" onClick={onMerge} disabled={!mergeSource}>
                Merge
              </button>
              <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
                Cancel
              </button>
            </div>
          </>
        )}
      </div>
    )
  }

  const renderSplitModal = () => {
    const empty = tables.filter((t) => t.tinhTrang === "TRONG" && t.maBan !== selectedId)
    const invoice = detail?.invoice
    if (!invoice) return null
    return (
      <div className="modal-card modal-card--medium">
        <h3>Tach ban {selectedId}</h3>
        <div className="modal-form">
          <div className="form-group">
            <label>Chon ban dich (trong)</label>
            <select value={splitTo} onChange={(event) => setSplitTo(event.target.value)}>
              <option value="">-- Select --</option>
              {empty.map((t) => (
                <option key={t.maBan} value={t.maBan}>
                  {t.tenBan}
                </option>
              ))}
            </select>
          </div>
        </div>
        <table className="modal-table">
          <thead>
            <tr>
              <th>Ten mon</th>
              <th>So luong</th>
              <th>So luong tach</th>
            </tr>
          </thead>
          <tbody>
            {invoice.items.map((it) => (
              <tr key={it.maThucDon}>
                <td>{it.tenMon}</td>
                <td className="text-center">{it.soLuong}</td>
                <td className="text-center">
                  <input
                    type="number"
                    min={0}
                    max={it.soLuong}
                    value={splitQty[it.maThucDon] || "0"}
                    onChange={(event) =>
                      setSplitQty((prev) => ({ ...prev, [it.maThucDon]: event.target.value }))
                    }
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="modal-actions">
          <button type="button" className="btn btn-sm btn-primary" onClick={onSplit} disabled={!splitTo}>
            Confirm
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
            Cancel
          </button>
        </div>
      </div>
    )
  }

  const tableClass = (table: Table) => {
    if (table.tinhTrang === "DANG_SU_DUNG") return "table-card table-busy"
    if (table.tinhTrang === "DA_DAT") return "table-card table-reserved"
    return "table-card table-free"
  }

  return (
    <div className="content-wrapper">
      <h1>Quan ly ban hang - Danh sach ban</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="status-legend">
        <span className="legend-item">
          <span className="legend-swatch table-free"></span> Trong
        </span>
        <span className="legend-item">
          <span className="legend-swatch table-busy"></span> Dang su dung
        </span>
        <span className="legend-item">
          <span className="legend-swatch table-reserved"></span> Da dat
        </span>
      </div>

      {loading ? (
        <div className="page-loading">Loading...</div>
      ) : (
        <div className="table-grid">
          {tables.map((t) => (
            <div
              key={t.maBan}
              className={`${tableClass(t)}${selectedId === t.maBan ? " active" : ""}`}
              data-id={t.maBan}
              onClick={() => setSelectedId(t.maBan)}
            >
              <div>{t.tenBan}</div>
            </div>
          ))}
        </div>
      )}

      <div className="action-bar sales-action-bar" style={{ marginTop: 16 }}>
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
          <button type="button" className="btn btn-sm" onClick={() => openModal("view")} disabled={!selectedId}>
            Xem ban
          </button>
          <button type="button" className="btn btn-sm" onClick={() => openModal("menu")} disabled={!selectedId}>
            Chon thuc don
          </button>
        </div>
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
          <button type="button" className="btn btn-sm" onClick={() => openModal("move")} disabled={!isBusy}>
            Chuyen
          </button>
          <button type="button" className="btn btn-sm" onClick={() => openModal("merge")} disabled={!isBusy}>
            Gop
          </button>
          <button type="button" className="btn btn-sm" onClick={() => openModal("split")} disabled={!isBusy}>
            Tach
          </button>
          <button type="button" className="btn btn-sm btn-delete" onClick={onCancelReservation} disabled={!isReserved}>
            Huy dat
          </button>
          <button type="button" className="btn btn-sm btn-edit" onClick={() => openModal("reserve")} disabled={isBusy || !selectedId}>
            Dat ban
          </button>
        </div>
      </div>

      <Modal open={modal !== null} onClose={closeModal}>
        {modal === "view" ? renderViewModal() : null}
        {modal === "menu" ? renderMenuModal() : null}
        {modal === "payment" ? renderPaymentModal() : null}
        {modal === "reserve" ? renderReserveModal() : null}
        {modal === "move" ? renderMoveModal() : null}
        {modal === "merge" ? renderMergeModal() : null}
        {modal === "split" ? renderSplitModal() : null}
      </Modal>
    </div>
  )
}
