package client;

import chess.ChessGame;
import model.CreateGameResult;
import model.GameData;
import model.ListGamesResult;
import model.LoginResult;
import model.RegisterResult;
import org.junit.jupiter.api.*;
import server.Server;

import java.util.List;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade facade;

    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        facade = new ServerFacade(port);
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @BeforeEach
    void clearDatabase() throws ResponseException {
        facade.clear();
    }


    @Test
    void clearSucceeds() {
        Assertions.assertDoesNotThrow(() -> facade.clear());
    }

    @Test
    void clearFailsWhenServerUnreachable() {
        ServerFacade offlineFacade = new ServerFacade(1);
        Assertions.assertThrows(ResponseException.class, offlineFacade::clear);
    }

    @Test
    void registerSucceeds() throws ResponseException {
        RegisterResult result = facade.register("player1", "password", "p1@email.com");
        Assertions.assertEquals("player1", result.username());
        Assertions.assertTrue(result.authToken().length() > 10);
    }

    @Test
    void registerFailsWhenUsernameTaken() throws ResponseException {
        facade.register("player1", "password", "p1@email.com");
        ResponseException ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.register("player1", "different", "other@email.com"));
        Assertions.assertEquals(403, ex.statusCode());
    }

    @Test
    void loginSucceeds() throws ResponseException {
        facade.register("player1", "password", "p1@email.com");
        LoginResult result = facade.login("player1", "password");
        Assertions.assertEquals("player1", result.username());
        Assertions.assertTrue(result.authToken().length() > 10);
    }

    @Test
    void loginFailsWithWrongPassword() throws ResponseException {
        facade.register("player1", "password", "p1@email.com");
        ResponseException ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.login("player1", "wrongPassword"));
        Assertions.assertEquals(401, ex.statusCode());
    }

    @Test
    void logoutSucceeds() throws ResponseException {
        String authToken = facade.register("player1", "password", "p1@email.com").authToken();
        Assertions.assertDoesNotThrow(() -> facade.logout(authToken));
    }

    @Test
    void logoutFailsWithInvalidToken() {
        ResponseException ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.logout("notARealToken"));
        Assertions.assertEquals(401, ex.statusCode());
    }

    @Test
    void createGameSucceeds() throws ResponseException {
        String authToken = facade.register("player1", "password", "p1@email.com").authToken();
        CreateGameResult result = facade.createGame(authToken, "game1");
        Assertions.assertTrue(result.gameID() > 0);
    }

    @Test
    void createGameFailsWithInvalidToken() {
        ResponseException ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.createGame("notARealToken", "game1"));
        Assertions.assertEquals(401, ex.statusCode());
    }

    @Test
    void listGamesSucceeds() throws ResponseException {
        String authToken = facade.register("player1", "password", "p1@email.com").authToken();
        facade.createGame(authToken, "game1");
        facade.createGame(authToken, "game2");

        ListGamesResult result = facade.listGames(authToken);
        List<String> names = result.games().stream().map(GameData::gameName).toList();
        Assertions.assertEquals(2, names.size());
        Assertions.assertTrue(names.containsAll(List.of("game1", "game2")));
    }

    @Test
    void listGamesFailsWithInvalidToken() {
        ResponseException ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.listGames("notARealToken"));
        Assertions.assertEquals(401, ex.statusCode());
    }

    @Test
    void joinGameSucceeds() throws ResponseException {
        String authToken = facade.register("player1", "password", "p1@email.com").authToken();
        int gameID = facade.createGame(authToken, "game1").gameID();

        facade.joinGame(authToken, ChessGame.TeamColor.WHITE, gameID);

        GameData game = facade.listGames(authToken).games().iterator().next();
        Assertions.assertEquals("player1", game.whiteUsername());
        Assertions.assertNull(game.blackUsername());
    }

    @Test
    void joinGameFailsWhenColorTaken() throws ResponseException {
        String firstToken = facade.register("player1", "password", "p1@email.com").authToken();
        String secondToken = facade.register("player2", "password", "p2@email.com").authToken();
        int gameID = facade.createGame(firstToken, "game1").gameID();
        facade.joinGame(firstToken, ChessGame.TeamColor.WHITE, gameID);

        ResponseException ex = Assertions.assertThrows(ResponseException.class,
                () -> facade.joinGame(secondToken, ChessGame.TeamColor.WHITE, gameID));
        Assertions.assertEquals(403, ex.statusCode());
    }
}