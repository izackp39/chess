package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.AlreadyTakenException;
import service.BadRequestException;
import service.RegisterRequest;
import service.RegisterResult;
import service.UserService;

public class RegisterHandler implements Handler {
    private final UserService userService;
    private final Gson gson = new Gson();

    public RegisterHandler(UserService userService) {
        this.userService = userService;
    }

    @Override
    public void handle(Context ctx) throws BadRequestException, AlreadyTakenException, DataAccessException {
        RegisterRequest request = gson.fromJson(ctx.body(), RegisterRequest.class);
        RegisterResult result = userService.register(request);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(result));
    }
}