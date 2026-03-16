import { formatMoney } from "../lib/format";

export default function OrderPreviewCard({
  form,
  validationErrors = [],
  estimatedNotional,
  marketWarning
}) {
  const instrument = String(form.instrument || "").trim().toUpperCase() || "—";
  const side = form.side || "—";
  const orderType = form.orderType || "—";
  const quantity = Number(form.quantity || 0);
  const limitPrice =
    form.orderType === "LIMIT" && form.limitPrice !== ""
      ? Number(form.limitPrice)
      : null;

  return (
    <section className="panel-card fade-up">
      <div className="panel-header">
        <h3>Order Preview</h3>
        <span className="mini-badge yellow-badge">{orderType}</span>
      </div>

      <div className="order-preview-grid">
        <div className="order-preview-item">
          <span>Instrument</span>
          <strong>{instrument}</strong>
        </div>

        <div className="order-preview-item">
          <span>Side</span>
          <strong>{side}</strong>
        </div>

        <div className="order-preview-item">
          <span>Order Type</span>
          <strong>{orderType}</strong>
        </div>

        <div className="order-preview-item">
          <span>Quantity</span>
          <strong>{quantity || "—"}</strong>
        </div>

        <div className="order-preview-item">
          <span>Limit Price</span>
          <strong>{orderType === "LIMIT" ? formatMoney(limitPrice) : "MARKET"}</strong>
        </div>

        <div className="order-preview-item">
          <span>Estimated Notional</span>
          <strong>{estimatedNotional != null ? formatMoney(estimatedNotional) : "—"}</strong>
        </div>
      </div>

      {marketWarning && <div className="info-box">{marketWarning}</div>}

      {validationErrors.length > 0 && (
        <div className="error-box">
          <strong>Please fix the following:</strong>
          <ul className="validation-list">
            {validationErrors.map((error) => (
              <li key={error}>{error}</li>
            ))}
          </ul>
        </div>
      )}

      {validationErrors.length === 0 && !marketWarning && (
        <div className="success-box">
          Order ticket looks valid and ready for submission.
        </div>
      )}
    </section>
  );
}