package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.AlreadyTakenException;
import service.BadRequestException;
import service.GameService;
import service.JoinGameRequest;
import service.UnauthorizedException;

public class JoinGameHandler implements Handler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public JoinGameHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handle(Context ctx)
            throws UnauthorizedException, BadRequestException, AlreadyTakenException, DataAccessException {
        String authToken = ctx.header("authorization");
        JoinGameRequest request = gson.fromJson(ctx.body(), JoinGameRequest.class);
        gameService.joinGame(authToken, request);
        ctx.contentType("application/json");
        ctx.result("{}");
    }
}