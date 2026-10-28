package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import model.GameData;

import java.util.ArrayList;
import java.util.Collection;

public class SqlGameDAO implements GameDAO {

    public SqlGameDAO() throws DataAccessException {
        DatabaseSetup.createTable("""
            CREATE TABLE IF NOT EXISTS game (
              gameID INT NOT NULL AUTO_INCREMENT,
              whiteUsername VARCHAR(255),
              blackUsername VARCHAR(255),
              gameName VARCHAR(255) NOT NULL,
              game TEXT NOT NULL,
              PRIMARY KEY (gameID)
            )
            """);
    }

    @Override
    public void clear() throws DataAccessException {
    }

    @Override
    public int createGame(String gameName) throws DataAccessException {
        return 0;
    }

    @Override
    public GameData getGame(int gameID) throws DataAccessException {
        return null;
    }

    @Override
    public Collection<GameData> listGames() throws DataAccessException {
        return new ArrayList<>();
    }

    @Override
    public void updateGame(GameData game) throws DataAccessException {
    }
}