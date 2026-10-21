package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.BadRequestException;
import service.CreateGameRequest;
import service.CreateGameResult;
import service.GameService;
import service.UnauthorizedException;

public class CreateGameHandler implements Handler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public CreateGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handle(Context ctx) throws UnauthorizedException, BadRequestException, DataAccessException {
        String authToken = ctx.header("authorization");
        CreateGameRequest request = gson.fromJson(ctx.body(), CreateGameRequest.class);
        CreateGameResult result = gameService.createGame(authToken, request);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(result));
    }
}