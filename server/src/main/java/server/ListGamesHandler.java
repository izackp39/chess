package server;

import com.google.gson.Gson;
import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.GameService;
import service.ListGamesResult;
import service.UnauthorizedException;

public class ListGamesHandler implements Handler {
    private final GameService gameService;
    private final Gson gson = new Gson();

    public ListGamesHandler(GameService gameService) {
        this.gameService = gameService;
    }

    @Override
    public void handle(Context ctx) throws UnauthorizedException, DataAccessException {
        String authToken = ctx.header("authorization");
        ListGamesResult result = gameService.listGames(authToken);
        ctx.contentType("application/json");
        ctx.result(gson.toJson(result));
    }
}