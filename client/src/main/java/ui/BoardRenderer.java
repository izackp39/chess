package ui;

import chess.ChessBoard;
import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;

import static ui.EscapeSequences.RESET_BG_COLOR;
import static ui.EscapeSequences.RESET_TEXT_BOLD_FAINT;
import static ui.EscapeSequences.RESET_TEXT_COLOR;
import static ui.EscapeSequences.SET_BG_COLOR_BLACK;
import static ui.EscapeSequences.SET_BG_COLOR_LIGHT_GREY;
import static ui.EscapeSequences.SET_BG_COLOR_WHITE;
import static ui.EscapeSequences.SET_TEXT_BOLD;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLACK;
import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;
import static ui.EscapeSequences.SET_TEXT_COLOR_RED;

public class BoardRenderer {
    private static final String[] COLUMN_LETTERS = {"a", "b", "c", "d", "e", "f", "g", "h"};
    private static final String EMPTY_SQUARE = "   ";
    private static final int[] ASCENDING = {1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] DESCENDING = {8, 7, 6, 5, 4, 3, 2, 1};

    public static String draw(ChessBoard board, ChessGame.TeamColor perspective) {
        boolean whiteView = (perspective == ChessGame.TeamColor.WHITE);
        int[] rows = whiteView ? DESCENDING : ASCENDING;
        int[] columns = whiteView ? ASCENDING : DESCENDING;

        StringBuilder out = new StringBuilder();
        appendColumnLabels(out, columns);
        for (int row : rows) {
            appendRow(out, board, row, columns);
        }
        appendColumnLabels(out, columns);
        return out.toString().stripTrailing();
    }

    private static void appendColumnLabels(StringBuilder out, int[] columns) {
        out.append(SET_BG_COLOR_LIGHT_GREY).append(SET_TEXT_COLOR_BLACK).append(EMPTY_SQUARE);
        for (int column : columns) {
            out.append(" ").append(COLUMN_LETTERS[column - 1]).append(" ");
        }
        out.append(EMPTY_SQUARE);
        endLine(out);
    }

    private static void appendRow(StringBuilder out, ChessBoard board, int row, int[] columns) {
        appendRowLabel(out, row);
        for (int column : columns) {
            out.append(isLightSquare(row, column) ? SET_BG_COLOR_WHITE : SET_BG_COLOR_BLACK);
            out.append(pieceText(board.getPiece(new ChessPosition(row, column))));
        }
        appendRowLabel(out, row);
        endLine(out);
    }

    private static void appendRowLabel(StringBuilder out, int row) {
        out.append(SET_BG_COLOR_LIGHT_GREY).append(SET_TEXT_COLOR_BLACK).append(" ").append(row).append(" ");
    }

    private static boolean isLightSquare(int row, int column) {
        return (row + column) % 2 == 1;
    }

    private static String pieceText(ChessPiece piece) {
        if (piece == null) {
            return EMPTY_SQUARE;
        }
        String color = (piece.getTeamColor() == ChessGame.TeamColor.WHITE) ? SET_TEXT_COLOR_RED : SET_TEXT_COLOR_BLUE;
        return color + SET_TEXT_BOLD + " " + pieceLetter(piece.getPieceType()) + " " + RESET_TEXT_BOLD_FAINT;
    }

    private static String pieceLetter(ChessPiece.PieceType type) {
        return switch (type) {
            case KING -> "K";
            case QUEEN -> "Q";
            case BISHOP -> "B";
            case KNIGHT -> "N";
            case ROOK -> "R";
            case PAWN -> "P";
        };
    }

    private static void endLine(StringBuilder out) {
        out.append(RESET_BG_COLOR).append(RESET_TEXT_COLOR).append("\n");
    }
}
