package dataaccess;

import dataaccess.sql.SqlAuthDAO;
import model.AuthData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SqlAuthDAOTest {
    private SqlAuthDAO authDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        authDAO = new SqlAuthDAO();
        authDAO.clear();
    }

    @Test
    public void createAuthSuccess() throws DataAccessException {
        AuthData auth = new AuthData("token1", "isaac");
        authDAO.createAuth(auth);

        Assertions.assertEquals(auth, authDAO.getAuth("token1"));
    }

    @Test
    public void createAuthDuplicateToken() throws DataAccessException {
        authDAO.createAuth(new AuthData("token1", "isaac"));

        Assertions.assertThrows(DataAccessException.class, () ->
                authDAO.createAuth(new AuthData("token1", "other")));
    }

    @Test
    public void getAuthSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token1", "isaac"));
        authDAO.createAuth(new AuthData("token2", "isaac"));

        AuthData result = authDAO.getAuth("token2");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("isaac", result.username());
    }

    @Test
    public void getAuthNotFound() throws DataAccessException {
        Assertions.assertNull(authDAO.getAuth("missing"));
    }

    @Test
    public void deleteAuthSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token1", "isaac"));
        authDAO.deleteAuth("token1");

        Assertions.assertNull(authDAO.getAuth("token1"));
    }

    @Test
    public void deleteAuthMissingTokenLeavesOthers() throws DataAccessException {
        authDAO.createAuth(new AuthData("token1", "isaac"));
        authDAO.deleteAuth("missing");

        Assertions.assertNotNull(authDAO.getAuth("token1"));
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        authDAO.createAuth(new AuthData("token1", "isaac"));
        authDAO.clear();

        Assertions.assertNull(authDAO.getAuth("token1"));
    }
}