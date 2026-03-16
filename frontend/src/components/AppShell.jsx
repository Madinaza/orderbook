import { NavLink, useNavigate } from "react-router-dom";
import { clearToken, isAuthenticated } from "../lib/auth";
import { isAdmin, isTokenExpired } from "../lib/jwt";

export default function AppShell({ children }) {
  const navigate = useNavigate();

  const expired = isAuthenticated() && isTokenExpired();
  if (expired) {
    clearToken();
  }

  const loggedIn = isAuthenticated() && !expired;
  const admin = loggedIn && isAdmin();

  function logout() {
    clearToken();
    navigate("/login");
  }

  return (
    <div className="shell-bg">
      <div className="floating-orb orb-1" />
      <div className="floating-orb orb-2" />
      <div className="floating-orb orb-3" />

      <div className="shell-container">
        <header className="topbar-card">
          <div className="brand-block">
            <div className="brand-mark">
              <span className="brand-coin" />
            </div>

            <div className="brand-copy">
              <h1 className="brand-title">ORDERBOOK</h1>
              <p className="brand-subtitle">
                Multi-service trading platform for order execution, market visibility, and trader activity management.
              </p>
            </div>
          </div>

          <nav className="nav-links">
            <NavLink to="/">Overview</NavLink>
            <NavLink to="/market">Market</NavLink>
            {loggedIn && <NavLink to="/orders">Orders</NavLink>}
            {loggedIn && <NavLink to="/my-trades">Trades</NavLink>}
            {loggedIn && <NavLink to="/place-order">New Order</NavLink>}
            {loggedIn && admin && <NavLink to="/admin">Admin</NavLink>}
            {loggedIn ? (
              <button className="nav-logout" onClick={logout}>
                Sign Out
              </button>
            ) : (
              <>
                <NavLink to="/login">Login</NavLink>
                <NavLink to="/register">Register</NavLink>
              </>
            )}
          </nav>
        </header>

        <main className="page-content">{children}</main>
      </div>
    </div>
  );
}