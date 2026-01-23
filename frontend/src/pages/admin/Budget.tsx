import { useEffect, useMemo, useState } from "react"
import api from "../../api"
import { formatDate, formatNumber, toDigits } from "../../utils/format"
import Pagination from "../../components/Pagination"

type Row = { ngay: string; thu: number; chi: number }

export default function AdminBudget() {
  const [from, setFrom] = useState("")
  const [to, setTo] = useState("")
  const [rows, setRows] = useState<Row[]>([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState<string | null>(null)
  const [form, setForm] = useState({ ngayChi: "", tenKhoanChi: "", soTien: "" })
  const [filterErrors, setFilterErrors] = useState<Record<string, string>>({})
  const [expenseErrors, setExpenseErrors] = useState<Record<string, string>>({})
  const [page, setPage] = useState(1)
  const pageSize = 10

  const totals = useMemo(() => {
    const totalThu = rows.reduce((sum, r) => sum + (r.thu || 0), 0)
    const totalChi = rows.reduce((sum, r) => sum + (r.chi || 0), 0)
    const net = totalThu - totalChi
    const ratio = totalThu ? (totalChi / totalThu) * 100 : null
    return { totalThu, totalChi, net, ratio }
  }, [rows])

  const load = async (fromDate: string, toDate: string) => {
    setLoading(true)
    setError(null)
    try {
      const data = await api.chitieu.report(fromDate, toDate)
      setRows(data || [])
    } catch (err: any) {
      setError(err?.body || err?.message || "Tải báo cáo thất bại")
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    if (from && to) {
      load(from, to)
    }
  }, [from, to])

  useEffect(() => {
    setPage(1)
  }, [from, to, rows.length])

  useEffect(() => {
    const totalPages = Math.max(1, Math.ceil(rows.length / pageSize))
    if (page > totalPages) setPage(totalPages)
  }, [rows.length, page, pageSize])

  const onSubmitFilter = (event: React.FormEvent) => {
    event.preventDefault()
    const errors: Record<string, string> = {}
    if (!from) errors.from = "Từ ngày không được để trống"
    if (!to) errors.to = "Đến ngày không được để trống"
    if (from && to) {
      const fromDate = new Date(from)
      const toDate = new Date(to)
      if (!Number.isNaN(fromDate.getTime()) && !Number.isNaN(toDate.getTime()) && fromDate > toDate) {
        errors.to = "Đến ngày không được trước từ ngày"
      }
    }
    setFilterErrors(errors)
    if (Object.keys(errors).length > 0) return
    if (from && to) load(from, to)
  }

  const onSubmitExpense = async (event: React.FormEvent) => {
    event.preventDefault()
    setError(null)
    const payload = {
      ngayChi: form.ngayChi,
      tenKhoanChi: form.tenKhoanChi.trim(),
      soTien: Number(form.soTien || 0),
    }
    const errors: Record<string, string> = {}
    if (!payload.ngayChi) errors.ngayChi = "Ngày chi không được để trống"
    if (!payload.tenKhoanChi) errors.tenKhoanChi = "Khoản chi không được để trống"
    if (!payload.soTien || payload.soTien < 1) errors.soTien = "Số tiền không hợp lệ"
    setExpenseErrors(errors)
    if (Object.keys(errors).length > 0) return
    try {
      await api.chitieu.create(payload)
      setForm({ ngayChi: "", tenKhoanChi: "", soTien: "" })
      setExpenseErrors({})
      if (from && to) await load(from, to)
    } catch (err: any) {
      setError(err?.body || err?.message || "Lưu thất bại")
    }
  }

  return (
    <div className="content-wrapper">
      <h1>Quản lý ngân sách</h1>
      {error ? <div className="alert alert-error">{String(error)}</div> : null}

      <form className="action-bar budget-filter" onSubmit={onSubmitFilter}>
        <div className="form-row budget-filter-row">
          <div className="form-group">
            <label>Từ ngày</label>
            <input
              type="date"
              value={from}
              onChange={(event) => {
                setFrom(event.target.value)
                if (filterErrors.from) setFilterErrors((prev) => ({ ...prev, from: "" }))
              }}
            />
            {filterErrors.from ? <div className="field-error">{filterErrors.from}</div> : null}
          </div>
          <div className="form-group">
            <label>Đến ngày</label>
            <input
              type="date"
              value={to}
              onChange={(event) => {
                setTo(event.target.value)
                if (filterErrors.to) setFilterErrors((prev) => ({ ...prev, to: "" }))
              }}
            />
            {filterErrors.to ? <div className="field-error">{filterErrors.to}</div> : null}
          </div>
          <div className="form-group budget-filter-actions">
            <label className="budget-filter-label" aria-hidden="true">
              &nbsp;
            </label>
            <div className="form-actions form-actions--equal">
              <button className="btn btn-primary btn-sm" type="submit">
                Xem
              </button>
                <button
                  className="btn btn-sm btn-cancel"
                  type="button"
                  onClick={() => {
                    setFrom("")
                    setTo("")
                    setRows([])
                    setFilterErrors({})
                  }}
                >
                  Đặt lại
                </button>
            </div>
          </div>
        </div>
      </form>

      {rows.length > 0 ? (
        <>
          <div className="budget-summary">
            <div className="budget-summary-card">
              <div className="budget-summary-label">Tổng thu</div>
              <div className="budget-summary-value">{formatNumber(totals.totalThu)}</div>
            </div>
            <div className="budget-summary-card">
              <div className="budget-summary-label">Tổng chi</div>
              <div className="budget-summary-value">{formatNumber(totals.totalChi)}</div>
            </div>
            <div className="budget-summary-card">
              <div className={`budget-summary-value${totals.net < 0 ? " is-negative" : ""}`}>{formatNumber(totals.net)}</div>
              <div className="budget-summary-label">Chênh lệch</div>
            </div>
            <div className="budget-summary-card">
              <div className="budget-summary-label">Tỷ lệ chi/thu</div>
              <div className="budget-summary-value">{totals.ratio ? `${totals.ratio.toFixed(2)}%` : "-"}</div>
            </div>
          </div>
        </>
      ) : null}

      {loading ? (
        <div className="page-loading">Đang tải...</div>
      ) : (
        <table className="data-table report-table">
          <thead>
            <tr>
              <th>Ngày</th>
              <th className="text-right">Thu</th>
              <th className="text-right">Chi</th>
            </tr>
          </thead>
          <tbody>
            {rows.slice((page - 1) * pageSize, page * pageSize).map((row, idx) => (
              <tr key={`${row.ngay}-${idx}`}>
                <td>{formatDate(row.ngay)}</td>
                <td className="text-right">{formatNumber(row.thu)}</td>
                <td className="text-right">{formatNumber(row.chi)}</td>
              </tr>
            ))}
            {rows.length === 0 ? (
              <tr>
                <td colSpan={3} className="text-center text-muted">
                  Không có dữ liệu
                </td>
              </tr>
            ) : null}
          </tbody>
        </table>
      )}
      {rows.length > 0 ? <Pagination page={page} pageSize={pageSize} total={rows.length} onPageChange={setPage} /> : null}

      <div className="form-box" style={{ marginTop: 16 }}>
        <h3>Thêm chi tiêu</h3>
        <form onSubmit={onSubmitExpense} noValidate>
          <div className="form-row">
            <div className="form-group">
              <label>Ngày</label>
              <input
                type="date"
                value={form.ngayChi}
                onChange={(event) => {
                  setForm((prev) => ({ ...prev, ngayChi: event.target.value }))
                  if (expenseErrors.ngayChi) setExpenseErrors((prev) => ({ ...prev, ngayChi: "" }))
                }}
              />
              {expenseErrors.ngayChi ? <div className="field-error">{expenseErrors.ngayChi}</div> : null}
            </div>
            <div className="form-group" style={{ flex: 1 }}>
              <label>Khoản chi</label>
              <input
                value={form.tenKhoanChi}
                onChange={(event) => {
                  setForm((prev) => ({ ...prev, tenKhoanChi: event.target.value }))
                  if (expenseErrors.tenKhoanChi) setExpenseErrors((prev) => ({ ...prev, tenKhoanChi: "" }))
                }}
              />
              {expenseErrors.tenKhoanChi ? <div className="field-error">{expenseErrors.tenKhoanChi}</div> : null}
            </div>
            <div className="form-group">
              <label>Số tiền</label>
              <input
                inputMode="numeric"
                value={formatNumber(form.soTien)}
                onChange={(event) => {
                  setForm((prev) => ({ ...prev, soTien: toDigits(event.target.value) }))
                  if (expenseErrors.soTien) setExpenseErrors((prev) => ({ ...prev, soTien: "" }))
                }}
              />
              {expenseErrors.soTien ? <div className="field-error">{expenseErrors.soTien}</div> : null}
            </div>
          </div>
          <div className="form-actions form-actions--equal">
            <button className="btn btn-primary btn-sm" type="submit">
              Lưu
            </button>
            <button
              className="btn btn-sm btn-cancel"
              type="button"
              onClick={() => {
                setForm({ ngayChi: "", tenKhoanChi: "", soTien: "" })
                setExpenseErrors({})
              }}
            >
              Hủy
            </button>
          </div>
        </form>
      </div>
    </div>
  )
}
