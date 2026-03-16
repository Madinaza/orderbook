import { clearToken, getToken } from "./auth";
import { isTokenExpired } from "./jwt";

const API_BASE = "http://localhost:8080";

function buildQuery(params = {}) {
  const search = new URLSearchParams();

  Object.entries(params).forEach(([key, value]) => {
    if (value !== null && value !== undefined && value !== "") {
      search.set(key, value);
    }
  });

  const text = search.toString();
  return text ? `?${text}` : "";
}

async function request(path, options = {}) {
  const token = getToken();

  if (token && isTokenExpired(token)) {
    clearToken();
    sessionStorage.setItem("session_expired_message", "Session expired, please login again.");
    throw new Error("Session expired, please login again.");
  }

  const headers = {
    "Content-Type": "application/json",
    ...(options.headers || {})
  };

  if (token) {
    headers.Authorization = `Bearer ${token}`;
  }

  const response = await fetch(`${API_BASE}${path}`, {
    ...options,
    headers
  });

  const contentType = response.headers.get("content-type") || "";
  const isJson = contentType.includes("application/json");
  const data = isJson ? await response.json() : await response.text();

  if (!response.ok) {
    const message =
      (typeof data === "object" && data?.message) ||
      (typeof data === "string" && data) ||
      `HTTP ${response.status}`;

    if (response.status === 401) {
      clearToken();
      sessionStorage.setItem("session_expired_message", "Session expired, please login again.");
    }

    throw new Error(message);
  }

  return data;
}

export const api = {
  register: (payload) =>
    request("/api/auth/register", {
      method: "POST",
      body: JSON.stringify(payload)
    }),

  login: (payload) =>
    request("/api/auth/login", {
      method: "POST",
      body: JSON.stringify(payload)
    }),

  getMyOrders: (filters = {}) =>
    request(`/api/orders${buildQuery(filters)}`),

  getOrderEvents: (id) =>
    request(`/api/orders/${id}/events`),

  getMyTrades: (filters = {}) =>
    request(`/api/trades/mine${buildQuery(filters)}`),

  getAdminSummary: () =>
    request("/api/admin/summary"),

  getSystemStatus: () =>
    request("/api/system/status"),

  placeOrder: (payload) =>
    request("/api/orders", {
      method: "POST",
      body: JSON.stringify(payload)
    }),

  cancelOrder: (id) =>
    request(`/api/orders/${id}/cancel`, {
      method: "POST"
    }),

  replaceOrder: (id, payload) =>
    request(`/api/orders/${id}`, {
      method: "PUT",
      body: JSON.stringify(payload)
    }),

  getOrderBook: (instrument) =>
    request(`/api/orderbook/${encodeURIComponent(instrument)}`),

  getTrades: (instrument, params = {}) =>
    request(`/api/trades/${encodeURIComponent(instrument)}${buildQuery(params)}`)
};