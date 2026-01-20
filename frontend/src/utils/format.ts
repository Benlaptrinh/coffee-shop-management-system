export function toDigits(value: string) {
  return (value || "").toString().replace(/\D/g, "")
}

export function formatNumber(value: number | string | null | undefined) {
  if (value === null || value === undefined) return ""
  const digits = toDigits(String(value))
  if (!digits) return ""
  return digits.replace(/\B(?=(\d{3})+(?!\d))/g, ".")
}

export function formatDate(value?: string | null) {
  if (!value) return "-"
  const dt = new Date(value)
  if (Number.isNaN(dt.getTime())) return value
  const dd = String(dt.getDate()).padStart(2, "0")
  const mm = String(dt.getMonth() + 1).padStart(2, "0")
  const yyyy = dt.getFullYear()
  return `${dd}/${mm}/${yyyy}`
}

export function formatDateTime(value?: string | null) {
  if (!value) return "-"
  const dt = new Date(value)
  if (Number.isNaN(dt.getTime())) return value
  const dd = String(dt.getDate()).padStart(2, "0")
  const mm = String(dt.getMonth() + 1).padStart(2, "0")
  const yyyy = dt.getFullYear()
  const hh = String(dt.getHours()).padStart(2, "0")
  const min = String(dt.getMinutes()).padStart(2, "0")
  return `${hh}:${min} ${dd}/${mm}/${yyyy}`
}

export function toInputDate(value?: string | null) {
  if (!value) return ""
  const dt = new Date(value)
  if (Number.isNaN(dt.getTime())) return ""
  const dd = String(dt.getDate()).padStart(2, "0")
  const mm = String(dt.getMonth() + 1).padStart(2, "0")
  const yyyy = dt.getFullYear()
  return `${yyyy}-${mm}-${dd}`
}

export function toInputDateTime(value?: string | null) {
  if (!value) return ""
  const dt = new Date(value)
  if (Number.isNaN(dt.getTime())) return ""
  const dd = String(dt.getDate()).padStart(2, "0")
  const mm = String(dt.getMonth() + 1).padStart(2, "0")
  const yyyy = dt.getFullYear()
  const hh = String(dt.getHours()).padStart(2, "0")
  const min = String(dt.getMinutes()).padStart(2, "0")
  return `${yyyy}-${mm}-${dd}T${hh}:${min}`
}
