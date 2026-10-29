package service;

import dataaccess.AuthDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mindrot.jbcrypt.BCrypt;

public class UserServiceTest {
    private UserDAO userDAO;
    private AuthDAO authDAO;
    private UserService userService;

    @BeforeEach
    public void setUp() {
        userDAO = new MemoryUserDAO();
        authDAO = new MemoryAuthDAO();
        userService = new UserService(userDAO, authDAO);
    }

    @Test
    public void registerSuccess() throws Exception {
        RegisterResult result = userService.register(
                new RegisterRequest("isaac", "password", "isaac@byu.edu"));

        Assertions.assertEquals("isaac", result.username());
        Assertions.assertNotNull(result.authToken());
        Assertions.assertNotNull(userDAO.getUser("isaac"));
        Assertions.assertEquals("isaac", authDAO.getAuth(result.authToken()).username());
    }

    @Test
    public void registerUsernameTaken() throws Exception {
        userService.register(new RegisterRequest("isaac", "password", "isaac@byu.edu"));

        Assertions.assertThrows(AlreadyTakenException.class, () ->
                userService.register(new RegisterRequest("isaac", "other", "other@byu.edu")));
    }

    @Test
    public void registerMissingField() {
        Assertions.assertThrows(BadRequestException.class, () ->
                userService.register(new RegisterRequest("isaac", null, "isaac@byu.edu")));
    }

    @Test
    public void registerHashesPassword() throws Exception {
        userService.register(new RegisterRequest("isaac", "password", "isaac@byu.edu"));

        String stored = userDAO.getUser("isaac").password();
        Assertions.assertNotEquals("password", stored);
        Assertions.assertTrue(BCrypt.checkpw("password", stored));
    }

    @Test
    public void loginSuccess() throws Exception {
        userService.register(new RegisterRequest("isaac", "password", "isaac@byu.edu"));

        LoginResult result = userService.login(new LoginRequest("isaac", "password"));

        Assertions.assertEquals("isaac", result.username());
        Assertions.assertEquals("isaac", authDAO.getAuth(result.authToken()).username());
    }

    @Test
    public void loginWrongPassword() throws Exception {
        userService.register(new RegisterRequest("isaac", "password", "isaac@byu.edu"));

        Assertions.assertThrows(UnauthorizedException.class, () ->
                userService.login(new LoginRequest("isaac", "wrongPassword")));
    }

    @Test
    public void loginUnknownUser() {
        Assertions.assertThrows(UnauthorizedException.class, () ->
                userService.login(new LoginRequest("nobody", "password")));
    }

    @Test
    public void loginMissingField() {
        Assertions.assertThrows(BadRequestException.class, () ->
                userService.login(new LoginRequest("isaac", null)));
    }

    @Test
    public void logoutSuccess() throws Exception {
        RegisterResult registered = userService.register(
                new RegisterRequest("isaac", "password", "isaac@byu.edu"));

        userService.logout(registered.authToken());

        Assertions.assertNull(authDAO.getAuth(registered.authToken()));
    }

    @Test
    public void logoutInvalidToken() {
        Assertions.assertThrows(UnauthorizedException.class, () ->
                userService.logout("notARealToken"));
    }
}