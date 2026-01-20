import { useState } from "react"

type ReportType = "FINANCE" | "SALES" | "STAFF"

export default function AdminReport() {
  const [type, setType] = useState<ReportType>("FINANCE")
  const [from, setFrom] = useState("")
  const [to, setTo] = useState("")

  return (
    <div className="content-wrapper report-page">
      <h2 className="report-title">Thong ke - bao cao</h2>

      <form className="report-form" onSubmit={(event) => event.preventDefault()}>
        <fieldset className="report-types">
          <legend>Loai bao cao</legend>
          <label className="report-option">
            <input type="radio" name="type" checked={type === "FINANCE"} onChange={() => setType("FINANCE")} />
            Thu - Chi
          </label>
          <label className="report-option">
            <input type="radio" name="type" checked={type === "SALES"} onChange={() => setType("SALES")} />
            Ban hang
          </label>
          <label className="report-option">
            <input type="radio" name="type" checked={type === "STAFF"} onChange={() => setType("STAFF")} />
            Nhan vien
          </label>
        </fieldset>
        {type !== "STAFF" ? (
          <div className="report-dates" id="reportDates">
            <label className="report-date">
              <span>Tu ngay:</span>
              <input type="date" value={from} onChange={(event) => setFrom(event.target.value)} required />
            </label>
            <label className="report-date">
              <span>Den ngay:</span>
              <input type="date" value={to} onChange={(event) => setTo(event.target.value)} required />
            </label>
          </div>
        ) : null}
        <div className="report-actions">
          <button className="btn btn-primary" type="submit">
            View
          </button>
          <button className="btn btn-secondary no-print" type="button" onClick={() => window.print()}>
            Print
          </button>
        </div>
      </form>

      <div>
        <p className="report-empty">Report data is not wired to API yet.</p>
      </div>
    </div>
  )
}
