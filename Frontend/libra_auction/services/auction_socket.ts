type Callback = (message: unknown) => void;

type SubscriptionEntry = {
  destination: string;
  callback: Callback;
  id: string;
};

class AuctionSocket {
  private socket: WebSocket | null = null;
  private connected = false;
  private connecting = false;
  private subscriptions = new Map<string, SubscriptionEntry>();
  private pendingFrames: string[] = [];
  private nextSubscriptionId = 1;
  private reconnectTimer: ReturnType<typeof setTimeout> | null = null;
  private manualClose = false;

  connect(backendServerUrl?: string) {
    if (this.connected || this.connecting) return;
    if (typeof window === 'undefined') return;

    this.connecting = true;
    this.manualClose = false;

    const url = this.resolveSocketUrl(backendServerUrl);

    try {
      this.socket = new WebSocket(url);
      this.socket.onopen = () => {
        this.connecting = false;
        this.sendRaw('CONNECT', {
          'accept-version': '1.2',
          'heart-beat': '0,0',
        });
      };
      this.socket.onmessage = (event) => this.handleSocketMessage(String(event.data ?? ''));
      this.socket.onerror = (event) => {
        console.error('Auction websocket error', event);
      };
      this.socket.onclose = () => {
        this.connected = false;
        this.connecting = false;
        this.socket = null;
        if (!this.manualClose) this.scheduleReconnect();
      };
    } catch (error) {
      this.connecting = false;
      console.error('Failed to open auction websocket', error);
    }
  }

  disconnect() {
    this.manualClose = true;
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer);
      this.reconnectTimer = null;
    }
    if (this.socket) {
      try {
        this.sendRaw('DISCONNECT', {});
      } catch {
        // ignore
      }
      this.socket.close();
    }
    this.socket = null;
    this.connected = false;
    this.connecting = false;
  }

  subscribe<T = unknown>(destination: string, callback: (message: T) => void) {
    const existing = this.subscriptions.get(destination);
    if (existing) {
      existing.callback = callback as Callback;
      return;
    }

    const entry: SubscriptionEntry = {
      destination,
      callback: callback as Callback,
      id: String(this.nextSubscriptionId++),
    };
    this.subscriptions.set(destination, entry);
    this.connect();
    if (this.connected) {
      this.sendSubscribe(entry);
    }
  }

  unsubscribe(destination: string) {
    const entry = this.subscriptions.get(destination);
    if (!entry) return;
    this.subscriptions.delete(destination);
    this.sendRaw('UNSUBSCRIBE', { id: entry.id });
  }

  send(destination: string, payload: unknown) {
    this.connect();
    this.sendRaw('SEND', {
      destination,
      'content-type': 'application/json',
    }, JSON.stringify(payload));
  }

  private resubscribeAll() {
    for (const entry of this.subscriptions.values()) {
      this.sendSubscribe(entry);
    }
  }

  private sendSubscribe(entry: SubscriptionEntry) {
    this.sendRaw('SUBSCRIBE', {
      id: entry.id,
      destination: entry.destination,
      ack: 'auto',
    });
  }

  private scheduleReconnect() {
    if (this.reconnectTimer || this.manualClose) return;
    this.reconnectTimer = setTimeout(() => {
      this.reconnectTimer = null;
      this.connect();
    }, 3000);
  }

  private handleSocketMessage(data: string) {
    const frames = data.split('\u0000');
    for (const rawFrame of frames) {
      const frame = rawFrame.trim();
      if (!frame) continue;
      if (frame === '\n' || frame === '\r') continue;
      const parsed = this.parseFrame(frame);
      if (!parsed) continue;
      const { command, headers, body } = parsed;
      if (command === 'CONNECTED') {
        this.connected = true;
        const framesToFlush = this.pendingFrames.splice(0);
        framesToFlush.forEach((pendingFrame) => this.socket?.send(pendingFrame));
        this.resubscribeAll();
      } else if (command === 'MESSAGE') {
        this.dispatchMessage(headers, body);
      } else if (command === 'ERROR') {
        console.error('STOMP error', headers, body);
      }
    }
  }

  private dispatchMessage(headers: Record<string, string>, body: string) {
    const destination = headers.destination;
    if (!destination) return;
    const entry = this.subscriptions.get(destination);
    if (!entry) return;

    try {
      entry.callback(body ? JSON.parse(body) : null);
    } catch {
      entry.callback(body);
    }
  }

  private sendRaw(command: string, headers: Record<string, string>, body = '') {
    const frame = this.buildFrame(command, headers, body);
    if (command === 'CONNECT') {
      if (this.socket && this.socket.readyState === WebSocket.OPEN) {
        this.socket.send(frame);
        return;
      }
      this.pendingFrames.push(frame);
      return;
    }

    if (this.connected && this.socket && this.socket.readyState === WebSocket.OPEN) {
      this.socket.send(frame);
      return;
    }
    this.pendingFrames.push(frame);
  }

  private buildFrame(command: string, headers: Record<string, string>, body: string) {
    const headerLines = Object.entries(headers).map(([key, value]) => `${key}:${this.escapeHeader(value)}`);
    const parts = [command, ...headerLines, ''];
    if (body) parts.push(body);
    parts.push('\u0000');
    return parts.join('\n');
  }

  private parseFrame(frame: string): { command: string; headers: Record<string, string>; body: string } | null {
    const lines = frame.split('\n');
    const command = lines.shift()?.trim();
    if (!command) return null;

    const headers: Record<string, string> = {};
    let index = 0;
    while (index < lines.length) {
      const line = lines[index];
      index += 1;
      if (line === '') break;
      const separator = line.indexOf(':');
      if (separator < 0) continue;
      const key = line.slice(0, separator).trim();
      const value = line.slice(separator + 1).trim();
      headers[key] = value;
    }

    const body = lines.slice(index).join('\n').replace(/\u0000$/, '');
    return { command, headers, body };
  }

  private escapeHeader(value: string) {
    return String(value)
      .replace(/\\/g, '\\\\')
      .replace(/\n/g, '\\n')
      .replace(/:/g, '\\c');
  }

  private resolveSocketUrl(backendServerUrl?: string) {
    const baseUrl = backendServerUrl || window.location.origin;
    const socketBase = baseUrl.replace(/^http:/, 'ws:').replace(/^https:/, 'wss:');
    return `${socketBase.replace(/\/$/, '')}/auction-websocket`;
  }
}

export const auctionSocket = new AuctionSocket();
export default auctionSocket;
