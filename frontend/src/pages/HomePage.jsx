import { Link } from "react-router-dom";
import StatCard from "../components/StatCard";
import { isAuthenticated } from "../lib/auth";
import { isAdmin } from "../lib/jwt";

export default function HomePage() {
  const loggedIn = isAuthenticated();
  const admin = isAdmin();

  return (
    <div className="dashboard-grid">
      <section className="hero-card-enterprise fade-up">
        <div className="hero-left">
          <div className="hero-chip">REAL-TIME TRADING PLATFORM</div>

          <h2>Execution, market intelligence, and trader operations in one professional workspace</h2>

          <p>
            Submit and manage orders, monitor market depth in real time, review execution activity,
            and access operational dashboards through a secure multi-service trading platform.
          </p>

          <div className="hero-actions">
            <Link className="primary-button inline-button" to="/market">
              Open Market
            </Link>

            {loggedIn ? (
              <Link className="secondary-button inline-button" to="/place-order">
                Create Order
              </Link>
            ) : (
              <Link className="secondary-button inline-button" to="/login">
                Sign In
              </Link>
            )}
          </div>

          <div className="hero-trust-row">
            <div className="hero-trust-item">
              <span className="hero-trust-dot" />
              <span>Secure authentication</span>
            </div>

            <div className="hero-trust-item">
              <span className="hero-trust-dot" />
              <span>Live streaming market updates</span>
            </div>

            <div className="hero-trust-item">
              <span className="hero-trust-dot" />
              <span>Audit-ready order lifecycle</span>
            </div>
          </div>
        </div>

        <div className="hero-right">
          <div className="executive-panel">
            <div className="executive-panel-top">
              <div>
                <div className="executive-panel-kicker">Platform Status</div>
                <div className="executive-panel-title">Trading Workspace</div>
              </div>
              <span className="executive-live-badge">LIVE</span>
            </div>

            <div className="executive-panel-grid">
              <div className="executive-metric-card">
                <small>Access</small>
                <strong>{loggedIn ? "Authenticated Session" : "Guest Access"}</strong>
              </div>

              <div className="executive-metric-card">
                <small>Role</small>
                <strong>{admin ? "Administrator" : loggedIn ? "Trader" : "Public"}</strong>
              </div>

              <div className="executive-metric-card">
                <small>Architecture</small>
                <strong>Gateway + Auth + Trading + Market Data</strong>
              </div>

              <div className="executive-metric-card">
                <small>Streaming</small>
                <strong>Kafka + WebSocket + Poll Fallback</strong>
              </div>
            </div>

            <div className="mini-terminal">
              <div className="mini-terminal-head">
                <span className="terminal-dot dot-red" />
                <span className="terminal-dot dot-yellow" />
                <span className="terminal-dot dot-green" />
                <span className="mini-terminal-title">Live Service Snapshot</span>
              </div>

              <div className="mini-terminal-body">
                <div className="terminal-row">
                  <span className="terminal-label">Gateway</span>
                  <span className="terminal-status terminal-ok">ONLINE</span>
                </div>
                <div className="terminal-row">
                  <span className="terminal-label">Trading Service</span>
                  <span className="terminal-status terminal-ok">ONLINE</span>
                </div>
                <div className="terminal-row">
                  <span className="terminal-label">Market Feed</span>
                  <span className="terminal-status terminal-live">STREAMING</span>
                </div>
                <div className="terminal-row">
                  <span className="terminal-label">Security</span>
                  <span className="terminal-status terminal-ok">JWT ACTIVE</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </section>

      <div className="stats-row stats-row-4">
        <StatCard label="Platform Access" value={loggedIn ? "Authenticated" : "Guest"} accent="blue" compact />
        <StatCard label="Account Role" value={admin ? "Administrator" : loggedIn ? "Trader" : "Public"} accent="yellow" compact />
        <StatCard label="Infrastructure" value="Microservices" accent="blue" compact />
        <StatCard label="Market Feed" value="Live Streaming" accent="yellow" compact />
      </div>

      <section className="panel-card fade-up">
        <div className="panel-header">
          <div>
            <h3>Core Platform Capabilities</h3>
            <p className="panel-subtitle">
              The primary workflows available across the platform.
            </p>
          </div>
          <span className="mini-badge">OVERVIEW</span>
        </div>

        <div className="feature-grid-pro">
          <div className="feature-card-pro">
            <div className="feature-icon">01</div>
            <h4>Order Entry</h4>
            <p>Create market and limit orders through authenticated execution requests.</p>
          </div>

          <div className="feature-card-pro">
            <div className="feature-icon">02</div>
            <h4>Order Management</h4>
            <p>Track live status, inspect audit history, and perform replace or cancel actions.</p>
          </div>

          <div className="feature-card-pro">
            <div className="feature-icon">03</div>
            <h4>Market Monitoring</h4>
            <p>Review order book depth, spread changes, and recent executions by instrument.</p>
          </div>

          <div className="feature-card-pro">
            <div className="feature-icon">04</div>
            <h4>Administrative Oversight</h4>
            <p>Access operational summaries and management views through restricted access.</p>
          </div>
        </div>
      </section>

      <section className="panel-card fade-up">
        <div className="panel-header">
          <div>
            <h3>Platform Value</h3>
            <p className="panel-subtitle">
              Designed for trader workflows and operational visibility.
            </p>
          </div>
          <span className="mini-badge yellow-badge">ENTERPRISE</span>
        </div>

        <div className="value-strip">
          <div className="value-box">
            <h4>Real-time visibility</h4>
            <p>Live order book and trade activity are delivered through a streaming-ready architecture.</p>
          </div>

          <div className="value-box">
            <h4>Operational reliability</h4>
            <p>Separated services improve maintainability, testing, deployment flexibility, and resilience.</p>
          </div>

          <div className="value-box">
            <h4>Controlled access</h4>
            <p>JWT-based authentication and role-aware navigation protect critical trading and admin operations.</p>
          </div>
        </div>
      </section>
    </div>
  );
}