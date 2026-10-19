package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import dataaccess.UserDAO;
import model.AuthData;
import model.UserData;

import java.util.UUID;

public class UserService {
    private final UserDAO userDAO;
    private final AuthDAO authDAO;

    public UserService(UserDAO userDAO, AuthDAO authDAO) {
        this.userDAO = userDAO;
        this.authDAO = authDAO;
    }

    public RegisterResult register(RegisterRequest request)
            throws BadRequestException, AlreadyTakenException, DataAccessException {
        if (request == null || request.username() == null
                || request.password() == null || request.email() == null) {
            throw new BadRequestException("missing username, password, or email");
        }
        if (userDAO.getUser(request.username()) != null) {
            throw new AlreadyTakenException("username already taken");
        }

        userDAO.createUser(new UserData(request.username(), request.password(), request.email()));
        String authToken = createAuth(request.username());
        return new RegisterResult(request.username(), authToken);
    }

    private String createAuth(String username) throws DataAccessException {
        String authToken = UUID.randomUUID().toString();
        authDAO.createAuth(new AuthData(authToken, username));
        return authToken;
    }
}