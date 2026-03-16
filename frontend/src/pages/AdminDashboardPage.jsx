import { useCallback, useMemo, useState } from "react";
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend
} from "recharts";
import usePolling from "../hooks/usePolling";
import { api } from "../lib/api";
import StatCard from "../components/StatCard";

function safeNumber(value) {
  const n = Number(value);
  return Number.isFinite(n) ? n : 0;
}

function EmptyChartState({ title, subtitle }) {
  return (
    <div className="chart-empty-state">
      <strong>{title}</strong>
      <span>{subtitle}</span>
    </div>
  );
}

export default function AdminDashboardPage() {
  const [summary, setSummary] = useState({
    totalOrders: 0,
    openOrders: 0,
    filledOrders: 0,
    cancelledOrders: 0,
    totalTrades: 0,
    activeInstruments: 0,
    buySellRatio: 0,
    filledRatePercent: 0,
    cancelRatePercent: 0,
    instruments: [],
    top5ActiveInstruments: [],
    top5TradedInstruments: []
  });

  const [error, setError] = useState("");

  const loadSummary = useCallback(async () => {
    try {
      setError("");
      const data = await api.getAdminSummary();

      setSummary({
        totalOrders: data.totalOrders ?? 0,
        openOrders: data.openOrders ?? 0,
        filledOrders: data.filledOrders ?? 0,
        cancelledOrders: data.cancelledOrders ?? 0,
        totalTrades: data.totalTrades ?? 0,
        activeInstruments: data.activeInstruments ?? 0,
        buySellRatio: data.buySellRatio ?? 0,
        filledRatePercent: data.filledRatePercent ?? 0,
        cancelRatePercent: data.cancelRatePercent ?? 0,
        instruments: Array.isArray(data.instruments) ? data.instruments : [],
        top5ActiveInstruments: Array.isArray(data.top5ActiveInstruments) ? data.top5ActiveInstruments : [],
        top5TradedInstruments: Array.isArray(data.top5TradedInstruments) ? data.top5TradedInstruments : []
      });
    } catch (err) {
      setError(err.message || "Failed to load admin summary.");
    }
  }, []);

  usePolling(loadSummary, 5000);

  const orderTradeChartData = useMemo(() => {
    return summary.instruments.slice(0, 10).map((row) => ({
      instrument: row.instrument,
      orders: safeNumber(row.orders),
      trades: safeNumber(row.trades),
      openQuantity: safeNumber(row.openQuantity)
    }));
  }, [summary.instruments]);

  const topActiveChartData = useMemo(() => {
    return summary.top5ActiveInstruments.map((row) => ({
      instrument: row.instrument,
      openQuantity: safeNumber(row.openQuantity)
    }));
  }, [summary.top5ActiveInstruments]);

  const topTradedChartData = useMemo(() => {
    return summary.top5TradedInstruments.map((row) => ({
      instrument: row.instrument,
      trades: safeNumber(row.trades)
    }));
  }, [summary.top5TradedInstruments]);

  return (
    <div className="dashboard-grid">
      <section className="panel-card fade-up">
        <div className="panel-header">
          <div>
            <h2>Admin Dashboard</h2>
            <p className="panel-subtitle">
              Operational visibility for orders, trades, and instrument activity.
            </p>
          </div>
          <span className="mini-badge yellow-badge">LIVE SUMMARY</span>
        </div>

        {error && <div className="error-box">{error}</div>}
      </section>

      <div className="stats-row stats-row-3">
        <StatCard label="Total Orders" value={summary.totalOrders} accent="blue" />
        <StatCard label="Open Orders" value={summary.openOrders} accent="yellow" />
        <StatCard label="Filled Orders" value={summary.filledOrders} accent="blue" />
      </div>

      <div className="stats-row stats-row-3">
        <StatCard label="Cancelled Orders" value={summary.cancelledOrders} accent="yellow" />
        <StatCard label="Total Trades" value={summary.totalTrades} accent="blue" />
        <StatCard label="Active Instruments" value={summary.activeInstruments} accent="yellow" />
      </div>

      <div className="stats-row stats-row-3">
        <StatCard label="Buy / Sell Ratio" value={summary.buySellRatio} accent="blue" />
        <StatCard label="Filled Rate %" value={summary.filledRatePercent} accent="yellow" />
        <StatCard label="Cancel Rate %" value={summary.cancelRatePercent} accent="blue" />
      </div>

      <section className="panel-card fade-up">
        <div className="panel-header">
          <div>
            <h3>Orders vs Trades by Instrument</h3>
            <p className="panel-subtitle">
              First 10 instruments ranked by returned summary set.
            </p>
          </div>
          <span className="mini-badge">{orderTradeChartData.length}</span>
        </div>

        <div className="chart-card">
          {orderTradeChartData.length === 0 ? (
            <EmptyChartState
              title="No chart data"
              subtitle="No instrument summary data is available yet."
            />
          ) : (
            <ResponsiveContainer width="100%" height={340}>
              <BarChart data={orderTradeChartData}>
                <CartesianGrid strokeDasharray="3 3" />
                <XAxis dataKey="instrument" />
                <YAxis />
                <Tooltip />
                <Legend />
                <Bar dataKey="orders" name="Orders" />
                <Bar dataKey="trades" name="Trades" />
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </section>

      <div className="admin-chart-grid">
        <section className="panel-card fade-up">
          <div className="panel-header">
            <div>
              <h3>Top 5 Active Instruments</h3>
              <p className="panel-subtitle">
                Ranked by open quantity still resting in the book.
              </p>
            </div>
            <span className="mini-badge yellow-badge">OPEN QTY</span>
          </div>

          <div className="chart-card">
            {topActiveChartData.length === 0 ? (
              <EmptyChartState
                title="No chart data"
                subtitle="No active open-quantity data is available yet."
              />
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={topActiveChartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="instrument" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="openQuantity" name="Open Quantity" />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </section>

        <section className="panel-card fade-up">
          <div className="panel-header">
            <div>
              <h3>Top 5 Traded Instruments</h3>
              <p className="panel-subtitle">
                Ranked by number of completed trades.
              </p>
            </div>
            <span className="mini-badge">TRADES</span>
          </div>

          <div className="chart-card">
            {topTradedChartData.length === 0 ? (
              <EmptyChartState
                title="No chart data"
                subtitle="No traded instrument data is available yet."
              />
            ) : (
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={topTradedChartData}>
                  <CartesianGrid strokeDasharray="3 3" />
                  <XAxis dataKey="instrument" />
                  <YAxis />
                  <Tooltip />
                  <Bar dataKey="trades" name="Trades" />
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </section>
      </div>

      <section className="panel-card fade-up">
        <div className="panel-header">
          <div>
            <h3>Instrument Activity Table</h3>
            <p className="panel-subtitle">
              Orders, trades, and open quantity by instrument.
            </p>
          </div>
          <span className="mini-badge">{summary.instruments.length}</span>
        </div>

        <div className="section-bar bar-blue">ORDERS / TRADES / OPEN QUANTITY</div>

        <div className="table-wrap admin-table-wrap">
          <table className="data-table admin-data-table">
            <thead>
              <tr>
                <th>Instrument</th>
                <th>Orders</th>
                <th>Trades</th>
                <th>Open Quantity</th>
              </tr>
            </thead>
            <tbody>
              {summary.instruments.length === 0 ? (
                <tr>
                  <td colSpan="4" className="empty-cell">
                    No summary data available
                  </td>
                </tr>
              ) : (
                summary.instruments.map((row) => (
                  <tr key={row.instrument}>
                    <td>{row.instrument}</td>
                    <td>{row.orders}</td>
                    <td>{row.trades}</td>
                    <td>{row.openQuantity}</td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </section>

      <div className="admin-top-grid">
        <section className="panel-card fade-up">
          <div className="panel-header">
            <div>
              <h3>Top 5 Active Instruments</h3>
              <p className="panel-subtitle">
                Operational ranking for open quantity concentration.
              </p>
            </div>
            <span className="mini-badge yellow-badge">RANKING</span>
          </div>

          <div className="table-wrap admin-table-wrap">
            <table className="data-table admin-data-table admin-compact-table">
              <thead>
                <tr>
                  <th>Instrument</th>
                  <th>Open Quantity</th>
                  <th>Orders</th>
                  <th>Trades</th>
                </tr>
              </thead>
              <tbody>
                {summary.top5ActiveInstruments.length === 0 ? (
                  <tr>
                    <td colSpan="4" className="empty-cell">No active instruments</td>
                  </tr>
                ) : (
                  summary.top5ActiveInstruments.map((row) => (
                    <tr key={row.instrument}>
                      <td>{row.instrument}</td>
                      <td>{row.openQuantity}</td>
                      <td>{row.orders}</td>
                      <td>{row.trades}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>

        <section className="panel-card fade-up">
          <div className="panel-header">
            <div>
              <h3>Top 5 Traded Instruments</h3>
              <p className="panel-subtitle">
                Trading concentration leaderboard based on executions.
              </p>
            </div>
            <span className="mini-badge">RANKING</span>
          </div>

          <div className="table-wrap admin-table-wrap">
            <table className="data-table admin-data-table admin-compact-table">
              <thead>
                <tr>
                  <th>Instrument</th>
                  <th>Trades</th>
                  <th>Orders</th>
                  <th>Open Quantity</th>
                </tr>
              </thead>
              <tbody>
                {summary.top5TradedInstruments.length === 0 ? (
                  <tr>
                    <td colSpan="4" className="empty-cell">No traded instruments</td>
                  </tr>
                ) : (
                  summary.top5TradedInstruments.map((row) => (
                    <tr key={row.instrument}>
                      <td>{row.instrument}</td>
                      <td>{row.trades}</td>
                      <td>{row.orders}</td>
                      <td>{row.openQuantity}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </section>
      </div>
    </div>
  );
}