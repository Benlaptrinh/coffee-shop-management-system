import { useEffect, useState } from "react"
import { useParams } from "react-router-dom"
import api from "../../api"
import { formatDateTime, formatNumber } from "../../utils/format"

type Invoice = {
  maHoaDon: number
  ngayThanhToan?: string
  tongTien: number
  items: Array<{ maThucDon: number; tenMon: string; soLuong: number; giaTaiThoiDiemBan: number; thanhTien: number }>
}

export default function InvoicePrint() {
  const params = useParams()
  const [invoice, setInvoice] = useState<Invoice | null>(null)
  const [loading, setLoading] = useState(false)

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
      setTimeout(() => window.print(), 300)
    }
  }, [invoice])

  return (
    <div style={{ padding: 16, color: "#000", background: "#fff" }}>
      <h2 style={{ textAlign: "center" }}>INVOICE</h2>
      {loading ? <p>Loading...</p> : null}
      {invoice ? (
        <>
          <p>Invoice ID: {invoice.maHoaDon}</p>
          <p>Date: {invoice.ngayThanhToan ? formatDateTime(invoice.ngayThanhToan) : "-"}</p>
          <table style={{ width: "100%", borderCollapse: "collapse", marginTop: 12 }}>
            <thead>
              <tr>
                <th style={{ border: "1px solid #000", padding: 6 }}>Item</th>
                <th style={{ border: "1px solid #000", padding: 6 }}>Qty</th>
                <th style={{ border: "1px solid #000", padding: 6 }}>Price</th>
                <th style={{ border: "1px solid #000", padding: 6 }}>Total</th>
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
          <h3 style={{ textAlign: "right" }}>Total: {formatNumber(invoice.tongTien)}</h3>
          <div style={{ textAlign: "center", marginTop: 12 }}>
            <button type="button" onClick={() => window.close()}>
              Close
            </button>
          </div>
        </>
      ) : (
        <p>No invoice data.</p>
      )}
    </div>
  )
}
