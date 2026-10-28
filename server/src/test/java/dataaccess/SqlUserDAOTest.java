package dataaccess;

import dataaccess.sql.SqlUserDAO;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SqlUserDAOTest {
    private SqlUserDAO userDAO;

    @BeforeEach
    public void setUp() throws DataAccessException {
        userDAO = new SqlUserDAO();
        userDAO.clear();
    }

    @Test
    public void createUserSuccess() throws DataAccessException {
        UserData user = new UserData("isaac", "password", "isaac@byu.edu");
        userDAO.createUser(user);

        Assertions.assertEquals(user, userDAO.getUser("isaac"));
    }

    @Test
    public void createUserDuplicateUsername() throws DataAccessException {
        userDAO.createUser(new UserData("isaac", "password", "isaac@byu.edu"));

        Assertions.assertThrows(DataAccessException.class, () ->
                userDAO.createUser(new UserData("isaac", "other", "other@byu.edu")));
    }

    @Test
    public void getUserSuccess() throws DataAccessException {
        userDAO.createUser(new UserData("isaac", "password", "isaac@byu.edu"));
        userDAO.createUser(new UserData("other", "password2", "other@byu.edu"));

        UserData result = userDAO.getUser("other");

        Assertions.assertNotNull(result);
        Assertions.assertEquals("other@byu.edu", result.email());
    }

    @Test
    public void getUserNotFound() throws DataAccessException {
        Assertions.assertNull(userDAO.getUser("nobody"));
    }

    @Test
    public void clearSuccess() throws DataAccessException {
        userDAO.createUser(new UserData("isaac", "password", "isaac@byu.edu"));
        userDAO.clear();

        Assertions.assertNull(userDAO.getUser("isaac"));
    }
}