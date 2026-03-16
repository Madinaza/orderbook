import { formatMoney } from "../lib/format";

function SideTable({ title, rows, side }) {
  return (
    <div className="book-side">
      <div className={`section-bar ${side === "BUY" ? "bar-yellow" : "bar-blue"}`}>
        {title}
      </div>

      <div className="table-wrap">
        <table className="data-table book-table">
          <thead>
            <tr>
              <th>Price</th>
              <th>Qty</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {rows.length === 0 ? (
              <tr>
                <td colSpan="3" className="empty-cell">
                  No orders
                </td>
              </tr>
            ) : (
              rows.map((row) => (
                <tr key={row.id}>
                  <td>{formatMoney(row.limitPrice)}</td>
                  <td>{row.openQty}</td>
                  <td>{row.status}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
}

export default function OrderBookPanel({ snapshot }) {
  const instrument = snapshot?.instrument || "—";
  const bestBid = formatMoney(snapshot?.bestBid);
  const bestAsk = formatMoney(snapshot?.bestAsk);
  const spread = formatMoney(snapshot?.spread);

  return (
    <section className="panel-card fade-up">
      <div className="panel-header">
        <h3>Order Book</h3>
        <span className="mini-badge">{instrument}</span>
      </div>

      <div className="orderbook-summary-grid">
        <div className="orderbook-summary-card">
          <span>Best Bid</span>
          <strong>{bestBid}</strong>
        </div>

        <div className="orderbook-summary-card">
          <span>Best Ask</span>
          <strong>{bestAsk}</strong>
        </div>

        <div className="orderbook-summary-card">
          <span>Spread</span>
          <strong>{spread}</strong>
        </div>
      </div>

      <div className="book-grid">
        <SideTable title="BUY SIDE" rows={snapshot?.bids || []} side="BUY" />
        <SideTable title="SELL SIDE" rows={snapshot?.asks || []} side="SELL" />
      </div>
    </section>
  );
}