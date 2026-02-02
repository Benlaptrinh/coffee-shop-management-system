import { useState } from "react"
import api from "../api"

type PayPalPaymentProps = {
  amount: number
  description: string
  invoiceId: string
  onSuccess: (transactionId: string) => void
  onError: (error: string) => void
  onCancel: () => void
}

export default function PayPalPayment({
  amount,
  description,
  invoiceId,
  onSuccess,
  onError,
  onCancel,
}: PayPalPaymentProps) {
  const [loading, setLoading] = useState(false)
  const [orderId, setOrderId] = useState<string | null>(null)

  const handlePayPalPayment = async () => {
    setLoading(true)
    try {
      // Create PayPal order
      const orderResult = await api.paypal.createOrder({
        amount: amount,
        description: description,
        invoiceId: invoiceId,
      })

      if (orderResult.approvalUrl) {
        // Store orderId for later capture
        setOrderId(orderResult.orderId)
        // Redirect to PayPal for approval
        window.location.href = orderResult.approvalUrl
      } else {
        onError("Không thể tạo đơn hàng PayPal")
      }
    } catch (err: any) {
      console.error("PayPal create order error:", err)
      onError(err.body || "Lỗi khi tạo đơn hàng PayPal")
    } finally {
      setLoading(false)
    }
  }

  return (
    <div className="paypal-payment">
      <div className="paypal-info">
        <h4>Thanh toán PayPal</h4>
        <p className="paypal-amount">
          <strong>Số tiền:</strong> ${amount.toLocaleString()}
        </p>
        <p className="paypal-description">{description}</p>
      </div>

      <div className="paypal-actions">
        <button
          className="btn btn-paypal"
          onClick={handlePayPalPayment}
          disabled={loading}
        >
          {loading ? (
            "Đang xử lý..."
          ) : (
            <>
              <svg viewBox="0 0 24 24" width="20" height="20">
                <path fill="#003087" d="M7.076 21.337H2.47a.641.641 0 0 1-.633-.74L4.944.901C5.026.382 5.474 0 5.998 0h7.46c2.57 0 4.578.543 5.69 1.81 1.01 1.15 1.304 2.42 1.012 4.287-.023.143-.047.288-.077.437-.983 5.05-4.349 6.797-8.647 6.797h-2.19c-.524 0-.968.382-1.05.9l-1.12 7.106z"/>
                <path fill="#009CDE" d="M21.385 15.815h-2.19c-1.964 0-2.943-.658-3.695-1.538-.51-.595-.88-.913-1.316-1.094-.756-.314-1.777-.453-2.78-.453-.734 0-1.563.047-2.502.142l1.82 11.08h8.325l1.615-9.94c.102-.478-.262-.917-.777-.997z"/>
              </svg>
              Thanh toán với PayPal
            </>
          )}
        </button>

        <button className="btn btn-cancel" onClick={onCancel} disabled={loading}>
          Hủy
        </button>
      </div>

      <p className="paypal-note">
        Bạn sẽ được chuyển đến PayPal để hoàn tất thanh toán an toàn.
      </p>
    </div>
  )
}

