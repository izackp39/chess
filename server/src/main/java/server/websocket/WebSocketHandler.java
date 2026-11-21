package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import dataaccess.DataAccessException;
import io.javalin.websocket.WsCloseContext;
import io.javalin.websocket.WsCloseHandler;
import io.javalin.websocket.WsConnectContext;
import io.javalin.websocket.WsConnectHandler;
import io.javalin.websocket.WsMessageContext;
import io.javalin.websocket.WsMessageHandler;
import model.AuthData;
import model.GameData;
import org.eclipse.jetty.websocket.api.Session;
import service.BadRequestException;
import service.GameplayService;
import service.UnauthorizedException;
import websocket.commands.UserGameCommand;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;

import java.io.IOException;

/**
 * Receives gameplay commands over WebSocket, validates them, and routes
 * each one to the matching gameplay action
 */
public class WebSocketHandler implements WsConnectHandler, WsMessageHandler, WsCloseHandler {
    private final Gson gson = new Gson();
    private final ConnectionManager connections = new ConnectionManager();
    private final GameplayService gameplay;

    public WebSocketHandler(GameplayService gameplay) {
        this.gameplay = gameplay;
    }

    @Override
    public void handleConnect(WsConnectContext ctx) {
        ctx.enableAutomaticPings();
    }

    @Override
    public void handleMessage(WsMessageContext ctx) {
        Session session = ctx.session;
        try {
            UserGameCommand command = gson.fromJson(ctx.message(), UserGameCommand.class);
            AuthData auth = gameplay.authenticate(command.getAuthToken());
            GameData game = gameplay.getGame(command.getGameID());
            switch (command.getCommandType()) {
                case CONNECT -> connect(session, auth, game);
                default -> sendError(session, "unsupported command");
            }
        } catch (IOException ex) {
            System.err.println("Unable to send WebSocket message: " + ex.getMessage());
        } catch (UnauthorizedException ex) {
            sendError(session, "unauthorized");
        } catch (BadRequestException ex) {
            sendError(session, ex.getMessage());
        } catch (DataAccessException | JsonSyntaxException ex) {
            sendError(session, "bad request");
        }
    }

    @Override
    public void handleClose(WsCloseContext ctx) {
        connections.removeEverywhere(ctx.session);
    }

    private void connect(Session session, AuthData auth, GameData game) throws IOException {
        connections.add(game.gameID(), session);
        connections.send(session, new LoadGameMessage(game.game()));

        ChessGame.TeamColor color = gameplay.playerColor(game, auth.username());
        String role = (color == null) ? "an observer" : color.toString().toLowerCase();
        String message = String.format("%s joined the game as %s", auth.username(), role);
        connections.broadcast(game.gameID(), new NotificationMessage(message), session);
    }

    private void sendError(Session session, String reason) {
        try {
            connections.send(session, new ErrorMessage("Error: " + reason));
        } catch (IOException ex) {
            System.err.println("Unable to send error message: " + ex.getMessage());
        }
    }
}
