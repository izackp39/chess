package client;

import chess.ChessGame;
import com.google.gson.Gson;
import model.CreateGameRequest;
import model.CreateGameResult;
import model.JoinGameRequest;
import model.ListGamesResult;
import model.LoginRequest;
import model.LoginResult;
import model.RegisterRequest;
import model.RegisterResult;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ServerFacade {
    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final Gson gson = new Gson();
    private final String serverUrl;

    public ServerFacade(int port) {
        serverUrl = "http://localhost:" + port;
    }

    public void clear() throws ResponseException {
        HttpRequest request = buildRequest("DELETE", "/db", null, null);
        handleResponse(sendRequest(request), null);
    }

    public RegisterResult register(String username, String password, String email) throws ResponseException {
        RegisterRequest body = new RegisterRequest(username, password, email);
        HttpRequest request = buildRequest("POST", "/user", body, null);
        return handleResponse(sendRequest(request), RegisterResult.class);
    }

    public LoginResult login(String username, String password) throws ResponseException {
        LoginRequest body = new LoginRequest(username, password);
        HttpRequest request = buildRequest("POST", "/session", body, null);
        return handleResponse(sendRequest(request), LoginResult.class);
    }

    public void logout(String authToken) throws ResponseException {
        HttpRequest request = buildRequest("DELETE", "/session", null, authToken);
        handleResponse(sendRequest(request), null);
    }

    public CreateGameResult createGame(String authToken, String gameName) throws ResponseException {
        CreateGameRequest body = new CreateGameRequest(gameName);
        HttpRequest request = buildRequest("POST", "/game", body, authToken);
        return handleResponse(sendRequest(request), CreateGameResult.class);
    }

    public ListGamesResult listGames(String authToken) throws ResponseException {
        HttpRequest request = buildRequest("GET", "/game", null, authToken);
        return handleResponse(sendRequest(request), ListGamesResult.class);
    }

    public void joinGame(String authToken, ChessGame.TeamColor playerColor, int gameID) throws ResponseException {
        JoinGameRequest body = new JoinGameRequest(playerColor, gameID);
        HttpRequest request = buildRequest("PUT", "/game", body, authToken);
        handleResponse(sendRequest(request), null);
    }

    private HttpRequest buildRequest(String method, String path, Object body, String authToken) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(serverUrl + path))
                .method(method, makeRequestBody(body));
        if (body != null) {
            builder.header("Content-Type", "application/json");
        }
        if (authToken != null) {
            builder.header("authorization", authToken);
        }
        return builder.build();
    }

    private HttpRequest.BodyPublisher makeRequestBody(Object body) {
        if (body == null) {
            return HttpRequest.BodyPublishers.noBody();
        }
        return HttpRequest.BodyPublishers.ofString(gson.toJson(body));
    }

    private HttpResponse<String> sendRequest(HttpRequest request) throws ResponseException {
        try {
            return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException ex) {
            throw new ResponseException(500, "Unable to reach the server");
        }
    }

    private <T> T handleResponse(HttpResponse<String> response, Class<T> responseClass) throws ResponseException {
        int status = response.statusCode();
        if (status / 100 != 2) {
            ErrorResponse error = gson.fromJson(response.body(), ErrorResponse.class);
            throw new ResponseException(status, error.message());
        }
        if (responseClass == null) {
            return null;
        }
        return gson.fromJson(response.body(), responseClass);
    }

    private record ErrorResponse(String message) {
    }
}