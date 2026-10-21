package service;

import chess.ChessGame;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GameServiceTest {
    private static final String VALID_TOKEN = "validToken";

    private GameDAO gameDAO;
    private GameService gameService;

    @BeforeEach
    public void setUp() throws Exception {
        gameDAO = new MemoryGameDAO();
        AuthDAO authDAO = new MemoryAuthDAO();
        authDAO.createAuth(new AuthData(VALID_TOKEN, "isaac"));
        gameService = new GameService(gameDAO, authDAO);
    }

    @Test
    public void createGameSuccess() throws Exception {
        CreateGameResult result = gameService.createGame(VALID_TOKEN, new CreateGameRequest("my game"));

        Assertions.assertEquals("my game", gameDAO.getGame(result.gameID()).gameName());
    }

    @Test
    public void createGameUnauthorized() {
        Assertions.assertThrows(UnauthorizedException.class, () ->
                gameService.createGame("notARealToken", new CreateGameRequest("my game")));
    }

    @Test
    public void createGameMissingName() {
        Assertions.assertThrows(BadRequestException.class, () ->
                gameService.createGame(VALID_TOKEN, new CreateGameRequest(null)));
    }

    @Test
    public void listGamesSuccess() throws Exception {
        gameService.createGame(VALID_TOKEN, new CreateGameRequest("game one"));
        gameService.createGame(VALID_TOKEN, new CreateGameRequest("game two"));

        ListGamesResult result = gameService.listGames(VALID_TOKEN);

        Assertions.assertEquals(2, result.games().size());
    }

    @Test
    public void listGamesUnauthorized() {
        Assertions.assertThrows(UnauthorizedException.class, () ->
                gameService.listGames("notARealToken"));
    }

    @Test
    public void joinGameSuccess() throws Exception {
        int gameID = gameService.createGame(VALID_TOKEN, new CreateGameRequest("my game")).gameID();

        gameService.joinGame(VALID_TOKEN, new JoinGameRequest(ChessGame.TeamColor.WHITE, gameID));

        Assertions.assertEquals("isaac", gameDAO.getGame(gameID).whiteUsername());
        Assertions.assertNull(gameDAO.getGame(gameID).blackUsername());
    }

    @Test
    public void joinGameColorTaken() throws Exception {
        int gameID = gameService.createGame(VALID_TOKEN, new CreateGameRequest("my game")).gameID();
        gameService.joinGame(VALID_TOKEN, new JoinGameRequest(ChessGame.TeamColor.BLACK, gameID));

        Assertions.assertThrows(AlreadyTakenException.class, () ->
                gameService.joinGame(VALID_TOKEN, new JoinGameRequest(ChessGame.TeamColor.BLACK, gameID)));
    }

    @Test
    public void joinGameBadGameID() {
        Assertions.assertThrows(BadRequestException.class, () ->
                gameService.joinGame(VALID_TOKEN, new JoinGameRequest(ChessGame.TeamColor.WHITE, 999)));
    }

    @Test
    public void joinGameUnauthorized() {
        Assertions.assertThrows(UnauthorizedException.class, () ->
                gameService.joinGame("notARealToken", new JoinGameRequest(ChessGame.TeamColor.WHITE, 1)));
    }
}