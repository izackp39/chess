package client;

import model.GameData;
import model.LoginResult;
import model.RegisterResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String username;
    private String authToken;
    private List<GameData> listedGames = new ArrayList<>();

    public ChessClient(ServerFacade server) {
        this.server = server;
    }

    public State state() {
        return state;
    }

    public String eval(String input) {
        String[] tokens = input.trim().split("\\s+");
        String command = tokens[0].toLowerCase();
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        if (command.isEmpty()) {
            return "";
        }
        try {
            if (state == State.SIGNEDOUT) {
                return evalSignedOut(command, params);
            }
            return evalSignedIn(command, params);
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    private String evalSignedOut(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "register" -> register(params);
            case "login" -> login(params);
            case "quit" -> quit();
            case "help" -> helpSignedOut();
            default -> unknownCommand();
        };
    }

    private String evalSignedIn(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "create" -> createGame(params);
            case "list" -> listGames();
            case "logout" -> logout();
            case "quit" -> quit();
            case "help" -> helpSignedIn();
            default -> unknownCommand();
        };
    }

    private String register(String[] params) throws ResponseException {
        if (params.length != 3) {
            return "Usage: register <USERNAME> <PASSWORD> <EMAIL>";
        }
        RegisterResult result = server.register(params[0], params[1], params[2]);
        signIn(result.username(), result.authToken());
        return "Registered and logged in as " + username + ".";
    }

    private String login(String[] params) throws ResponseException {
        if (params.length != 2) {
            return "Usage: login <USERNAME> <PASSWORD>";
        }
        LoginResult result = server.login(params[0], params[1]);
        signIn(result.username(), result.authToken());
        return "Logged in as " + username + ".";
    }

    private void signIn(String username, String authToken) {
        this.username = username;
        this.authToken = authToken;
        state = State.SIGNEDIN;
    }

    private String createGame(String[] params) throws ResponseException {
        if (params.length == 0) {
            return "Usage: create <NAME>";
        }
        String gameName = String.join(" ", params);
        server.createGame(authToken, gameName);
        return "Created game '" + gameName + "'. Type list to see it.";
    }

    private String listGames() throws ResponseException {
        listedGames = new ArrayList<>(server.listGames(authToken).games());
        if (listedGames.isEmpty()) {
            return "No games yet. Create one with: create <NAME>";
        }
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < listedGames.size(); i++) {
            GameData game = listedGames.get(i);
            result.append(String.format("%d. %s  (white: %s, black: %s)%n", i + 1, game.gameName(),
                    playerOrOpen(game.whiteUsername()), playerOrOpen(game.blackUsername())));
        }
        return result.toString().trim();
    }

    private String playerOrOpen(String player) {
        return (player == null) ? "open" : player;
    }

    private String logout() throws ResponseException {
        server.logout(authToken);
        username = null;
        authToken = null;
        listedGames = new ArrayList<>();
        state = State.SIGNEDOUT;
        return "Logged out.";
    }

    private String quit() {
        state = State.QUIT;
        return "Goodbye!";
    }

    private String unknownCommand() {
        return "Unknown command. Type help to see what you can do.";
    }

    private String helpSignedOut() {
        return """
                register <USERNAME> <PASSWORD> <EMAIL> - to create an account
                login <USERNAME> <PASSWORD> - to play chess
                quit - playing chess
                help - with possible commands""";
    }

    private String helpSignedIn() {
        return """
                create <NAME> - a game
                list - games
                logout - when you are done
                quit - playing chess
                help - with possible commands""";
    }
}