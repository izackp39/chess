package client;

import model.LoginResult;
import model.RegisterResult;

import java.util.Arrays;

public class ChessClient {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String username;
    private String authToken;

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
            return evalSignedIn(command);
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

    private String evalSignedIn(String command) {
        return switch (command) {
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
                quit - playing chess
                help - with possible commands""";
    }
}
