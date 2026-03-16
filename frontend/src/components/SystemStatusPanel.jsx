import { useCallback, useState } from "react";
import usePolling from "../hooks/usePolling";
import { api } from "../lib/api";

function normalizeServiceStatus(value) {
  if (typeof value === "string") return value;
  if (value && typeof value === "object" && typeof value.status === "string") {
    return value.status;
  }
  return "UNKNOWN";
}

function StatusBadge({ value }) {
  const normalized = String(value || "").toUpperCase();

  let className = "status-pill status-partial";
  if (normalized === "UP" || normalized === "LIVE" || normalized === "ENABLED") {
    className = "status-pill status-filled";
  } else if (
    normalized === "DOWN" ||
    normalized === "OFFLINE" ||
    normalized === "DISABLED" ||
    normalized === "ERROR"
  ) {
    className = "status-pill status-cancelled";
  }

  return <span className={className}>{normalized || "UNKNOWN"}</span>;
}

export default function SystemStatusPanel({ websocketStatus = "OFFLINE" }) {
  const [status, setStatus] = useState({
    gateway: "UNKNOWN",
    authService: "UNKNOWN",
    tradingService: "UNKNOWN",
    marketDataService: "UNKNOWN",
    kafkaMode: "UNKNOWN"
  });
  const [error, setError] = useState("");

  const loadStatus = useCallback(async () => {
    try {
      setError("");
      const data = await api.getSystemStatus();

      setStatus({
        gateway: normalizeServiceStatus(data.gateway),
        authService: normalizeServiceStatus(data.authService),
        tradingService: normalizeServiceStatus(data.tradingService),
        marketDataService: normalizeServiceStatus(data.marketDataService),
        kafkaMode: data.kafkaMode ?? "UNKNOWN"
      });
    } catch (err) {
      setError(err.message || "Failed to load system status.");
      setStatus({
        gateway: "DOWN",
        authService: "DOWN",
        tradingService: "DOWN",
        marketDataService: "DOWN",
        kafkaMode: "UNKNOWN"
      });
    }
  }, []);

  usePolling(loadStatus, 8000);

  return (
    <section className="panel-card fade-up">
      <div className="panel-header">
        <div>
          <h3>System Status</h3>
          <p className="panel-subtitle">
            Live health view for gateway and backend services.
          </p>
        </div>
        <span className="mini-badge yellow-badge">LIVE</span>
      </div>

      {error && <div className="error-box">{error}</div>}

      <div className="system-status-grid">
        <div className="system-status-item">
          <span>Gateway</span>
          <StatusBadge value={status.gateway} />
        </div>

        <div className="system-status-item">
          <span>Auth Service</span>
          <StatusBadge value={status.authService} />
        </div>

        <div className="system-status-item">
          <span>Trading Service</span>
          <StatusBadge value={status.tradingService} />
        </div>

        <div className="system-status-item">
          <span>Market Data Service</span>
          <StatusBadge value={status.marketDataService} />
        </div>

        <div className="system-status-item">
          <span>WebSocket</span>
          <StatusBadge value={websocketStatus} />
        </div>

        <div className="system-status-item">
          <span>Kafka Mode</span>
          <StatusBadge value={status.kafkaMode} />
        </div>
      </div>
    </section>
  );
}