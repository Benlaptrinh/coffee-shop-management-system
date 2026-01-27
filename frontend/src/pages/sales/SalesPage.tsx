import { useEffect, useMemo, useState } from "react"
import api from "../../api"
import Modal from "../../components/Modal"
import { formatDateTime, formatNumber, toDigits } from "../../utils/format"
import { useForm } from "react-hook-form"

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
  const [menuErrors, setMenuErrors] = useState<Record<number, string>>({})
  const { register, handleSubmit, reset } = useForm<Record<string, string>>({
    defaultValues: {},
  })
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
      setError(err?.body || err?.message || "Tải danh sách bàn thất bại")
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
      const defaults: Record<string, string> = {}
      if (tableDetail.invoice?.items) {
        tableDetail.invoice.items.forEach((it) => {
          defaults[`qty_${it.maThucDon}`] = String(it.soLuong)
        })
      }
      menu.forEach((m) => {
        const k = `qty_${m.maThucDon}`
        if (!(k in defaults)) defaults[k] = ""
      })
      reset(defaults)
      setMenuErrors({})
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
          next[it.maThucDon] = ""
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

  const onMenuSubmit = handleSubmit(async (data) => {
    if (!selectedId) return
    const params: Record<string, string> = {}
    const errors: Record<number, string> = {}
    menuItems.forEach((item) => {
      const raw = data[`qty_${item.maThucDon}`] ?? ""
      const normalized = toDigits(String(raw))
      const qty = normalized ? Number(normalized) : 0
      if (qty > 10) {
        errors[item.maThucDon] = "Tối đa 10"
      }
      params[`qty_${item.maThucDon}`] = String(qty)
    })
    setMenuErrors(errors)
    if (Object.keys(errors).length) return
    try {
      await api.sales.menuSelection(selectedId, params)
      // Refresh data and show view modal for the same table
      await loadTables()
      await loadDetail(selectedId)
      setModal("view")
    } catch (err: any) {
      setError(err?.body || err?.message || "Lưu thực đơn thất bại")
    }
  })

  const onReserveSubmit = async () => {
    if (!selectedId) return
    const errors: Record<string, string> = {}
    if (!reserveForm.tenKhach.trim()) errors.tenKhach = "Không được để trống tên khách hàng"
    if (!reserveForm.sdt.trim()) errors.sdt = "Không được để trống số điện thoại khách hàng"
    if (!reserveForm.ngayGio) errors.ngayGio = "Không được để trống ngày đặt"
    setReserveErrors(errors)
    if (Object.keys(errors).length) return
    try {
      await api.sales.reserve(selectedId, reserveForm)
      closeModal()
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Đặt bàn thất bại")
    }
  }

  const onPaymentSubmit = async () => {
    if (!selectedId) return
    const amount = Number(toDigits(paymentRaw || "0"))
    if (!amount) {
      setError("Cần nhập số tiền")
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
      setError(err?.body || err?.message || "Thanh toán thất bại")
    }
  }

  const onCancelInvoice = async () => {
    if (!selectedId) return
    if (!window.confirm("Hủy hóa đơn này?")) return
    try {
      await api.sales.cancel(selectedId)
      closeModal()
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Hủy thất bại")
    }
  }

  const onCancelReservation = async () => {
    if (!selectedId) return
    if (!window.confirm("Hủy đặt bàn này?")) return
    try {
      await api.sales.cancelReservation(selectedId)
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Hủy đặt bàn thất bại")
    }
  }

  const onMove = async () => {
    if (!selectedId || !moveTo) return
    try {
      await api.sales.move({ fromBanId: selectedId, toBanId: Number(moveTo) })
      closeModal()
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Chuyển bàn thất bại")
    }
  }

  const onMerge = async () => {
    if (!selectedId || !mergeSource) return
    try {
      await api.sales.merge({ targetBanId: selectedId, sourceBanId: Number(mergeSource) })
      closeModal()
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Gộp bàn thất bại")
    }
  }

  const onSplit = async () => {
    if (!selectedId || !splitTo) return
    const items = Object.entries(splitQty)
      .map(([id, qty]) => ({ thucDonId: Number(id), soLuong: Number(toDigits(qty || "0")) }))
      .filter((it) => it.soLuong > 0)
    if (!items.length) {
      setError("Chọn ít nhất một món để tách")
      return
    }
    try {
      await api.sales.split({ fromBanId: selectedId, toBanId: Number(splitTo), items })
      closeModal()
      await loadTables()
    } catch (err: any) {
      setError(err?.body || err?.message || "Tách bàn thất bại")
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
          <h3>Xem bàn {detail.table.maBan}</h3>
          <p className="modal-hint">Bàn đã đặt</p>
          <table className="modal-table">
            <tbody>
              <tr>
                <th>Khách</th>
                <td>{reservation?.tenKhach || "-"}</td>
              </tr>
              <tr>
                <th>Số điện thoại</th>
                <td>{reservation?.sdt || "-"}</td>
              </tr>
              <tr>
                <th>Giờ đến</th>
                <td>{reservation?.ngayGioDat ? formatDateTime(reservation.ngayGioDat) : "-"}</td>
              </tr>
            </tbody>
          </table>
          <div className="modal-actions">
            <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
              Đóng
            </button>
          </div>
        </div>
      )
    }

    if (!invoice && !reserved) {
      return (
        <div className="modal-card modal-card--compact">
          <h3>Xem bàn {detail.table.maBan}</h3>
          <p className="modal-hint">Chưa gọi món</p>
          <div className="modal-actions">
            <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
              Đóng
            </button>
          </div>
        </div>
      )
    }

    return (
      <div className="modal-card modal-card--medium">
        <h3>Xem bàn {detail.table.maBan}</h3>
        {reservation ? (
          <>
            <p className="modal-hint">Khách đặt trước</p>
            <table className="modal-table">
              <tbody>
                <tr>
                  <th>Khách</th>
                  <td>{reservation?.tenKhach || "-"}</td>
                </tr>
                <tr>
                  <th>Số điện thoại</th>
                  <td>{reservation?.sdt || "-"}</td>
                </tr>
                <tr>
                  <th>Giờ đến</th>
                  <td>{reservation?.ngayGioDat ? formatDateTime(reservation.ngayGioDat) : "-"}</td>
                </tr>
              </tbody>
            </table>
          </>
        ) : null}
        <table className="modal-table">
          <thead>
            <tr>
              <th>Tên món</th>
              <th>Số lượng</th>
              <th>Đơn giá</th>
              <th>Thành tiền</th>
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
                <strong>TỔNG:</strong>
              </td>
              <td className="text-right">
                <strong>{formatNumber(invoice?.tongTien || 0)}</strong>
              </td>
            </tr>
          </tfoot>
        </table>
        <div className="modal-actions">
          <button type="button" className="btn btn-sm" onClick={() => openModal("menu")}>
            Chọn thực đơn
          </button>
          <button type="button" className="btn btn-sm btn-primary" onClick={() => openModal("payment")}>
            Thanh toán
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
            Đóng
          </button>
          {isNewInvoice ? (
            <button type="button" className="btn btn-sm btn-delete" onClick={onCancelInvoice}>
              Hủy hóa đơn
            </button>
          ) : null}
          {isPaid && invoice?.maHoaDon ? (
            <button type="button" className="btn btn-sm btn-secondary" onClick={() => window.open(`/invoice/${invoice.maHoaDon}`, "_blank")}>
              In hóa đơn
            </button>
          ) : null}
        </div>
      </div>
    )
  }

  const renderMenuModal = () => {
    return (
      <div className="modal-card modal-card--wide">
        <h3>Chọn món - Bàn {selectedId}</h3>
        <form className="modal-form" onSubmit={onMenuSubmit}>
          <table className="modal-table">
            <thead>
              <tr>
                <th>Tên món</th>
                <th className="text-right">Giá</th>
                <th>Số lượng</th>
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
                      {...register(`qty_${item.maThucDon}`, {
                        onChange: (e: any) => {
                          const digits = toDigits(e.target.value)
                          const normalized = digits === "" ? "" : digits.replace(/^0+(?=\d)/, "")
                          e.target.value = normalized
                          if (menuErrors[item.maThucDon]) {
                            setMenuErrors((prev) => {
                              const next = { ...prev }
                              delete next[item.maThucDon]
                              return next
                            })
                          }
                        },
                      })}
                    />
                    {menuErrors[item.maThucDon] ? <div className="field-error">{menuErrors[item.maThucDon]}</div> : null}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
          <div className="modal-actions">
            <button type="submit" className="btn btn-sm btn-primary">
              Lưu
            </button>
            <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
              Hủy
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
          <h3>Thanh toán - Bàn {selectedId}</h3>
          <p className="modal-hint">Không có hóa đơn</p>
          <div className="modal-actions">
            <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
              Đóng
            </button>
          </div>
        </div>
      )
    }
    return (
      <div className="modal-card modal-card--medium">
        <h3>Thanh toán - Bàn {selectedId}</h3>
        <table className="modal-table">
          <thead>
            <tr>
              <th>Tên món</th>
              <th>Số lượng</th>
              <th>Đơn giá</th>
              <th>Thành tiền</th>
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
                <strong>TỔNG:</strong>
              </td>
              <td className="text-right">
                <strong>{formatNumber(invoice.tongTien)}</strong>
              </td>
            </tr>
          </tfoot>
        </table>
        <div className="modal-form">
          <div className="form-group">
            <label>Tiền khách đưa</label>
            <input
              inputMode="numeric"
              value={formatNumber(paymentRaw)}
              onChange={(event) => setPaymentRaw(toDigits(event.target.value))}
            />
          </div>
        </div>
        <label className="modal-check">
          <input type="checkbox" checked={printInvoice} onChange={(event) => setPrintInvoice(event.target.checked)} />
          In hóa đơn
        </label>
        <div className="modal-actions">
          <button type="button" className="btn btn-sm btn-primary" onClick={onPaymentSubmit}>
            Xác nhận
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
            Hủy
          </button>
        </div>
      </div>
    )
  }

  const renderReserveModal = () => {
    return (
      <div className="modal-card modal-card--compact">
        <h3>Đặt bàn {selectedId}</h3>
        <div className="modal-form">
          <div className="form-group">
            <label>Tên khách</label>
            <input
              value={reserveForm.tenKhach}
              onChange={(event) => {
                setReserveForm((prev) => ({ ...prev, tenKhach: event.target.value }))
                if (reserveErrors.tenKhach) setReserveErrors((prev) => ({ ...prev, tenKhach: "" }))
              }}
            />
            {reserveErrors.tenKhach ? <div className="field-error">{reserveErrors.tenKhach}</div> : null}
          </div>
          <div className="form-group">
            <label>SDT</label>
            <input
              value={reserveForm.sdt}
              onChange={(event) => {
                setReserveForm((prev) => ({ ...prev, sdt: event.target.value.replace(/\\D/g, "") }))
                if (reserveErrors.sdt) setReserveErrors((prev) => ({ ...prev, sdt: "" }))
              }}
            />
            {reserveErrors.sdt ? <div className="field-error">{reserveErrors.sdt}</div> : null}
          </div>
          <div className="form-group">
            <label>Ngày giờ đến</label>
            <input
              type="datetime-local"
              value={reserveForm.ngayGio}
              onChange={(event) => {
                setReserveForm((prev) => ({ ...prev, ngayGio: event.target.value }))
                if (reserveErrors.ngayGio) setReserveErrors((prev) => ({ ...prev, ngayGio: "" }))
              }}
            />
            {reserveErrors.ngayGio ? <div className="field-error">{reserveErrors.ngayGio}</div> : null}
          </div>
        </div>
        <div className="modal-actions">
          <button type="button" className="btn btn-sm btn-primary" onClick={onReserveSubmit}>
            Đặt bàn
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
            Đóng
          </button>
        </div>
      </div>
    )
  }

  const renderMoveModal = () => {
    const empty = tables.filter((t) => t.tinhTrang === "TRONG" && t.maBan !== selectedId)
    return (
      <div className="modal-card modal-card--compact">
        <h3>Chuyển bàn {selectedId}</h3>
        <div className="modal-form">
          <div className="form-group">
            <label>Chọn bàn cần chuyển đến</label>
            <select value={moveTo} onChange={(event) => setMoveTo(event.target.value)}>
              <option value="">-- Chọn --</option>
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
            Chuyển
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
            Hủy
          </button>
        </div>
      </div>
    )
  }

  const renderMergeModal = () => {
    const busy = tables.filter((t) => t.tinhTrang === "DANG_SU_DUNG" && t.maBan !== selectedId)
    return (
      <div className="modal-card modal-card--compact">
        <h3>Gộp bàn vào {selectedId}</h3>
        {busy.length === 0 ? (
          <div className="modal-hint">
            <em>Không có bàn để gộp</em>
          </div>
        ) : (
          <>
            <div className="modal-form">
              <div className="form-group">
                <label>Chọn bàn nguồn</label>
                <select value={mergeSource} onChange={(event) => setMergeSource(event.target.value)}>
                  <option value="">-- Chọn --</option>
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
                Gộp
              </button>
              <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
                Hủy
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
        <h3>Tách bàn {selectedId}</h3>
        <div className="modal-form">
          <div className="form-group">
            <label>Chọn bàn đích (trống)</label>
            <select value={splitTo} onChange={(event) => setSplitTo(event.target.value)}>
              <option value="">-- Chọn --</option>
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
              <th>Tên món</th>
              <th>Số lượng</th>
              <th>Số lượng tách</th>
            </tr>
          </thead>
          <tbody>
            {invoice.items.map((it) => (
              <tr key={it.maThucDon}>
                <td>{it.tenMon}</td>
                <td className="text-center">{it.soLuong}</td>
                <td className="text-center">
                  <input
                    inputMode="numeric"
                    value={splitQty[it.maThucDon] ?? ""}
                    onChange={(event) => {
                      const digits = toDigits(event.target.value)
                      const normalized = digits === "" ? "" : digits.replace(/^0+(?=\d)/, "")
                      setSplitQty((prev) => ({ ...prev, [it.maThucDon]: normalized }))
                    }}
                  />
                </td>
              </tr>
            ))}
          </tbody>
        </table>
        <div className="modal-actions">
          <button type="button" className="btn btn-sm btn-primary" onClick={onSplit} disabled={!splitTo}>
            Xác nhận
          </button>
          <button type="button" className="btn btn-sm btn-secondary" onClick={closeModal}>
            Hủy
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
      <h1>Quản lý bán hàng - Danh sách bàn</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <div className="status-legend">
        <span className="legend-item">
          <span className="legend-swatch table-free"></span> Trống
        </span>
        <span className="legend-item">
          <span className="legend-swatch table-busy"></span> Đang sử dụng
        </span>
        <span className="legend-item">
          <span className="legend-swatch table-reserved"></span> Đã đặt
        </span>
      </div>

      {loading ? (
        <div className="page-loading">Đang tải...</div>
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
            Xem bàn
          </button>
          <button type="button" className="btn btn-sm" onClick={() => openModal("menu")} disabled={!selectedId}>
            Chọn thực đơn
          </button>
        </div>
        <div style={{ display: "flex", gap: 8, flexWrap: "wrap" }}>
          <button type="button" className="btn btn-sm" onClick={() => openModal("move")} disabled={!isBusy}>
            Chuyển
          </button>
          <button type="button" className="btn btn-sm" onClick={() => openModal("merge")} disabled={!isBusy}>
            Gộp
          </button>
          <button type="button" className="btn btn-sm" onClick={() => openModal("split")} disabled={!isBusy}>
            Tách
          </button>
          <button type="button" className="btn btn-sm btn-delete" onClick={onCancelReservation} disabled={!isReserved}>
            Hủy đặt
          </button>
          <button type="button" className="btn btn-sm btn-edit" onClick={() => openModal("reserve")} disabled={isBusy || !selectedId}>
            Đặt bàn
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
