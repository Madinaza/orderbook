import { useCallback, useMemo, useState } from "react";
import usePolling from "../hooks/usePolling";
import { api } from "../lib/api";
import { formatDate, formatMoney } from "../lib/format";
import StatCard from "../components/StatCard";
import PaginationBar from "../components/PaginationBar";

const DEFAULT_FILTERS = {
  instrument: "",
  side: "ALL",
  from: "",
  to: "",
  page: 0,
  size: 10,
  sortBy: "executedAt",
  sortDirection: "desc"
};

function downloadCsv(filename, rows) {
  const csv = rows
    .map((row) =>
      row.map((value) => `"${String(value ?? "").replaceAll('"', '""')}"`).join(",")
    )
    .join("\n");

  const blob = new Blob([csv], { type: "text/csv;charset=utf-8;" });
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.setAttribute("download", filename);
  document.body.appendChild(link);
  link.click();
  link.remove();
  URL.revokeObjectURL(url);
}

export default function MyTradesPage() {
  const [draftFilters, setDraftFilters] = useState(DEFAULT_FILTERS);
  const [appliedFilters, setAppliedFilters] = useState(DEFAULT_FILTERS);
  const [tradePage, setTradePage] = useState({
    content: [],
    page: 0,
    size: 10,
    totalElements: 0,
    totalPages: 0,
    first: true,
    last: true,
    sortBy: "executedAt",
    sortDirection: "desc"
  });
  const [error, setError] = useState("");

  const trades = tradePage.content || [];

  const loadTrades = useCallback(async () => {
    try {
      setError("");
      const data = await api.getMyTrades(appliedFilters);
      setTradePage(data);
    } catch (err) {
      setError(err.message || "Failed to load trades.");
    }
  }, [appliedFilters]);

  usePolling(loadTrades, 5000);

  function updateFilter(event) {
    const { name, value } = event.target;
    setDraftFilters((prev) => ({ ...prev, [name]: value }));
  }

  function applyFilters() {
    setAppliedFilters({
      instrument: draftFilters.instrument.trim().toUpperCase(),
      side: draftFilters.side === "ALL" ? "" : draftFilters.side,
      from: draftFilters.from,
      to: draftFilters.to,
      page: 0,
      size: draftFilters.size,
      sortBy: draftFilters.sortBy,
      sortDirection: draftFilters.sortDirection
    });
  }

  function resetFilters() {
    setDraftFilters(DEFAULT_FILTERS);
    setAppliedFilters(DEFAULT_FILTERS);
  }

  function changePage(nextPage) {
    setAppliedFilters((prev) => ({
      ...prev,
      page: nextPage
    }));
  }

  function changePageSize(nextSize) {
    setDraftFilters((prev) => ({ ...prev, size: nextSize }));
    setAppliedFilters((prev) => ({
      ...prev,
      size: nextSize,
      page: 0
    }));
  }

  function exportCsv() {
    const rows = [
      ["Trade ID", "Instrument", "My Side", "Buy Order", "Sell Order", "Price", "Quantity", "Executed At"],
      ...trades.map((trade) => [
        trade.id,
        trade.instrument,
        trade.mySide || "",
        trade.buyOrderId,
        trade.sellOrderId,
        trade.price,
        trade.quantity,
        trade.executedAt
      ])
    ];

    downloadCsv("my-trades.csv", rows);
  }

  const totalQuantity = useMemo(
    () => trades.reduce((sum, trade) => sum + Number(trade.quantity || 0), 0),
    [trades]
  );

  const totalNotional = useMemo(
    () => trades.reduce((sum, trade) => sum + Number(trade.price || 0) * Number(trade.quantity || 0), 0),
    [trades]
  );

  const buyCount = useMemo(
    () => trades.filter((trade) => trade.mySide === "BUY").length,
    [trades]
  );

  const sellCount = useMemo(
    () => trades.filter((trade) => trade.mySide === "SELL").length,
    [trades]
  );

  return (
    <div className="dashboard-grid">
      <section className="panel-card fade-up">
        <div className="panel-header">
          <h2>My Trades</h2>
          <span className="mini-badge yellow-badge">{tradePage.totalElements}</span>
        </div>

        <div className="filters-grid">
          <label>
            <span>Instrument</span>
            <input
              name="instrument"
              value={draftFilters.instrument}
              onChange={updateFilter}
              placeholder="AAPL"
            />
          </label>

          <label>
            <span>My Side</span>
            <select name="side" value={draftFilters.side} onChange={updateFilter}>
              <option value="ALL">All</option>
              <option value="BUY">BUY</option>
              <option value="SELL">SELL</option>
            </select>
          </label>

          <label>
            <span>From Date</span>
            <input name="from" type="date" value={draftFilters.from} onChange={updateFilter} />
          </label>

          <label>
            <span>To Date</span>
            <input name="to" type="date" value={draftFilters.to} onChange={updateFilter} />
          </label>

          <label>
            <span>Sort By</span>
            <select name="sortBy" value={draftFilters.sortBy} onChange={updateFilter}>
              <option value="executedAt">Executed Time</option>
              <option value="price">Price</option>
              <option value="quantity">Quantity</option>
              <option value="instrument">Instrument</option>
            </select>
          </label>

          <label>
            <span>Direction</span>
            <select name="sortDirection" value={draftFilters.sortDirection} onChange={updateFilter}>
              <option value="desc">Descending</option>
              <option value="asc">Ascending</option>
            </select>
          </label>
        </div>

        <div className="toolbar-actions">
          <button className="primary-button toolbar-button" onClick={applyFilters}>Apply Filters</button>
          <button className="secondary-button toolbar-button" onClick={resetFilters}>Reset</button>
          <button className="secondary-button toolbar-button" onClick={exportCsv}>Export CSV</button>
        </div>

        {error && <div className="error-box">{error}</div>}
      </section>

      <div className="stats-row stats-row-3">
        <StatCard label="Trades" value={tradePage.totalElements} accent="blue" />
        <StatCard label="Buy Trades" value={buyCount} accent="yellow" />
        <StatCard label="Sell Trades" value={sellCount} accent="blue" />
      </div>

      <div className="stats-row stats-row-3">
        <StatCard label="Page Quantity" value={totalQuantity} accent="yellow" />
        <StatCard label="Page Notional" value={formatMoney(totalNotional)} accent="blue" />
        <StatCard label="Live Refresh" value="5 sec" accent="yellow" />
      </div>

      <section className="panel-card fade-up">
        <div className="panel-header">
          <h3>Executed Trade History</h3>
          <span className="mini-badge">{appliedFilters.instrument || "ALL"}</span>
        </div>

        <div className="section-bar bar-blue">FILTERED TRADE HISTORY</div>

        <PaginationBar
          page={tradePage.page}
          totalPages={tradePage.totalPages}
          totalElements={tradePage.totalElements}
          pageSize={tradePage.size}
          onPrevious={() => changePage(Math.max(tradePage.page - 1, 0))}
          onNext={() => changePage(tradePage.page + 1)}
          onPageSizeChange={changePageSize}
        />

        <div className="table-wrap">
          <table className="data-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Instrument</th>
                <th>My Side</th>
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
                  <td colSpan="8" className="empty-cell">No trades found for the selected filters</td>
                </tr>
              ) : (
                trades.map((trade) => (
                  <tr key={trade.id}>
                    <td>{trade.id}</td>
                    <td>{trade.instrument}</td>
                    <td>
                      <span className={`status-pill ${trade.mySide === "BUY" ? "status-new" : "status-partial"}`}>
                        {trade.mySide || "—"}
                      </span>
                    </td>
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
    </div>
  );
}