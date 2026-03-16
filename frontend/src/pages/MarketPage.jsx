import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { api } from "../lib/api";
import { createMarketClient } from "../lib/ws";
import OrderBookPanel from "../components/OrderBookPanel";
import TradesPanel from "../components/TradesPanel";
import StatCard from "../components/StatCard";
import SystemStatusPanel from "../components/SystemStatusPanel";
import { formatDate, formatMoney } from "../lib/format";

function normalizeTradeRows(value) {
  if (Array.isArray(value)) return value;
  if (value && Array.isArray(value.content)) return value.content;
  return [];
}

export default function MarketPage() {
  const [instrumentInput, setInstrumentInput] = useState("AAPL");
  const [instrument, setInstrument] = useState("AAPL");
  const [snapshot, setSnapshot] = useState({
    instrument: "AAPL",
    bestBid: null,
    bestAsk: null,
    spread: null,
    bids: [],
    asks: []
  });
  const [trades, setTrades] = useState([]);
  const [error, setError] = useState("");
  const [connectionStatus, setConnectionStatus] = useState("CONNECTING");
  const [lastUpdatedAt, setLastUpdatedAt] = useState(null);
  const [reconnectCount, setReconnectCount] = useState(0);
  const [fallbackMode, setFallbackMode] = useState("SOCKET");

  const previousStatusRef = useRef("CONNECTING");

  const loadMarketSnapshot = useCallback(async (selectedInstrument) => {
    try {
      const upper = String(selectedInstrument || "").trim().toUpperCase();
      if (!upper) return;

      const [book, tradeData] = await Promise.all([
        api.getOrderBook(upper),
        api.getTrades(upper, { page: 0, size: 20, sortBy: "executedAt", sortDirection: "desc" })
      ]);

      setSnapshot(book);
      setTrades(normalizeTradeRows(tradeData));
      setError("");
      setLastUpdatedAt(new Date().toISOString());
    } catch (err) {
      setError(err.message || "Failed to load market.");
    }
  }, []);

  useEffect(() => {
    let client = null;
    let fallbackInterval = null;
    let active = true;

    async function initialize() {
      await loadMarketSnapshot(instrument);
      if (!active) return;

      client = createMarketClient({
        instrument,
        onOrderBook: (book) => {
          setSnapshot(book);
          setError("");
          setLastUpdatedAt(new Date().toISOString());
          setFallbackMode("SOCKET");
        },
        onTrades: (tradeRows) => {
          setTrades(normalizeTradeRows(tradeRows));
          setError("");
          setLastUpdatedAt(new Date().toISOString());
        },
        onStatus: (status) => {
          const previous = previousStatusRef.current;
          if ((status === "RECONNECTING" || status === "CONNECTING") && previous !== status) {
            setReconnectCount((count) => count + 1);
          }
          previousStatusRef.current = status;
          setConnectionStatus(status);
        }
      });

      client.activate();

      fallbackInterval = setInterval(() => {
        if (previousStatusRef.current !== "LIVE") {
          setFallbackMode("POLL");
          loadMarketSnapshot(instrument);
        }
      }, 5000);
    }

    initialize();

    return () => {
      active = false;
      if (fallbackInterval) clearInterval(fallbackInterval);
      if (client) client.deactivate();
    };
  }, [instrument, loadMarketSnapshot]);

  function applyInstrument() {
    const normalized = instrumentInput.trim().toUpperCase();
    if (!normalized) return;
    setInstrument(normalized);
  }

  const tradeCount = trades.length;
  const topBid = useMemo(() => formatMoney(snapshot.bestBid), [snapshot.bestBid]);
  const topAsk = useMemo(() => formatMoney(snapshot.bestAsk), [snapshot.bestAsk]);
  const spread = useMemo(() => formatMoney(snapshot.spread), [snapshot.spread]);

  return (
    <div className="dashboard-grid">
      <SystemStatusPanel websocketStatus={connectionStatus} />

      <section className="panel-card fade-up">
        <div className="panel-header">
          <h2>Market Dashboard</h2>
          <span className="mini-badge yellow-badge">{connectionStatus}</span>
        </div>

        <div className="market-toolbar">
          <label>
            <span>Instrument</span>
            <input
              value={instrumentInput}
              onChange={(e) => setInstrumentInput(e.target.value.toUpperCase())}
              placeholder="AAPL"
            />
          </label>

          <button className="primary-button toolbar-button" onClick={applyInstrument}>
            Load Instrument
          </button>
        </div>

        {connectionStatus !== "LIVE" && (
          <div className="info-box">
            Live socket is not fully connected. Poll fallback is active.
          </div>
        )}

        {error && <div className="error-box">{error}</div>}
      </section>

      <div className="stats-row stats-row-3">
        <StatCard label="Instrument" value={instrument} accent="blue" />
        <StatCard label="Best Bid" value={topBid} accent="yellow" />
        <StatCard label="Best Ask" value={topAsk} accent="blue" />
      </div>

      <div className="stats-row stats-row-3">
        <StatCard label="Reconnect Count" value={reconnectCount} accent="yellow" />
        <StatCard label="Fallback Mode" value={fallbackMode} accent="blue" />
        <StatCard label="Last Updated" value={formatDate(lastUpdatedAt)} accent="yellow" />
      </div>

      <div className="stats-row stats-row-3">
        <StatCard label="Trade Count" value={tradeCount} accent="yellow" />
        <StatCard label="Spread" value={spread} accent="blue" />
        <StatCard label="Feed Mode" value="WebSocket + Poll Fallback" accent="yellow" />
      </div>

      <OrderBookPanel snapshot={snapshot} />
      <TradesPanel trades={trades} />
    </div>
  );
}