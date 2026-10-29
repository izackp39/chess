package dataaccess;

import chess.ChessGame;
import dataaccess.sql.SqlGameDAO;
import model.GameData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SqlGameDAOTest {
    private SqlGameDAO gameDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        gameDAO = new SqlGameDAO();
        gameDAO.clear();
    }

    @Test
    public void createGameSuccess() throws DataAccessException {
        int gameID = gameDAO.createGame("My Game");

        GameData result = gameDAO.getGame(gameID);
        Assertions.assertNotNull(result);
        Assertions.assertEquals("My Game", result.gameName());
        Assertions.assertNull(result.whiteUsername());
        Assertions.assertNull(result.blackUsername());
        Assertions.assertEquals(new ChessGame(), result.game());
    }

    @Test
    public void createGameNullName() {
        Assertions.assertThrows(DataAccessException.class, () -> gameDAO.createGame(null));
    }

    @Test
    public void createGameUniqueIDs() throws DataAccessException {
        int first = gameDAO.createGame("First");
        int second = gameDAO.createGame("Second");

        Assertions.assertNotEquals(first, second);
    }

    @Test
    public void getGameNotFound() throws DataAccessException {
        Assertions.assertNull(gameDAO.getGame(9999));
    }

    @Test
    public void listGamesSuccess() throws DataAccessException {
        gameDAO.createGame("First");
        gameDAO.createGame("Second");
        gameDAO.createGame("Third");

        Assertions.assertEquals(3, gameDAO.listGames().size());
    }

    @Test
    public void listGamesEmpty() throws DataAccessException {
        Assertions.assertTrue(gameDAO.listGames().isEmpty());
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        gameDAO.createGame("First");
        gameDAO.clear();

        Assertions.assertTrue(gameDAO.listGames().isEmpty());
        Assertions.assertEquals(1, gameDAO.createGame("Fresh"));
    }
}