import { describe, expect, it, vi, beforeEach } from "vitest";
import { render, screen, waitFor } from "@testing-library/react";
import MarketPage from "./MarketPage";

vi.mock("../lib/api", () => ({
  api: {
    getOrderBook: vi.fn(),
    getTrades: vi.fn(),
    getSystemStatus: vi.fn()
  }
}));

vi.mock("../lib/ws", () => ({
  createMarketClient: vi.fn()
}));

import { api } from "../lib/api";
import { createMarketClient } from "../lib/ws";

describe("MarketPage", () => {
  beforeEach(() => {
    vi.clearAllMocks();

    api.getOrderBook.mockResolvedValue({
      instrument: "AAPL",
      bestBid: 100,
      bestAsk: 101,
      spread: 1,
      bids: [],
      asks: []
    });

    api.getTrades.mockResolvedValue({
      content: []
    });

    api.getSystemStatus.mockResolvedValue({
      overallStatus: "UP",
      kafkaMode: "ENABLED",
      services: {
        gateway: { status: "UP" },
        auth: { status: "UP" },
        trading: { status: "UP" },
        marketData: { status: "UP" }
      }
    });
  });

  it("shows reconnect and fallback information", async () => {
    createMarketClient.mockImplementation(({ onStatus }) => ({
      activate() {
        onStatus("RECONNECTING");
      },
      deactivate() {}
    }));

    render(<MarketPage />);

    await waitFor(() => {
      expect(screen.getByText("Market Dashboard")).toBeInTheDocument();
    });

    expect(screen.getByText("Reconnect Count")).toBeInTheDocument();
    expect(screen.getByText("Fallback Mode")).toBeInTheDocument();
  });

  it("shows polling warning when socket is not live", async () => {
    createMarketClient.mockImplementation(({ onStatus }) => ({
      activate() {
        onStatus("RECONNECTING");
      },
      deactivate() {}
    }));

    render(<MarketPage />);

    await waitFor(() => {
      expect(
        screen.getByText("Live socket is not fully connected. Poll fallback is active.")
      ).toBeInTheDocument();
    });
  });
});