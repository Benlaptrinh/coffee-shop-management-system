import { useEffect, useState } from "react"
import { useNavigate, useSearchParams } from "react-router-dom"
import api from "../api"

export default function PayPalCallback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const [status, setStatus] = useState<"processing" | "success" | "error">("processing")
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const token = searchParams.get("token") // PayPal order ID
    const payerId = searchParams.get("PayerID")

    if (!token || !payerId) {
      setStatus("error")
      setError("Thiếu thông tin từ PayPal")
      return
    }

    // Capture the PayPal order
    api.paypal
      .captureOrder(token)
      .then((result) => {
        setStatus("success")
        // Redirect to sales page after 2 seconds
        setTimeout(() => {
          navigate("/staff/sales", { replace: true })
        }, 2000)
      })
      .catch((err) => {
        setStatus("error")
        setError(err.body || "Lỗi khi xác nhận thanh toán")
      })
  }, [searchParams, navigate])

  return (
    <div className="auth">
      <div className="auth-card" style={{ textAlign: "center" }}>
        {status === "processing" && (
          <>
            <div className="page-loading">
              <div style={{ fontSize: "48px", marginBottom: "16px" }}>⏳</div>
              <p>Đang xử lý thanh toán PayPal...</p>
              <p style={{ fontSize: "14px", color: "#6b7280" }}>
                Vui lòng chờ trong khi chúng tôi xác nhận giao dịch của bạn.
              </p>
            </div>
          </>
        )}

        {status === "success" && (
          <>
            <div style={{ fontSize: "64px", marginBottom: "16px" }}>✅</div>
            <h2>Thanh toán thành công!</h2>
            <p style={{ color: "#6b7280" }}>
              Giao dịch của bạn đã được xác nhận.
            </p>
            <p style={{ fontSize: "14px", color: "#6b7280" }}>
              Đang chuyển về trang bán hàng...
            </p>
          </>
        )}

        {status === "error" && (
          <>
            <div style={{ fontSize: "64px", marginBottom: "16px" }}>❌</div>
            <h2>Lỗi thanh toán</h2>
            <p style={{ color: "#b91c1c" }}>{error}</p>
            <button
              className="btn btn-primary"
              onClick={() => navigate("/staff/sales", { replace: true })}
              style={{ marginTop: "16px" }}
            >
              Quay lại trang bán hàng
            </button>
          </>
        )}
      </div>
    </div>
  )
}

