package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.AuthData;

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

    private AuthData authenticate(String authToken) throws UnauthorizedException, DataAccessException {
        AuthData auth = (authToken == null) ? null : authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("invalid auth token");
        }
        return auth;
    }
}