import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import SystemStatusPanel from "./SystemStatusPanel";

vi.mock("../lib/api", () => ({
  api: {
    getSystemStatus: vi.fn()
  }
}));

import { api } from "../lib/api";

describe("SystemStatusPanel", () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it("renders system status data", async () => {
    api.getSystemStatus.mockResolvedValue({
      overallStatus: "UP",
      kafkaMode: "ENABLED",
      services: {
        gateway: { status: "UP" },
        auth: { status: "UP" },
        trading: { status: "UP" },
        marketData: { status: "DOWN" }
      }
    });

    render(<SystemStatusPanel websocketStatus="LIVE" />);

    await waitFor(() => {
      expect(screen.getByText("System Status")).toBeInTheDocument();
    });

    expect(screen.getByText("Gateway")).toBeInTheDocument();
    expect(screen.getByText("Auth Service")).toBeInTheDocument();
    expect(screen.getByText("Trading Service")).toBeInTheDocument();
    expect(screen.getByText("Market Data")).toBeInTheDocument();
    expect(screen.getByText("WebSocket")).toBeInTheDocument();
    expect(screen.getByText("Kafka Mode")).toBeInTheDocument();
    expect(screen.getByText("LIVE")).toBeInTheDocument();
    expect(screen.getByText("ENABLED")).toBeInTheDocument();
  });

  it("renders error message when request fails", async () => {
    api.getSystemStatus.mockRejectedValue(new Error("Failed to load system status."));

    render(<SystemStatusPanel websocketStatus="OFFLINE" />);

    await waitFor(() => {
      expect(screen.getByText("Failed to load system status.")).toBeInTheDocument();
    });
  });
});