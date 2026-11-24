package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPiece;
import chess.ChessPosition;
import model.GameData;
import model.LoginResult;
import model.RegisterResult;
import ui.BoardRenderer;
import websocket.messages.ErrorMessage;
import websocket.messages.LoadGameMessage;
import websocket.messages.NotificationMessage;
import websocket.messages.ServerMessage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class ChessClient implements ServerMessageObserver {
    private final ServerFacade server;
    private State state = State.SIGNEDOUT;
    private String username;
    private String authToken;
    private List<GameData> listedGames = new ArrayList<>();
    private Integer currentGameID;
    private volatile ChessGame currentGame;
    private ChessGame.TeamColor perspective = ChessGame.TeamColor.WHITE;
    private Consumer<String> display = System.out::println;

    public ChessClient(ServerFacade server) {
        this.server = server;
    }

    public State state() {
        return state;
    }

    /**
     * Sets where messages that arrive from the server (outside of eval) are printed
     */
    public void setDisplay(Consumer<String> display) {
        this.display = display;
    }

    @Override
    public void notify(ServerMessage message) {
        String text = switch (message.getServerMessageType()) {
            case LOAD_GAME -> {
                currentGame = ((LoadGameMessage) message).getGame();
                yield BoardRenderer.draw(currentGame.getBoard(), perspective);
            }
            case NOTIFICATION -> ((NotificationMessage) message).getMessage();
            case ERROR -> ((ErrorMessage) message).getErrorMessage();
        };
        display.accept(text);
    }

    public String eval(String input) {
        String[] tokens = input.trim().split("\\s+");
        String command = tokens[0].toLowerCase();
        String[] params = Arrays.copyOfRange(tokens, 1, tokens.length);
        if (command.isEmpty()) {
            return "";
        }
        try {
            return switch (state) {
                case SIGNEDOUT -> evalSignedOut(command, params);
                case GAMEPLAY -> evalGameplay(command, params);
                default -> evalSignedIn(command, params);
            };
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

    private String evalGameplay(String command, String[] params) throws ResponseException {
        return switch (command) {
            case "redraw" -> redraw();
            case "move" -> makeMove(params);
            case "leave" -> leave();
            case "help" -> helpGameplay();
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
        perspective = color;
        enterGame(game.gameID());
        return "Joined '" + game.gameName() + "' as " + color.name().toLowerCase() + ".";
    }

    private String observeGame(String[] params) throws ResponseException {
        if (params.length != 1) {
            return "Usage: observe <NUMBER>";
        }
        GameData game = findListedGame(params[0]);
        if (game == null) {
            return invalidGameNumber();
        }
        perspective = ChessGame.TeamColor.WHITE;
        enterGame(game.gameID());
        return "Observing '" + game.gameName() + "'.";
    }

    private void enterGame(int gameID) throws ResponseException {
        server.connect(authToken, gameID, this);
        currentGameID = gameID;
        state = State.GAMEPLAY;
    }

    private String redraw() {
        if (currentGame == null) {
            return "The board hasn't loaded yet. Try again in a moment.";
        }
        return BoardRenderer.draw(currentGame.getBoard(), perspective);
    }

    /**
     * Sends a move to the server. The result arrives later as a new board or an error,
     * so nothing is printed here.
     */
    private String makeMove(String[] params) throws ResponseException {
        if (params.length < 2 || params.length > 3) {
            return "Usage: move <FROM> <TO> [QUEEN|ROOK|BISHOP|KNIGHT]  e.g. move e2 e4";
        }
        ChessPosition start = parsePosition(params[0]);
        ChessPosition end = parsePosition(params[1]);
        if (start == null || end == null) {
            return "Squares look like e2 or h8 (a letter a-h, then a number 1-8).";
        }
        ChessPiece.PieceType promotion = null;
        if (params.length == 3) {
            promotion = parsePromotion(params[2]);
            if (promotion == null) {
                return "Promotion piece must be QUEEN, ROOK, BISHOP, or KNIGHT.";
            }
        } else if (needsPromotion(start, end)) {
            return "That pawn reaches the last row. Add a piece, e.g. move "
                    + params[0] + " " + params[1] + " queen";
        }
        server.makeMove(authToken, currentGameID, new ChessMove(start, end, promotion));
        return "";
    }

    private ChessPosition parsePosition(String square) {
        String s = square.toLowerCase();
        if (!s.matches("[a-h][1-8]")) {
            return null;
        }
        return new ChessPosition(s.charAt(1) - '0', s.charAt(0) - 'a' + 1);
    }

    private ChessPiece.PieceType parsePromotion(String piece) {
        return switch (piece.toLowerCase()) {
            case "queen", "q" -> ChessPiece.PieceType.QUEEN;
            case "rook", "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }

    private boolean needsPromotion(ChessPosition start, ChessPosition end) {
        ChessGame game = currentGame;
        if (game == null) {
            return false;
        }
        ChessPiece piece = game.getBoard().getPiece(start);
        boolean lastRow = end.getRow() == 1 || end.getRow() == 8;
        return piece != null && piece.getPieceType() == ChessPiece.PieceType.PAWN && lastRow;
    }

    private String leave() throws ResponseException {
        server.leave(authToken, currentGameID);
        currentGameID = null;
        currentGame = null;
        state = State.SIGNEDIN;
        return "You left the game.";
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
        server.disconnect();
        server.logout(authToken);
        username = null;
        authToken = null;
        listedGames = new ArrayList<>();
        state = State.SIGNEDOUT;
        return "Logged out.";
    }

    private String quit() {
        server.disconnect();
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

    private String helpGameplay() {
        return """
                redraw - the chess board
                move <FROM> <TO> [QUEEN|ROOK|BISHOP|KNIGHT] - a piece, e.g. move e2 e4
                leave - the game
                help - with possible commands""";
    }
}