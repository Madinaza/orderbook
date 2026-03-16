import { useEffect, useMemo, useState } from "react";
import { createPortal } from "react-dom";
import { api } from "../lib/api";
import { formatDate, formatMoney } from "../lib/format";

const MIN_LOADING_MS = 1200;

function ModalContent({ order, open, onClose }) {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState("");

  const filledQty = useMemo(() => {
    if (!order) return 0;
    return Math.max(Number(order.originalQty || 0) - Number(order.openQty || 0), 0);
  }, [order]);

  const fillPercent = useMemo(() => {
    if (!order || !order.originalQty) return "0.00";
    return ((filledQty / Number(order.originalQty)) * 100).toFixed(2);
  }, [order, filledQty]);

  useEffect(() => {
    if (!open) {
      setEvents([]);
      setError("");
      setLoading(false);
      return;
    }

    const previousOverflow = document.body.style.overflow;
    document.body.style.overflow = "hidden";

    function onKeyDown(event) {
      if (event.key === "Escape") {
        onClose();
      }
    }

    window.addEventListener("keydown", onKeyDown);

    return () => {
      document.body.style.overflow = previousOverflow;
      window.removeEventListener("keydown", onKeyDown);
    };
  }, [open, onClose]);

  useEffect(() => {
    let active = true;

    async function loadEvents() {
      if (!open || !order?.id) return;

      setLoading(true);
      setError("");
      setEvents([]);

      const start = Date.now();

      try {
        const data = await api.getOrderEvents(order.id);
        const elapsed = Date.now() - start;
        const remaining = Math.max(MIN_LOADING_MS - elapsed, 0);

        setTimeout(() => {
          if (!active) return;
          setEvents(Array.isArray(data) ? data : []);
          setLoading(false);
        }, remaining);
      } catch (err) {
        const elapsed = Date.now() - start;
        const remaining = Math.max(MIN_LOADING_MS - elapsed, 0);

        setTimeout(() => {
          if (!active) return;
          setEvents([]);
          setError(err.message || "Failed to load order audit events.");
          setLoading(false);
        }, remaining);
      }
    }

    loadEvents();

    return () => {
      active = false;
    };
  }, [open, order?.id]);

  if (!open || !order) return null;

  function handleBackdropClick(event) {
    if (event.target === event.currentTarget) {
      onClose();
    }
  }

  return (
    <div className="modal-backdrop" onClick={handleBackdropClick} role="presentation">
      <div
        className="modal-card details-modal-card details-modal-layout"
        role="dialog"
        aria-modal="true"
        aria-labelledby="order-details-title"
      >
        <div className="details-modal-header">
          <div className="panel-header details-panel-header">
            <h3 id="order-details-title">Order Details</h3>
            <button
              className="modal-close"
              onClick={onClose}
              type="button"
              aria-label="Close modal"
            >
              ×
            </button>
          </div>
        </div>

        <div className="details-modal-body">
          <div className="replace-order-summary">
            <div><strong>Order ID:</strong> {order.id}</div>
            <div><strong>Client Order ID:</strong> {order.clientOrderId || "—"}</div>
            <div><strong>Instrument:</strong> {order.instrument}</div>
            <div><strong>Side:</strong> {order.side}</div>
            <div><strong>Type:</strong> {order.orderType}</div>
            <div><strong>Price:</strong> {formatMoney(order.limitPrice)}</div>
            <div><strong>Original Qty:</strong> {order.originalQty}</div>
            <div><strong>Open Qty:</strong> {order.openQty}</div>
            <div><strong>Filled Qty:</strong> {filledQty}</div>
            <div><strong>Fill %:</strong> {fillPercent}%</div>
            <div><strong>Status:</strong> {order.status}</div>
            <div><strong>Created:</strong> {formatDate(order.createdAt)}</div>
            <div><strong>Updated:</strong> {formatDate(order.updatedAt)}</div>
          </div>

          <div className="section-bar bar-blue">ORDER AUDIT TIMELINE</div>

          <div className="timeline-shell">
            {loading && (
              <div className="timeline-loading">
                <div className="timeline-loading-box">
                  <div className="timeline-loading-spinner" />
                  <span>Loading timeline...</span>
                </div>
              </div>
            )}

            {!loading && error && <div className="error-box">{error}</div>}

            {!loading && !error && (
              <div className="timeline">
                {events.length === 0 ? (
                  <div className="empty-cell timeline-empty-state">No audit events found</div>
                ) : (
                  events.map((event, index) => (
                    <div
                      key={event.id ?? `${event.eventType}-${event.createdAt}-${index}`}
                      className="timeline-item"
                    >
                      <div className="timeline-dot" />
                      <div className="timeline-content">
                        <div className="timeline-top">
                          <span className="status-pill status-new">{event.eventType}</span>
                          <span className="timeline-date">{formatDate(event.createdAt)}</span>
                        </div>
                        <div className="timeline-message">{event.message}</div>
                      </div>
                    </div>
                  ))
                )}
              </div>
            )}
          </div>
        </div>

        <div className="details-modal-footer">
          <div className="modal-actions">
            <button className="secondary-button" onClick={onClose} type="button">
              Close
            </button>
          </div>
        </div>
      </div>
    </div>
  );
}

export default function OrderDetailsModal({ order, open, onClose }) {
  if (!open || !order) return null;

  return createPortal(
    <ModalContent order={order} open={open} onClose={onClose} />,
    document.body
  );
}