package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;

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

    @Test
    public void updateGameAddsPlayers() throws DataAccessException {
        int gameID = gameDAO.createGame("My Game");
        GameData original = gameDAO.getGame(gameID);

        gameDAO.updateGame(new GameData(gameID, "white", "black", original.gameName(), original.game()));

        GameData result = gameDAO.getGame(gameID);
        Assertions.assertEquals("white", result.whiteUsername());
        Assertions.assertEquals("black", result.blackUsername());
    }

    @Test
    public void updateGameSavesMove() throws Exception {
        int gameID = gameDAO.createGame("My Game");
        GameData original = gameDAO.getGame(gameID);
        ChessGame game = original.game();
        game.makeMove(new ChessMove(new ChessPosition(2, 5), new ChessPosition(4, 5), null));

        gameDAO.updateGame(new GameData(gameID, null, null, original.gameName(), game));

        GameData result = gameDAO.getGame(gameID);
        Assertions.assertEquals(game, result.game());
        Assertions.assertEquals(ChessGame.TeamColor.BLACK, result.game().getTeamTurn());
    }

    @Test
    public void updateGameNotFound() {
        GameData missing = new GameData(9999, null, null, "Ghost", new ChessGame());

        Assertions.assertThrows(DataAccessException.class, () -> gameDAO.updateGame(missing));
    }
}