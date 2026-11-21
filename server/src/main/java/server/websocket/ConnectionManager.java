package server.websocket;

import com.google.gson.Gson;
import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.ServerMessage;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks which WebSocket sessions are connected to which game, and sends
 * server messages to them
 */
public class ConnectionManager {
    private final Gson gson = new Gson();
    private final ConcurrentHashMap<Integer, Set<Session>> gameSessions = new ConcurrentHashMap<>();

    public void add(int gameID, Session session) {
        gameSessions.computeIfAbsent(gameID, id -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void remove(int gameID, Session session) {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions != null) {
            sessions.remove(session);
        }
    }

    /**
     * Removes a session from every game, e.g. when its connection closes
     * without the user leaving first
     */
    public void removeEverywhere(Session session) {
        for (Set<Session> sessions : gameSessions.values()) {
            sessions.remove(session);
        }
    }

    public void send(Session session, ServerMessage message) throws IOException {
        if (session.isOpen()) {
            session.getRemote().sendString(gson.toJson(message));
        }
    }

    /**
     * Sends a message to every open session in a game
     *
     * @param exclude a session to skip, or null to send to everyone
     */
    public void broadcast(int gameID, ServerMessage message, Session exclude) throws IOException {
        Set<Session> sessions = gameSessions.get(gameID);
        if (sessions == null) {
            return;
        }
        for (Session session : sessions) {
            if (!session.equals(exclude)) {
                send(session, message);
            }
        }
    }
}
