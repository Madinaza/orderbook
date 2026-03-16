import { clearToken, getToken } from "./auth";

function base64UrlDecode(value) {
  const normalized = value.replace(/-/g, "+").replace(/_/g, "/");
  const padded = normalized.padEnd(
    normalized.length + ((4 - (normalized.length % 4)) % 4),
    "="
  );
  return atob(padded);
}

export function parseJwt(token) {
  try {
    if (!token) return null;

    const parts = token.split(".");
    if (parts.length !== 3) return null;

    return JSON.parse(base64UrlDecode(parts[1]));
  } catch {
    return null;
  }
}

export function getJwtPayload() {
  return parseJwt(getToken());
}

export function getRoles() {
  const payload = getJwtPayload();
  const roles = payload?.roles;
  return Array.isArray(roles) ? roles.map(String) : [];
}

export function hasRole(role) {
  return getRoles().includes(role);
}

export function isAdmin() {
  return hasRole("ROLE_ADMIN");
}

export function isTokenExpired(token = getToken()) {
  const payload = parseJwt(token);
  if (!payload?.exp) return true;

  const nowInSeconds = Math.floor(Date.now() / 1000);
  return payload.exp <= nowInSeconds;
}

export function ensureValidSession() {
  const token = getToken();
  if (!token) return false;

  if (isTokenExpired(token)) {
    clearToken();
    sessionStorage.setItem("session_expired_message", "Session expired, please login again.");
    return false;
  }

  return true;
}