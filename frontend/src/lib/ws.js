import { Client } from "@stomp/stompjs";

const RAW_WS_BASE = import.meta.env.VITE_WS_BASE_URL || "ws://localhost:8083/ws";
const WS_BASE = RAW_WS_BASE.replace(/\/$/, "");

function normalizeInstrument(instrument) {
  return String(instrument || "").trim().toUpperCase();
}

function safeParseJson(body) {
  try {
    return JSON.parse(body);
  } catch (error) {
    console.error("Failed to parse websocket message body:", error, body);
    return null;
  }
}

function noop() {}

export function createMarketClient({
  instrument,
  onOrderBook = noop,
  onTrades = noop,
  onStatus = noop
}) {
  const upperInstrument = normalizeInstrument(instrument);

  if (!upperInstrument) {
    throw new Error("Instrument is required to create a market client.");
  }

  let orderBookSubscription = null;
  let tradesSubscription = null;

  const client = new Client({
    brokerURL: WS_BASE,
    reconnectDelay: 5000,
    heartbeatIncoming: 10000,
    heartbeatOutgoing: 10000,
    connectionTimeout: 10000,
    debug: () => {},

    beforeConnect: async () => {
      onStatus("CONNECTING");
    },

    onConnect: () => {
      onStatus("LIVE");

      if (orderBookSubscription) {
        orderBookSubscription.unsubscribe();
        orderBookSubscription = null;
      }

      if (tradesSubscription) {
        tradesSubscription.unsubscribe();
        tradesSubscription = null;
      }

      orderBookSubscription = client.subscribe(`/topic/orderbook/${upperInstrument}`, (message) => {
        const payload = safeParseJson(message.body);
        if (payload) {
          onOrderBook(payload);
        }
      });

      tradesSubscription = client.subscribe(`/topic/trades/${upperInstrument}`, (message) => {
        const payload = safeParseJson(message.body);
        if (payload) {
          onTrades(payload);
        }
      });
    },

    onStompError: (frame) => {
      console.error(
        "STOMP broker error:",
        frame.headers?.message || "Unknown broker error",
        frame.body || ""
      );
      onStatus("ERROR");
    },

    onDisconnect: () => {
      onStatus("DISCONNECTED");
    },

    onWebSocketClose: () => {
      onStatus("RECONNECTING");
    },

    onWebSocketError: (event) => {
      console.error("WebSocket transport error:", event);
      onStatus("ERROR");
    }
  });

  function disconnect() {
    try {
      if (orderBookSubscription) {
        orderBookSubscription.unsubscribe();
        orderBookSubscription = null;
      }

      if (tradesSubscription) {
        tradesSubscription.unsubscribe();
        tradesSubscription = null;
      }

      if (client.active) {
        client.deactivate();
      }
    } catch (error) {
      console.error("Failed to disconnect market client cleanly:", error);
    }
  }

  return {
    client,
    activate: () => client.activate(),
    deactivate: disconnect,
    isConnected: () => client.connected
  };
}