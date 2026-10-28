package dataaccess.sql;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

public class SqlAuthDAO implements AuthDAO {

    @Override
    public void clear() throws DataAccessException {
    }

    @Override
    public void createAuth(AuthData auth) throws DataAccessException {
    }

    @Override
    public AuthData getAuth(String authToken) throws DataAccessException {
        return null;
    }

    @Override
    public void deleteAuth(String authToken) throws DataAccessException {
    }
}