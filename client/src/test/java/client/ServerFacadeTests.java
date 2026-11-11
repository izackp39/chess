package client;

import model.LoginResult;
import model.RegisterResult;
import org.junit.jupiter.api.*;
import server.Server;


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
}