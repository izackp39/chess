package service;

import chess.ChessGame;
import chess.ChessMove;
import chess.InvalidMoveException;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

/**
 * Game rules behind the WebSocket gameplay commands, kept separate from
 * the networking code in WebSocketHandler
 */
public class GameplayService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameplayService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public AuthData authenticate(String authToken) throws UnauthorizedException, DataAccessException {
        AuthData auth = (authToken == null) ? null : authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("invalid auth token");
        }
        return auth;
    }

    public GameData getGame(Integer gameID) throws BadRequestException, DataAccessException {
        GameData game = (gameID == null) ? null : gameDAO.getGame(gameID);
        if (game == null) {
            throw new BadRequestException("game does not exist");
        }
        return game;
    }

    /**
     * @return the color this user is playing in the game, or null if they are an observer
     */
    public ChessGame.TeamColor playerColor(GameData game, String username) {
        if (username.equals(game.whiteUsername())) {
            return ChessGame.TeamColor.WHITE;
        }
        if (username.equals(game.blackUsername())) {
            return ChessGame.TeamColor.BLACK;
        }
        return null;
    }

    /**
     * Validates and applies a move for the given user, then saves the updated game
     */
    public void makeMove(GameData game, String username, ChessMove move)
            throws BadRequestException, DataAccessException {
        if (move == null) {
            throw new BadRequestException("missing move");
        }
        ChessGame chess = game.game();
        ChessGame.TeamColor color = playerColor(game, username);
        if (color == null) {
            throw new BadRequestException("observers cannot make moves");
        }
        if (chess.isGameOver()) {
            throw new BadRequestException("the game is over");
        }
        if (color != chess.getTeamTurn()) {
            throw new BadRequestException("it is not your turn");
        }
        try {
            chess.makeMove(move);
        } catch (InvalidMoveException ex) {
            throw new BadRequestException("invalid move");
        }
        gameDAO.updateGame(game);
    }
}
