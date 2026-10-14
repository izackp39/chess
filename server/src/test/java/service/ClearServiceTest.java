package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClearServiceTest {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private GameDAO gameDAO;
    private ClearService clearService;

    @BeforeEach
    public void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        gameDAO = new MemoryGameDAO();
        clearService = new ClearService(userDAO, authDAO, gameDAO);
    }

    @Test
    public void clearRemovesAllData() throws DataAccessException {
        userDAO.createUser(new UserData("isaac", "password", "isaac@byu.edu"));
        authDAO.createAuth(new AuthData("token123", "isaac"));
        gameDAO.createGame("first game");

        clearService.clear();

        Assertions.assertNull(userDAO.getUser("isaac"));
        Assertions.assertNull(authDAO.getAuth("token123"));
        Assertions.assertTrue(gameDAO.listGames().isEmpty());
    }
}