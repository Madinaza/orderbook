import { describe, expect, it, beforeEach, afterEach, vi } from "vitest";
import { isTokenExpired, parseJwt } from "./jwt";

function createToken(payload) {
  const header = btoa(JSON.stringify({ alg: "HS256", typ: "JWT" }))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");

  const body = btoa(JSON.stringify(payload))
    .replace(/\+/g, "-")
    .replace(/\//g, "_")
    .replace(/=+$/, "");

  return `${header}.${body}.signature`;
}

describe("jwt utils", () => {
  beforeEach(() => {
    vi.useFakeTimers();
    vi.setSystemTime(new Date("2026-03-11T12:00:00Z"));
  });

  afterEach(() => {
    vi.useRealTimers();
  });

  it("parseJwt should return payload", () => {
    const token = createToken({ sub: "1", roles: ["ROLE_TRADER"] });
    expect(parseJwt(token)).toEqual({ sub: "1", roles: ["ROLE_TRADER"] });
  });

  it("isTokenExpired should return false for valid token", () => {
    const token = createToken({
      sub: "1",
      exp: Math.floor(new Date("2026-03-11T13:00:00Z").getTime() / 1000)
    });

    expect(isTokenExpired(token)).toBe(false);
  });

  it("isTokenExpired should return true for expired token", () => {
    const token = createToken({
      sub: "1",
      exp: Math.floor(new Date("2026-03-11T11:00:00Z").getTime() / 1000)
    });

    expect(isTokenExpired(token)).toBe(true);
  });

  it("isTokenExpired should return true for invalid token", () => {
    expect(isTokenExpired("bad.token")).toBe(true);
  });
});