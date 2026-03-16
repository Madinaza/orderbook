import { useEffect, useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { api } from "../lib/api";
import { setToken } from "../lib/auth";

export default function LoginPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    password: ""
  });
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    const expiredMessage = sessionStorage.getItem("session_expired_message");
    if (expiredMessage) {
      setError(expiredMessage);
      sessionStorage.removeItem("session_expired_message");
    }
  }, []);

  function updateField(event) {
    const { name, value } = event.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  }

  async function submit(event) {
    event.preventDefault();
    setError("");

    const username = form.username.trim();
    const password = form.password;

    if (username.length < 3) {
      setError("Username must be at least 3 characters.");
      return;
    }

    if (password.length < 6) {
      setError("Password must be at least 6 characters.");
      return;
    }

    setLoading(true);

    try {
      const response = await api.login({
        username,
        password
      });

      setToken(response.token);
      navigate("/orders");
    } catch (err) {
      setError(err.message || "Login failed.");
    } finally {
      setLoading(false);
    }
  }

  return (
    <section className="auth-card fade-up">
      <div className="auth-head">
        <h2>Login</h2>
        <span className="coin-dot" />
      </div>

      <form className="auth-form" onSubmit={submit}>
        <label>
          <span>Username</span>
          <input
            name="username"
            value={form.username}
            onChange={updateField}
            autoComplete="username"
          />
        </label>

        <label>
          <span>Password</span>
          <input
            name="password"
            type="password"
            value={form.password}
            onChange={updateField}
            autoComplete="current-password"
          />
        </label>

        {error && <div className="error-box">{error}</div>}

        <button className="primary-button" disabled={loading}>
          {loading ? "Signing in..." : "Login"}
        </button>

        <p className="muted-line">
          No account? <Link to="/register">Create one</Link>
        </p>
      </form>
    </section>
  );
}