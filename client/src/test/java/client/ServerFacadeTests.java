package client;

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
}