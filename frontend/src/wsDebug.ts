import { Client } from "@stomp/stompjs"

const WS_URL = import.meta.env.VITE_WS_URL || "ws://localhost:8080/ws"
const WS_DEBUG = import.meta.env.VITE_WS_DEBUG === "true" || import.meta.env.DEV
const GLOBAL_KEY = "__ws_debug_started__"

export function startWsDebug() {
  if (!WS_DEBUG) return
  const g = globalThis as any
  if (g[GLOBAL_KEY]) return
  g[GLOBAL_KEY] = true

  const client = new Client({
    brokerURL: WS_URL,
    reconnectDelay: 5000,
    debug: (msg) => console.debug("[ws]", msg),
  })

  client.onConnect = () => {
    console.info("[ws] connected", WS_URL)
    client.subscribe("/topic/tables", (message) => {
      const body = parseBody(message.body)
      console.info("[ws] tables", body)
    })
    client.subscribe("/topic/invoices", (message) => {
      const body = parseBody(message.body)
      console.info("[ws] invoices", body)
    })
  }

  client.onStompError = (frame) => {
    console.error("[ws] broker error", frame.headers["message"], frame.body)
  }

  client.onWebSocketClose = (evt) => {
    console.warn("[ws] closed", evt.reason || "closed")
  }

  client.activate()
}

function parseBody(body: string) {
  try {
    return JSON.parse(body)
  } catch {
    return body
  }
}
