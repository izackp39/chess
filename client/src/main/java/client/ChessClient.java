package client;

import chess.ChessBoard;
import chess.ChessGame;
import model.GameData;
import model.LoginResult;
import model.RegisterResult;
import ui.BoardRenderer;

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
            return friendlyError(command, ex.statusCode());
        }
    }

    private String friendlyError(String command, int statusCode) {
        return switch (statusCode) {
            case 400 -> "Some of that information was missing or invalid. Type help to check the format.";
            case 401 -> command.equals("login")
                    ? "Wrong username or password."
                    : "You're not authorized to do that. Try logging out and back in.";
            case 403 -> command.equals("register")
                    ? "That username is already taken."
                    : "That color is already taken in this game. Type list to see open seats.";
            default -> "Couldn't reach the chess server, or it had a problem. Make sure it's running and try again.";
        };
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
            case "join" -> joinGame(params);
            case "observe" -> observeGame(params);
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

    private String joinGame(String[] params) throws ResponseException {
        if (params.length != 2) {
            return "Usage: join <NUMBER> <WHITE|BLACK>";
        }
        GameData game = findListedGame(params[0]);
        if (game == null) {
            return invalidGameNumber();
        }
        ChessGame.TeamColor color = parseColor(params[1]);
        if (color == null) {
            return "Color must be WHITE or BLACK.";
        }
        server.joinGame(authToken, color, game.gameID());
        String message = "Joined '" + game.gameName() + "' as " + color.name().toLowerCase() + ".";
        if (color == ChessGame.TeamColor.WHITE) {
            return message + "\n" + drawStartingBoard();
        }
        return message;
    }

    private String observeGame(String[] params) {
        if (params.length != 1) {
            return "Usage: observe <NUMBER>";
        }
        GameData game = findListedGame(params[0]);
        if (game == null) {
            return invalidGameNumber();
        }
        return "Observing '" + game.gameName() + "'.\n" + drawStartingBoard();
    }

    private String drawStartingBoard() {
        ChessBoard board = new ChessBoard();
        board.resetBoard();
        return BoardRenderer.draw(board);
    }

    private GameData findListedGame(String number) {
        if (!number.matches("\\d{1,9}")) {
            return null;
        }
        int index = Integer.parseInt(number) - 1;
        return (index >= 0 && index < listedGames.size()) ? listedGames.get(index) : null;
    }

    private String invalidGameNumber() {
        if (listedGames.isEmpty()) {
            return "No games listed yet. Type list first, then use a number from the list.";
        }
        return "Game number must be between 1 and " + listedGames.size() + ". Type list to see the games.";
    }

    private ChessGame.TeamColor parseColor(String color) {
        return switch (color.toLowerCase()) {
            case "white" -> ChessGame.TeamColor.WHITE;
            case "black" -> ChessGame.TeamColor.BLACK;
            default -> null;
        };
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
                join <NUMBER> <WHITE|BLACK> - a game
                observe <NUMBER> - a game
                logout - when you are done
                quit - playing chess
                help - with possible commands""";
    }
}