package dataaccess.sql;

import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.UserData;

public class SqlUserDAO implements UserDAO {

    public SqlUserDAO() throws DataAccessException {
        DatabaseSetup.createTable("""
            CREATE TABLE IF NOT EXISTS user (
              username VARCHAR(255) NOT NULL,
              password VARCHAR(255) NOT NULL,
              email VARCHAR(255) NOT NULL,
              PRIMARY KEY (username)
            )
            """);
    }

    @Override
    public void clear() throws DataAccessException {
    }

    @Override
    public void createUser(UserData user) throws DataAccessException {
    }

    @Override
    public UserData getUser(String username) throws DataAccessException {
        return null;
    }
}