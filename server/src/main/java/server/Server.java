package server;

import io.javalin.*;
import io.javalin.http.Context;
import service.AlreadyTakenException;
import service.BadRequestException;
import service.ClearService;
import service.UnauthorizedException;
import service.UserService;

import com.google.gson.Gson;
import dataaccess.AuthDAO;
import dataaccess.GameDAO;
import dataaccess.MemoryAuthDAO;
import dataaccess.MemoryGameDAO;
import dataaccess.MemoryUserDAO;
import dataaccess.UserDAO;

import java.util.Map;

public class Server {

    private final Javalin javalin;

    public Server() {
        this(new MemoryUserDAO(), new MemoryAuthDAO(), new MemoryGameDAO());
    }

    public Server(UserDAO userDAO, AuthDAO authDAO, GameDAO gameDAO) {
        javalin = Javalin.create(config -> config.staticFiles.add("web"));

        // Register your endpoints and exception handlers here.
        ClearService clearService = new ClearService(userDAO, authDAO, gameDAO);
        UserService userService = new UserService(userDAO, authDAO);

        javalin.delete("/db", new ClearHandler(clearService));
        javalin.post("/user", new RegisterHandler(userService));
        javalin.post("/session", new LoginHandler(userService));

        javalin.exception(BadRequestException.class, (e, ctx) -> sendError(ctx, 400, "bad request"));
        javalin.exception(UnauthorizedException.class, (e, ctx) -> sendError(ctx, 401, "unauthorized"));
        javalin.exception(AlreadyTakenException.class, (e, ctx) -> sendError(ctx, 403, "already taken"));
        javalin.exception(Exception.class, (e, ctx) -> sendError(ctx, 500, e.getMessage()));
    }

    public int run(int desiredPort) {
        javalin.start(desiredPort);
        return javalin.port();
    }

    public void stop() {
        javalin.stop();
    }

    private static void sendError(Context ctx, int status, String description) {
        ctx.status(status);
        ctx.contentType("application/json");
        ctx.result(new Gson().toJson(Map.of("message", "Error: " + description)));
    }
}
