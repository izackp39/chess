package client;

import com.google.gson.Gson;
import jakarta.websocket.ContainerProvider;
import jakarta.websocket.DeploymentException;
import jakarta.websocket.Endpoint;
import jakarta.websocket.EndpointConfig;
import jakarta.websocket.Session;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Holds the WebSocket connection to the server: sends user game commands and
 * passes every incoming server message to the observer
 */
public class WebSocketCommunicator extends Endpoint {
    private final Gson gson = new Gson();
    private final ServerMessageObserver observer;
    private final Session session;

    public WebSocketCommunicator(String serverUrl, ServerMessageObserver observer) throws ResponseException {
        this.observer = observer;
        try {
            URI uri = new URI(serverUrl.replaceFirst("^http", "ws") + "/ws");
            session = ContainerProvider.getWebSocketContainer().connectToServer(this, uri);
            session.addMessageHandler(String.class, this::handleMessage);
        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, "Unable to connect to the game");
        }
    }

    private void handleMessage(String json) {
        // Tyrus silently drops exceptions thrown here, so print them instead
        try {
            observer.notify(parse(json));
        } catch (Throwable t) {
            t.printStackTrace();
        }
    }

    /**
     * Reads the message type first, then parses the JSON again as the matching subclass
     */
    private ServerMessage parse(String json) {
        ServerMessage base = gson.fromJson(json, ServerMessage.class);
        return switch (base.getServerMessageType()) {
            case LOAD_GAME -> gson.fromJson(json, LoadGameMessage.class);
            case ERROR -> gson.fromJson(json, ErrorMessage.class);
            case NOTIFICATION -> gson.fromJson(json, NotificationMessage.class);
        };
    }

    public void send(UserGameCommand command) throws ResponseException {
        try {
            session.getBasicRemote().sendText(gson.toJson(command));
        } catch (IOException ex) {
            throw new ResponseException(500, "Unable to reach the server");
        }
    }

    public void close() {
        try {
            session.close();
        } catch (IOException ex) {
            // the connection is already gone, so there is nothing left to close
        }
    }

    @Override
    public void onOpen(Session session, EndpointConfig config) {
    }
}
