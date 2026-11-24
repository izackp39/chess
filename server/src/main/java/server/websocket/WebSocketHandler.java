package server.websocket;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
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
import websocket.commands.MakeMoveCommand;
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
            if (command == null || command.getCommandType() == null) {
                throw new BadRequestException("missing command type");
            }
            AuthData auth = gameplay.authenticate(command.getAuthToken());
            GameData game = gameplay.getGame(command.getGameID());
            switch (command.getCommandType()) {
                case CONNECT -> connect(session, auth, game);
                case MAKE_MOVE -> {
                    ChessMove move = gson.fromJson(ctx.message(), MakeMoveCommand.class).getMove();
                    makeMove(session, auth, game, move);
                }
                case LEAVE -> leave(session, auth, game);
                case RESIGN -> resign(session, auth, game);
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

    private void makeMove(Session session, AuthData auth, GameData game, ChessMove move)
            throws BadRequestException, DataAccessException, IOException {
        gameplay.makeMove(game, auth.username(), move);
        int gameID = game.gameID();

        connections.broadcast(gameID, new LoadGameMessage(game.game()), null);
        String moved = String.format("%s moved %s", auth.username(), describe(move));
        connections.broadcast(gameID, new NotificationMessage(moved), session);

        String status = statusMessage(game);
        if (status != null) {
            connections.broadcast(gameID, new NotificationMessage(status), null);
        }
    }

    private void leave(Session session, AuthData auth, GameData game)
            throws DataAccessException, IOException {
        gameplay.leave(game, auth.username());
        connections.remove(game.gameID(), session);
        String message = String.format("%s left the game", auth.username());
        connections.broadcast(game.gameID(), new NotificationMessage(message), session);
    }

    private void resign(Session session, AuthData auth, GameData game)
            throws BadRequestException, DataAccessException, IOException {
        gameplay.resign(game, auth.username());
        String message = String.format("%s resigned the game", auth.username());
        connections.broadcast(game.gameID(), new NotificationMessage(message), null);
    }

    /**
     * Describes a move in board notation, e.g. "e2 to e4"
     */
    private static String describe(ChessMove move) {
        String text = squareName(move.getStartPosition()) + " to " + squareName(move.getEndPosition());
        if (move.getPromotionPiece() != null) {
            text += " (promoted to " + move.getPromotionPiece().toString().toLowerCase() + ")";
        }
        return text;
    }

    private static String squareName(ChessPosition position) {
        char file = (char) ('a' + position.getColumn() - 1);
        return file + Integer.toString(position.getRow());
    }

    /**
     * Describes checkmate, stalemate, or check for the player whose turn it now is
     * (by username, or by color if their seat is empty), or returns null if none applies
     */
    private static String statusMessage(GameData game) {
        ChessGame chess = game.game();
        ChessGame.TeamColor toMove = chess.getTeamTurn();
        String player = (toMove == ChessGame.TeamColor.WHITE) ? game.whiteUsername() : game.blackUsername();
        if (player == null) {
            // that player left, so the seat is empty; name the color instead
            player = toMove.toString().toLowerCase();
        }
        if (chess.isInCheckmate(toMove)) {
            return player + " is in checkmate";
        }
        if (chess.isInStalemate(toMove)) {
            return player + " is in stalemate";
        }
        if (chess.isInCheck(toMove)) {
            return player + " is in check";
        }
        return null;
    }

    private void sendError(Session session, String reason) {
        try {
            connections.send(session, new ErrorMessage("Error: " + reason));
        } catch (IOException ex) {
            System.err.println("Unable to send error message: " + ex.getMessage());
        }
    }
}
