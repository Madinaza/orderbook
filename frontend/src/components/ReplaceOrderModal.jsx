import { useEffect, useState } from "react";
import { formatMoney } from "../lib/format";

export default function ReplaceOrderModal({ order, open, loading, onClose, onSubmit }) {
  const [form, setForm] = useState({ newLimitPrice: "", newQuantity: "" });
  const [error, setError] = useState("");

  useEffect(() => {
    if (!order) return;
    setForm({
      newLimitPrice: order.limitPrice ?? "",
      newQuantity: order.originalQty ?? ""
    });
    setError("");
  }, [order]);

  if (!open || !order) return null;

  function updateField(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  function submit(event) {
    event.preventDefault();
    setError("");

    const newLimitPrice = Number(form.newLimitPrice);
    const newQuantity = Number(form.newQuantity);

    if (!Number.isFinite(newLimitPrice) || newLimitPrice <= 0) {
      setError("New limit price must be a positive number.");
      return;
    }

    if (!Number.isInteger(newQuantity) || newQuantity <= 0) {
      setError("New quantity must be a positive whole number.");
      return;
    }

    onSubmit(order.id, {
      newLimitPrice,
      newQuantity
    });
  }

  return (
    <div className="modal-backdrop" onClick={onClose}>
      <div className="modal-card" onClick={(event) => event.stopPropagation()}>
        <div className="panel-header">
          <h3>Replace Order</h3>
          <button className="modal-close" onClick={onClose}>
            ×
          </button>
        </div>

        <div className="replace-order-summary">
          <div><strong>Order ID:</strong> {order.id}</div>
          <div><strong>Instrument:</strong> {order.instrument}</div>
          <div><strong>Side:</strong> {order.side}</div>
          <div><strong>Current Price:</strong> {formatMoney(order.limitPrice)}</div>
          <div><strong>Current Original Qty:</strong> {order.originalQty}</div>
          <div><strong>Open Qty:</strong> {order.openQty}</div>
        </div>

        <form className="auth-form" onSubmit={submit}>
          <label>
            <span>New Limit Price</span>
            <input
              name="newLimitPrice"
              type="number"
              step="0.000001"
              value={form.newLimitPrice}
              onChange={updateField}
            />
          </label>

          <label>
            <span>New Total Quantity</span>
            <input
              name="newQuantity"
              type="number"
              min="1"
              step="1"
              value={form.newQuantity}
              onChange={updateField}
            />
          </label>

          {error && <div className="error-box">{error}</div>}

          <div className="modal-actions">
            <button type="button" className="secondary-button" onClick={onClose}>
              Cancel
            </button>
            <button type="submit" className="primary-button" disabled={loading}>
              {loading ? "Saving..." : "Save Replace"}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}