import { useCallback, useEffect, useMemo, useRef, useState } from "react"
import Chart from "chart.js/auto"
import type { Chart as ChartInstance } from "chart.js"
import api from "../../api"
import Pagination from "../../components/Pagination"
import { formatDate, formatNumber } from "../../utils/format"

type ReportType = "FINANCE" | "SALES" | "STAFF"

type FinanceRow = {
  ngay: string
  thu: number
  chi: number
}

type SalesRow = {
  ngay: string
  soHoaDon: number
  doanhThu: number
}

type StaffRow = {
  trangThai: string
  soLuong: number
}

const toNumber = (value: number | string | null | undefined) => {
  if (value === null || value === undefined) return 0
  const num = typeof value === "number" ? value : Number(value)
  return Number.isNaN(num) ? 0 : num
}

const toInputDateValue = (date: Date) => {
  const yyyy = date.getFullYear()
  const mm = String(date.getMonth() + 1).padStart(2, "0")
  const dd = String(date.getDate()).padStart(2, "0")
  return `${yyyy}-${mm}-${dd}`
}

const formatDateLabel = (value: string) => {
  if (!value) return ""
  const dt = new Date(value)
  if (Number.isNaN(dt.getTime())) return value
  const dd = String(dt.getDate()).padStart(2, "0")
  const mm = String(dt.getMonth() + 1).padStart(2, "0")
  return `${dd}/${mm}`
}

export default function AdminReports() {
  const today = new Date()
  const startOfMonth = new Date(today.getFullYear(), today.getMonth(), 1)
  const [type, setType] = useState<ReportType>("FINANCE")
  const [fromDate, setFromDate] = useState(toInputDateValue(startOfMonth))
  const [toDate, setToDate] = useState(toInputDateValue(today))
  const [financeRows, setFinanceRows] = useState<FinanceRow[]>([])
  const [salesRows, setSalesRows] = useState<SalesRow[]>([])
  const [staffRows, setStaffRows] = useState<StaffRow[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({})
  const [financePage, setFinancePage] = useState(1)
  const [salesPage, setSalesPage] = useState(1)
  const pageSize = 10

  const financeCanvasRef = useRef<HTMLCanvasElement | null>(null)
  const salesCanvasRef = useRef<HTMLCanvasElement | null>(null)
  const staffCanvasRef = useRef<HTMLCanvasElement | null>(null)
  const financeChartRef = useRef<ChartInstance | null>(null)
  const salesChartRef = useRef<ChartInstance | null>(null)
  const staffChartRef = useRef<ChartInstance | null>(null)
  const didInitLoad = useRef(false)

  const financeTotals = useMemo(() => {
    return financeRows.reduce(
      (acc, row) => {
        acc.thu += toNumber(row.thu)
        acc.chi += toNumber(row.chi)
        return acc
      },
      { thu: 0, chi: 0 }
    )
  }, [financeRows])

  const salesTotals = useMemo(() => {
    return salesRows.reduce(
      (acc, row) => {
        acc.soHoaDon += toNumber(row.soHoaDon)
        acc.doanhThu += toNumber(row.doanhThu)
        return acc
      },
      { soHoaDon: 0, doanhThu: 0 }
    )
  }, [salesRows])

  const load = useCallback(async (nextType: ReportType = type) => {
    setError(null)
    setFieldErrors({})
    if (nextType !== "STAFF") {
      if (!fromDate || !toDate) {
        const errors: Record<string, string> = {}
        if (!fromDate) errors.fromDate = "Từ ngày không được để trống"
        if (!toDate) errors.toDate = "Đến ngày không được để trống"
        setFieldErrors(errors)
        setError("Vui lòng chọn đầy đủ ngày")
        return
      }
      const from = new Date(fromDate)
      const to = new Date(toDate)
      if (Number.isNaN(from.getTime()) || Number.isNaN(to.getTime())) {
        setFieldErrors({ fromDate: "Ngày không hợp lệ", toDate: "Ngày không hợp lệ" })
        setError("Ngày không hợp lệ")
        return
      }
      if (from > to) {
        setFieldErrors({ toDate: "Đến ngày không được trước từ ngày" })
        setError("Từ ngày không được sau đến ngày")
        return
      }
    }

    setLoading(true)
    try {
      if (nextType === "FINANCE") {
        const data = await api.report.finance(fromDate, toDate)
        setFinanceRows(data || [])
      } else if (nextType === "SALES") {
        const data = await api.report.sales(fromDate, toDate)
        setSalesRows(data || [])
      } else {
        const data = await api.report.staff()
        setStaffRows(data || [])
      }
    } catch (err: any) {
      setError(err?.body || err?.message || "Tải báo cáo thất bại")
    } finally {
      setLoading(false)
    }
  }, [fromDate, toDate, type])

  useEffect(() => {
    if (didInitLoad.current) return
    didInitLoad.current = true
    load("FINANCE")
  }, [load])

  useEffect(() => {
    if (financeChartRef.current) {
      financeChartRef.current.destroy()
      financeChartRef.current = null
    }
    if (type !== "FINANCE" || financeRows.length === 0 || !financeCanvasRef.current) return
    const labels = financeRows.map((row) => formatDateLabel(row.ngay))
    const thuData = financeRows.map((row) => toNumber(row.thu))
    const chiData = financeRows.map((row) => toNumber(row.chi))
    financeChartRef.current = new Chart(financeCanvasRef.current, {
      type: "line",
      data: {
        labels,
        datasets: [
          {
            label: "Thu",
            data: thuData,
            borderColor: "#16a34a",
            backgroundColor: "rgba(22, 163, 74, 0.15)",
            tension: 0.2,
          },
          {
            label: "Chi",
            data: chiData,
            borderColor: "#dc2626",
            backgroundColor: "rgba(220, 38, 38, 0.15)",
            tension: 0.2,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: "bottom" },
        },
        scales: {
          y: { beginAtZero: true },
        },
      },
    })
  }, [type, financeRows])

  useEffect(() => {
    if (type !== "FINANCE") return
    setFinancePage(1)
  }, [type, financeRows.length])

  useEffect(() => {
    const totalPages = Math.max(1, Math.ceil(financeRows.length / pageSize))
    if (financePage > totalPages) setFinancePage(totalPages)
  }, [financeRows.length, financePage, pageSize])

  useEffect(() => {
    if (salesChartRef.current) {
      salesChartRef.current.destroy()
      salesChartRef.current = null
    }
    if (type !== "SALES" || salesRows.length === 0 || !salesCanvasRef.current) return
    const labels = salesRows.map((row) => formatDateLabel(row.ngay))
    const doanhThuData = salesRows.map((row) => toNumber(row.doanhThu))
    const hoaDonData = salesRows.map((row) => toNumber(row.soHoaDon))
    salesChartRef.current = new Chart(salesCanvasRef.current, {
      type: "bar",
      data: {
        labels,
        datasets: [
          {
            label: "Doanh thu",
            data: doanhThuData,
            backgroundColor: "rgba(14, 116, 144, 0.55)",
            borderColor: "#0e7490",
            borderWidth: 1,
          },
          {
            label: "Số hóa đơn",
            data: hoaDonData,
            type: "line",
            borderColor: "#f97316",
            backgroundColor: "rgba(249, 115, 22, 0.2)",
            yAxisID: "y1",
            tension: 0.2,
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: "bottom" },
        },
        scales: {
          y: { beginAtZero: true },
          y1: {
            beginAtZero: true,
            position: "right",
            grid: { drawOnChartArea: false },
          },
        },
      },
    })
  }, [type, salesRows])

  useEffect(() => {
    if (type !== "SALES") return
    setSalesPage(1)
  }, [type, salesRows.length])

  useEffect(() => {
    const totalPages = Math.max(1, Math.ceil(salesRows.length / pageSize))
    if (salesPage > totalPages) setSalesPage(totalPages)
  }, [salesRows.length, salesPage, pageSize])

  useEffect(() => {
    if (staffChartRef.current) {
      staffChartRef.current.destroy()
      staffChartRef.current = null
    }
    if (type !== "STAFF" || staffRows.length === 0 || !staffCanvasRef.current) return
    const labels = staffRows.map((row) => row.trangThai)
    const counts = staffRows.map((row) => toNumber(row.soLuong))
    staffChartRef.current = new Chart(staffCanvasRef.current, {
      type: "doughnut",
      data: {
        labels,
        datasets: [
          {
            data: counts,
            backgroundColor: ["#22c55e", "#f97316", "#0ea5e9", "#a855f7"],
          },
        ],
      },
      options: {
        responsive: true,
        maintainAspectRatio: false,
        plugins: {
          legend: { position: "bottom" },
        },
      },
    })
  }, [type, staffRows])

  const onChangeType = (nextType: ReportType) => {
    setType(nextType)
    if (nextType === "STAFF") {
      load("STAFF")
    }
  }

  return (
    <div className="content-wrapper report-page">
      <h1 className="report-title">Thống kê - báo cáo</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <form
        className="report-form"
        onSubmit={(event) => {
          event.preventDefault()
          load(type)
        }}
      >
        <fieldset className="report-types">
          <legend>Loại báo cáo</legend>
          <label className="report-option">
            <input
              type="radio"
              name="type"
              value="FINANCE"
              checked={type === "FINANCE"}
              onChange={() => onChangeType("FINANCE")}
            />
            Thu - Chi
          </label>
          <label className="report-option">
            <input
              type="radio"
              name="type"
              value="SALES"
              checked={type === "SALES"}
              onChange={() => onChangeType("SALES")}
            />
            Bán hàng
          </label>
          <label className="report-option">
            <input
              type="radio"
              name="type"
              value="STAFF"
              checked={type === "STAFF"}
              onChange={() => onChangeType("STAFF")}
            />
            Nhân viên
          </label>
        </fieldset>

        <div className={`report-dates ${type === "STAFF" ? "is-hidden" : ""}`}>
          <label className="report-date">
            <span>Từ ngày:</span>
            <input
              type="date"
              value={fromDate}
              onChange={(event) => {
                setFromDate(event.target.value)
                if (fieldErrors.fromDate) setFieldErrors((prev) => ({ ...prev, fromDate: "" }))
              }}
              required={type !== "STAFF"}
              disabled={type === "STAFF"}
            />
            {fieldErrors.fromDate ? <div className="field-error">{fieldErrors.fromDate}</div> : null}
          </label>
          <label className="report-date">
            <span>Đến ngày:</span>
            <input
              type="date"
              value={toDate}
              onChange={(event) => {
                setToDate(event.target.value)
                if (fieldErrors.toDate) setFieldErrors((prev) => ({ ...prev, toDate: "" }))
              }}
              required={type !== "STAFF"}
              disabled={type === "STAFF"}
            />
            {fieldErrors.toDate ? <div className="field-error">{fieldErrors.toDate}</div> : null}
          </label>
        </div>

        <div className="report-actions">
          {type !== "STAFF" ? (
            <button className="btn btn-primary" type="submit">
              Xem
            </button>
          ) : null}
          <button className="btn btn-secondary no-print" type="button" onClick={() => window.print()}>
            In
          </button>
        </div>
      </form>

      {loading ? <div className="page-loading">Đang tải...</div> : null}

      {type === "FINANCE" ? (
        <>
          {financeRows.length > 0 ? (
            <div className="report-chart">
              <canvas ref={financeCanvasRef} />
            </div>
          ) : null}
          {financeRows.length > 0 ? (
            <table className="data-table report-table">
              <thead>
                <tr>
                  <th>Ngày</th>
                  <th>Thu</th>
                  <th>Chi</th>
                </tr>
              </thead>
              <tbody>
                {financeRows.slice((financePage - 1) * pageSize, financePage * pageSize).map((row, idx) => (
                  <tr key={`${row.ngay}-${idx}`}>
                    <td>{formatDate(row.ngay)}</td>
                    <td>{formatNumber(toNumber(row.thu))}</td>
                    <td>{formatNumber(toNumber(row.chi))}</td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr>
                  <th>Tổng</th>
                  <th>{formatNumber(financeTotals.thu)}</th>
                  <th>{formatNumber(financeTotals.chi)}</th>
                </tr>
              </tfoot>
            </table>
          ) : (
            <p className="report-empty">Chưa có dữ liệu</p>
          )}
          {financeRows.length > 0 ? (
            <Pagination page={financePage} pageSize={pageSize} total={financeRows.length} onPageChange={setFinancePage} />
          ) : null}
        </>
      ) : null}

      {type === "SALES" ? (
        <>
          <h3 className="report-subtitle">Bán hàng theo ngày</h3>
          {salesRows.length > 0 ? (
            <div className="report-chart">
              <canvas ref={salesCanvasRef} />
            </div>
          ) : null}
          {salesRows.length > 0 ? (
            <table className="data-table report-table">
              <thead>
                <tr>
                  <th>Ngày</th>
                  <th>Số hóa đơn</th>
                  <th>Doanh thu</th>
                </tr>
              </thead>
              <tbody>
                {salesRows.slice((salesPage - 1) * pageSize, salesPage * pageSize).map((row, idx) => (
                  <tr key={`${row.ngay}-${idx}`}>
                    <td>{formatDate(row.ngay)}</td>
                    <td>{toNumber(row.soHoaDon)}</td>
                    <td>{formatNumber(toNumber(row.doanhThu))}</td>
                  </tr>
                ))}
              </tbody>
              <tfoot>
                <tr>
                  <th>Tổng</th>
                  <th>{salesTotals.soHoaDon}</th>
                  <th>{formatNumber(salesTotals.doanhThu)}</th>
                </tr>
              </tfoot>
            </table>
          ) : (
            <p className="report-empty">Chưa có dữ liệu bán hàng</p>
          )}
          {salesRows.length > 0 ? (
            <Pagination page={salesPage} pageSize={pageSize} total={salesRows.length} onPageChange={setSalesPage} />
          ) : null}
        </>
      ) : null}

      {type === "STAFF" ? (
        <>
          <h3 className="report-subtitle">Báo cáo nhân viên</h3>
          {staffRows.length > 0 ? (
            <div className="report-chart report-chart--compact">
              <canvas ref={staffCanvasRef} />
            </div>
          ) : null}
          {staffRows.length === 0 ? <p className="report-empty">Chưa có dữ liệu nhân viên</p> : null}
        </>
      ) : null}
    </div>
  )
}
