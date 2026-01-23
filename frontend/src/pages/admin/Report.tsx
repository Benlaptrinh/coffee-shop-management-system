import { useState } from "react"

type ReportType = "FINANCE" | "SALES" | "STAFF"

export default function AdminReport() {
  const [type, setType] = useState<ReportType>("FINANCE")
  const [from, setFrom] = useState("")
  const [to, setTo] = useState("")

  return (
    <div className="content-wrapper report-page">
      <h2 className="report-title">Thống kê - báo cáo</h2>

      <form className="report-form" onSubmit={(event) => event.preventDefault()}>
        <fieldset className="report-types">
          <legend>Loại báo cáo</legend>
          <label className="report-option">
            <input type="radio" name="type" checked={type === "FINANCE"} onChange={() => setType("FINANCE")} />
            Thu - Chi
          </label>
          <label className="report-option">
            <input type="radio" name="type" checked={type === "SALES"} onChange={() => setType("SALES")} />
            Bán hàng
          </label>
          <label className="report-option">
            <input type="radio" name="type" checked={type === "STAFF"} onChange={() => setType("STAFF")} />
            Nhân viên
          </label>
        </fieldset>
        {type !== "STAFF" ? (
          <div className="report-dates" id="reportDates">
            <label className="report-date">
              <span>Từ ngày:</span>
              <input type="date" value={from} onChange={(event) => setFrom(event.target.value)} required />
            </label>
            <label className="report-date">
              <span>Đến ngày:</span>
              <input type="date" value={to} onChange={(event) => setTo(event.target.value)} required />
            </label>
          </div>
        ) : null}
        <div className="report-actions">
          <button className="btn btn-primary" type="submit">
            Xem
          </button>
          <button className="btn btn-secondary no-print" type="button" onClick={() => window.print()}>
            In
          </button>
        </div>
      </form>

      <div>
        <p className="report-empty">Dữ liệu báo cáo chưa được kết nối API.</p>
      </div>
    </div>
  )
}
