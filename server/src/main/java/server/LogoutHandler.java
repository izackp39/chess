package server;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.UnauthorizedException;
import service.UserService;

public class LogoutHandler implements Handler {
    private final UserService userService;

    public LogoutHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(Context ctx) throws UnauthorizedException, DataAccessException {
        String authToken = ctx.header("authorization");
        userService.logout(authToken);
        ctx.contentType("application/json");
        ctx.result("{}");
    }
}