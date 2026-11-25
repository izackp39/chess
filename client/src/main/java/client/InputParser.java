package client;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

/**
 * Turns text the user typed into chess values. Each method returns null if the text isn't valid.
 */
final class InputParser {
    static final String SQUARE_FORMAT = "Squares look like e2 or h8 (a letter a-h, then a number 1-8).";

    private InputParser() {
    }

    static ChessPosition parsePosition(String square) {
        String s = square.toLowerCase();
        if (!s.matches("[a-h][1-8]")) {
            return null;
        }
        return new ChessPosition(s.charAt(1) - '0', s.charAt(0) - 'a' + 1);
    }

    static ChessPiece.PieceType parsePromotion(String piece) {
        return switch (piece.toLowerCase()) {
            case "queen", "q" -> ChessPiece.PieceType.QUEEN;
            case "rook", "r" -> ChessPiece.PieceType.ROOK;
            case "bishop", "b" -> ChessPiece.PieceType.BISHOP;
            case "knight", "n" -> ChessPiece.PieceType.KNIGHT;
            default -> null;
        };
    }

    static ChessGame.TeamColor parseColor(String color) {
        return switch (color.toLowerCase()) {
            case "white" -> ChessGame.TeamColor.WHITE;
            case "black" -> ChessGame.TeamColor.BLACK;
            default -> null;
        };
    }
}
