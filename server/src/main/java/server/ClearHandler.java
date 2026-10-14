package server;

import dataaccess.DataAccessException;
import io.javalin.http.Context;
import io.javalin.http.Handler;
import service.ClearService;

public class ClearHandler implements Handler {
    private final ClearService clearService;

    public ClearHandler(ClearService clearService) {
        this.clearService = clearService;
    }

    @Override
    public void handle(Context ctx) throws DataAccessException {
        clearService.clear();
        ctx.contentType("application/json");
        ctx.result("{}");
    }
}