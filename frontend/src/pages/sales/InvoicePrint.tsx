import { useEffect, useState } from "react"
import { useParams } from "react-router-dom"
import api from "../../api"
import { formatDateTime, formatNumber } from "../../utils/format"
import { useRef } from "react"

type Invoice = {
  maHoaDon: number
  ngayThanhToan?: string
  tongTien: number
  tenNhanVien?: string
  tenKhachDat?: string
  items: Array<{ maThucDon: number; tenMon: string; soLuong: number; giaTaiThoiDiemBan: number; thanhTien: number }>
}

export default function InvoicePrint() {
  const params = useParams()
  const [invoice, setInvoice] = useState<Invoice | null>(null)
  const [loading, setLoading] = useState(false)
  const printedRef = useRef(false)

  useEffect(() => {
    const id = Number(params.id)
    if (!id) return
    setLoading(true)
    api.sales
      .invoice(id)
      .then((data) => setInvoice(data))
      .finally(() => setLoading(false))
  }, [params.id])

  useEffect(() => {
    if (invoice) {
      if (!printedRef.current) {
        printedRef.current = true
        setTimeout(() => window.print(), 300)
      }
    }
  }, [invoice])

  return (
    <div style={{ padding: 16, color: "#000", background: "#fff" }}>
      <h2 style={{ textAlign: "center" }}>HÓA ĐƠN</h2>
      {loading ? <p>Đang tải...</p> : null}
      {invoice ? (
        <>
          <p>Mã hóa đơn: {invoice.maHoaDon}</p>
          <p>Ngày: {invoice.ngayThanhToan ? formatDateTime(invoice.ngayThanhToan) : "-"}</p>
          <p>Nhân viên: {invoice.tenNhanVien || "-"}</p>
          <p>Khách hàng: {invoice.tenKhachDat || "-"}</p>
          <table style={{ width: "100%", borderCollapse: "collapse", marginTop: 12 }}>
            <thead>
              <tr>
                <th style={{ border: "1px solid #000", padding: 6 }}>Món</th>
                <th style={{ border: "1px solid #000", padding: 6 }}>SL</th>
                <th style={{ border: "1px solid #000", padding: 6 }}>Đơn giá</th>
                <th style={{ border: "1px solid #000", padding: 6 }}>Thành tiền</th>
              </tr>
            </thead>
            <tbody>
              {invoice.items.map((it) => (
                <tr key={it.maThucDon}>
                  <td style={{ border: "1px solid #000", padding: 6 }}>{it.tenMon}</td>
                  <td style={{ border: "1px solid #000", padding: 6, textAlign: "center" }}>{it.soLuong}</td>
                  <td style={{ border: "1px solid #000", padding: 6, textAlign: "right" }}>{formatNumber(it.giaTaiThoiDiemBan)}</td>
                  <td style={{ border: "1px solid #000", padding: 6, textAlign: "right" }}>{formatNumber(it.thanhTien)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          <h3 style={{ textAlign: "right" }}>Tổng: {formatNumber(invoice.tongTien)}</h3>
          <div style={{ textAlign: "center", marginTop: 12 }}>
            <button type="button" onClick={() => window.close()}>
              Đóng
            </button>
          </div>
        </>
      ) : (
        <p>Không có dữ liệu hóa đơn.</p>
      )}
    </div>
  )
}
