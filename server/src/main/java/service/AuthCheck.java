package service;

import dataaccess.AuthDAO;
import dataaccess.DataAccessException;
import model.AuthData;

/**
 * Looks up the auth token and rejects requests that don't have a valid one
 */
final class AuthCheck {
    private AuthCheck() {
    }

    static AuthData require(AuthDAO authDAO, String authToken)
            throws UnauthorizedException, DataAccessException {
        AuthData auth = (authToken == null) ? null : authDAO.getAuth(authToken);
        if (auth == null) {
            throw new UnauthorizedException("invalid auth token");
        }
        return auth;
    }
}
