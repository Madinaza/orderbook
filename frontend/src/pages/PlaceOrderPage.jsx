import { useState } from "react";
import OrderForm from "../components/OrderForm";

export default function PlaceOrderPage() {
  const [message, setMessage] = useState("Submit an order ticket to your trading-service.");
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");
  const [lastOrder, setLastOrder] = useState(null);

  async function submitOrder(payload) {
    setLoading(true);
    setError("");
    setMessage("");

    try {
      const { api } = await import("../lib/api");
      const response = await api.placeOrder(payload);

      setLastOrder({
        id: response.id,
        instrument: response.instrument,
        side: response.side,
        orderType: response.orderType,
        status: response.status,
        quantity: response.originalQty,
        openQty: response.openQty,
        price: response.limitPrice
      });

      setMessage(
        `Order placed successfully. Order ID: ${response.id}, instrument: ${response.instrument}, status: ${response.status}`
      );
    } catch (err) {
      setError(err.message || "Order placement failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <div className="dashboard-grid">
      <OrderForm onSubmit={submitOrder} loading={loading} />

      <section className="panel-card fade-up trading-notes-card">
        <div className="panel-header">
          <h3>Execution Notes</h3>
          <span className="mini-badge yellow-badge">LIVE API</span>
        </div>

        {message && <div className="success-box trading-notes-alert">{message}</div>}
        {error && <div className="error-box trading-notes-alert">{error}</div>}

        <div className="trading-notes-grid">
          <div className="trading-note-item">
            <div className="trading-note-label">Backend Route</div>
            <div className="trading-note-value">
              <span className="trading-note-code">POST /api/orders</span>
            </div>
          </div>

          <div className="trading-note-item">
            <div className="trading-note-label">Security</div>
            <div className="trading-note-value">
              JWT token from auth-service
            </div>
          </div>

          <div className="trading-note-item">
            <div className="trading-note-label">Projection</div>
            <div className="trading-note-value">
              market-data-service updates automatically after successful execution workflow
            </div>
          </div>

          <div className="trading-note-item">
            <div className="trading-note-label">Supported</div>
            <div className="trading-note-value">
              BUY / SELL, LIMIT / MARKET
            </div>
          </div>

          <div className="trading-note-item">
            <div className="trading-note-label">Real-time Effect</div>
            <div className="trading-note-value">
              Successful orders may affect order book depth and trade history immediately
            </div>
          </div>
        </div>

        {lastOrder && (
          <>
            <div className="section-bar bar-blue">LAST SUBMITTED ORDER</div>

            <div className="order-preview-grid">
              <div className="order-preview-item">
                <span>Order ID</span>
                <strong>{lastOrder.id}</strong>
              </div>

              <div className="order-preview-item">
                <span>Instrument</span>
                <strong>{lastOrder.instrument}</strong>
              </div>

              <div className="order-preview-item">
                <span>Side</span>
                <strong>{lastOrder.side}</strong>
              </div>

              <div className="order-preview-item">
                <span>Type</span>
                <strong>{lastOrder.orderType}</strong>
              </div>

              <div className="order-preview-item">
                <span>Status</span>
                <strong>{lastOrder.status}</strong>
              </div>

              <div className="order-preview-item">
                <span>Open Qty</span>
                <strong>{lastOrder.openQty}</strong>
              </div>
            </div>
          </>
        )}
      </section>
    </div>
  );
}