package service;

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
}