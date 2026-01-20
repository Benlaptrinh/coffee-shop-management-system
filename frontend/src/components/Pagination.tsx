type Props = {
  page: number
  pageSize: number
  total: number
  onPageChange: (page: number) => void
}

export default function Pagination({ page, pageSize, total, onPageChange }: Props) {
  const totalPages = Math.max(1, Math.ceil(total / pageSize))
  const currentPage = Math.min(Math.max(page, 1), totalPages)
  const start = total === 0 ? 0 : (currentPage - 1) * pageSize + 1
  const end = total === 0 ? 0 : Math.min(total, currentPage * pageSize)

  const goPrev = () => onPageChange(Math.max(1, currentPage - 1))
  const goNext = () => onPageChange(Math.min(totalPages, currentPage + 1))

  return (
    <div className="pagination">
      <button type="button" className={`btn btn-sm ${currentPage === 1 ? "btn-disabled" : ""}`} onClick={goPrev}>
        Prev
      </button>
      <span className="pagination-info">
        {start}-{end} / {total} • Trang {currentPage}/{totalPages}
      </span>
      <button
        type="button"
        className={`btn btn-sm ${currentPage === totalPages ? "btn-disabled" : ""}`}
        onClick={goNext}
      >
        Next
      </button>
    </div>
  )
}
