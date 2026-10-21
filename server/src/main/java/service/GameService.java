package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;
import model.GameData;

public class GameService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;

    public GameService(GameDAO gameDAO, AuthDAO authDAO) {
        this.gameDAO = gameDAO;
        this.authDAO = authDAO;
    }

    public CreateGameResult createGame(String authToken, CreateGameRequest request)
            throws UnauthorizedException, BadRequestException, DataAccessException {
        authenticate(authToken);
        if (request == null || request.gameName() == null) {
            throw new BadRequestException("missing game name");
        }

        int gameID = gameDAO.createGame(request.gameName());
        return new CreateGameResult(gameID);
    }

    public ListGamesResult listGames(String authToken) throws UnauthorizedException, DataAccessException {
        authenticate(authToken);
        return new ListGamesResult(gameDAO.listGames());
    }

    public void joinGame(String authToken, JoinGameRequest request)
            throws UnauthorizedException, BadRequestException, AlreadyTakenException, DataAccessException {
        AuthData auth = authenticate(authToken);
        if (request == null || request.playerColor() == null || request.gameID() == null) {
            throw new BadRequestException("missing or invalid player color or game ID");
        }
        GameData game = gameDAO.getGame(request.gameID());
        if (game == null) {
            throw new BadRequestException("game does not exist");
        }

        gameDAO.updateGame(claimColor(game, request.playerColor(), auth.username()));
    }

    private GameData claimColor(GameData game, ChessGame.TeamColor color, String username)
            throws AlreadyTakenException {
        if (color == ChessGame.TeamColor.WHITE) {
            if (game.whiteUsername() != null) {
                throw new AlreadyTakenException("white is already taken");
            }
            return new GameData(game.gameID(), username, game.blackUsername(), game.gameName(), game.game());
        }
        if (game.blackUsername() != null) {
            throw new AlreadyTakenException("black is already taken");
        }
        return new GameData(game.gameID(), game.whiteUsername(), username, game.gameName(), game.game());
    }

    private AuthData authenticate(String authToken) throws UnauthorizedException, DataAccessException {
        AuthData auth = (authToken == null) ? null : authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("invalid auth token");
        }
        return auth;
    }
}