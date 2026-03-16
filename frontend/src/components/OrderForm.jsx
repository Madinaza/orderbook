import { useMemo, useState } from "react";
import OrderPreviewCard from "./OrderPreviewCard";

const EMPTY_FORM = {
  instrument: "AAPL",
  side: "BUY",
  orderType: "LIMIT",
  limitPrice: "100.00",
  quantity: "1"
};

export default function OrderForm({ onSubmit, loading }) {
  const [form, setForm] = useState(EMPTY_FORM);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  const validationErrors = useMemo(() => {
    const errors = [];

    const instrument = String(form.instrument || "").trim().toUpperCase();
    const quantity = Number(form.quantity);
    const limitPrice = Number(form.limitPrice);

    if (!instrument) {
      errors.push("Instrument is required.");
    } else if (!/^[A-Z0-9._-]{2,16}$/.test(instrument)) {
      errors.push("Instrument must be 2-16 characters and use valid market symbol format.");
    }

    if (!Number.isInteger(quantity) || quantity <= 0) {
      errors.push("Quantity must be a positive whole number.");
    }

    if (form.orderType === "LIMIT") {
      if (!Number.isFinite(limitPrice) || limitPrice <= 0) {
        errors.push("Limit price must be a positive number for LIMIT orders.");
      }
    }

    return errors;
  }, [form]);

  const estimatedNotional = useMemo(() => {
    const quantity = Number(form.quantity);
    const limitPrice = Number(form.limitPrice);

    if (form.orderType !== "LIMIT") {
      return null;
    }

    if (!Number.isFinite(quantity) || !Number.isFinite(limitPrice)) {
      return null;
    }

    if (quantity <= 0 || limitPrice <= 0) {
      return null;
    }

    return quantity * limitPrice;
  }, [form]);

  const marketWarning = useMemo(() => {
    if (form.orderType !== "MARKET") {
      return "";
    }

    return "MARKET orders execute against available liquidity and price may differ from the last visible level.";
  }, [form.orderType]);

  function submit(event) {
    event.preventDefault();

    if (validationErrors.length > 0) {
      return;
    }

    const payload = {
      instrument: form.instrument.trim().toUpperCase(),
      side: form.side,
      orderType: form.orderType,
      limitPrice: form.orderType === "LIMIT" ? Number(form.limitPrice) : null,
      quantity: Number(form.quantity)
    };

    onSubmit(payload);
  }

  return (
    <div className="place-order-layout">
      <form className="invoice-form fade-up" onSubmit={submit}>
        <div className="form-topline">
          <h3>New Order Ticket</h3>
          <span className="coin-dot" />
        </div>

        <div className="form-grid">
          <label>
            <span>Instrument</span>
            <input
              name="instrument"
              value={form.instrument}
              onChange={updateField}
              placeholder="AAPL"
              maxLength={16}
            />
          </label>

          <label>
            <span>Side</span>
            <select name="side" value={form.side} onChange={updateField}>
              <option value="BUY">BUY</option>
              <option value="SELL">SELL</option>
            </select>
          </label>

          <label>
            <span>Order Type</span>
            <select name="orderType" value={form.orderType} onChange={updateField}>
              <option value="LIMIT">LIMIT</option>
              <option value="MARKET">MARKET</option>
            </select>
          </label>

          <label>
            <span>Quantity</span>
            <input
              name="quantity"
              type="number"
              min="1"
              step="1"
              value={form.quantity}
              onChange={updateField}
            />
          </label>

          <label className="full-width">
            <span>Limit Price</span>
            <input
              name="limitPrice"
              type="number"
              step="0.000001"
              value={form.limitPrice}
              onChange={updateField}
              disabled={form.orderType === "MARKET"}
              placeholder={form.orderType === "MARKET" ? "Disabled for MARKET orders" : "100.00"}
            />
          </label>
        </div>

        {validationErrors.length > 0 && (
          <div className="error-box form-inline-error">
            <strong>Validation failed:</strong>
            <ul className="validation-list">
              {validationErrors.map((error) => (
                <li key={error}>{error}</li>
              ))}
            </ul>
          </div>
        )}

        <button className="primary-button" disabled={loading || validationErrors.length > 0}>
          {loading ? "Submitting..." : "Place Order"}
        </button>
      </form>

      <OrderPreviewCard
        form={form}
        validationErrors={validationErrors}
        estimatedNotional={estimatedNotional}
        marketWarning={marketWarning}
      />
    </div>
  );
}