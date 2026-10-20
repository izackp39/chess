package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.BadRequestException;
import service.LoginRequest;
import service.LoginResult;
import service.UnauthorizedException;
import service.UserService;

public class LoginHandler implements Handler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public LoginHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(Context ctx) throws BadRequestException, UnauthorizedException, DataAccessException {
        LoginRequest request = gson.fromJson(ctx.body(), LoginRequest.class);
        LoginResult result = userService.login(request);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(result));
    }
}