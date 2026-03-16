import { formatDate, formatMoney } from "../lib/format";

export default function TradesPanel({ trades }) {
  return (
    <section className="panel-card fade-up">
      <div className="panel-header">
        <h3>Recent Trades</h3>
        <span className="mini-badge yellow-badge">{trades.length}</span>
      </div>

      <div className="section-bar bar-blue">TRADE HISTORY</div>

      <div className="table-wrap">
        <table className="data-table">
          <thead>
            <tr>
              <th>Buy Order</th>
              <th>Sell Order</th>
              <th>Price</th>
              <th>Qty</th>
              <th>Executed At</th>
            </tr>
          </thead>
          <tbody>
            {trades.length === 0 ? (
              <tr>
                <td colSpan="5" className="empty-cell">
                  No trades yet
                </td>
              </tr>
            ) : (
              trades.map((trade, index) => (
                <tr key={`${trade.buyOrderId}-${trade.sellOrderId}-${index}`}>
                  <td>{trade.buyOrderId}</td>
                  <td>{trade.sellOrderId}</td>
                  <td>{formatMoney(trade.price)}</td>
                  <td>{trade.quantity}</td>
                  <td>{formatDate(trade.executedAt)}</td>
                </tr>
              ))
            )}
          </tbody>
        </table>
      </div>
    </section>
  );
}