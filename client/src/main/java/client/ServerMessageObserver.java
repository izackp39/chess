package client;

import websocket.messages.ServerMessage;

/**
 * Receives messages the server pushes over the WebSocket connection
 */
public interface ServerMessageObserver {
    void notify(ServerMessage message);
}
