package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;

/**
 * A class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private ChessBoard board;
    private TeamColor teamTurn;

    private boolean whiteKingMoved;
    private boolean whiteRookAMoved;
    private boolean whiteRookHMoved;
    private boolean blackKingMoved;
    private boolean blackRookAMoved;
    private boolean blackRookHMoved;

    private ChessPosition enPassantTarget;

    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ChessGame that)) {
            return false;
        }
        return teamTurn == that.teamTurn
                && board.equals(that.board)
                && whiteKingMoved == that.whiteKingMoved
                && whiteRookAMoved == that.whiteRookAMoved
                && whiteRookHMoved == that.whiteRookHMoved
                && blackKingMoved == that.blackKingMoved
                && blackRookAMoved == that.blackRookAMoved
                && blackRookHMoved == that.blackRookHMoved
                && Objects.equals(enPassantTarget, that.enPassantTarget);
    }

    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn, whiteKingMoved, whiteRookAMoved, whiteRookHMoved,
                blackKingMoved, blackRookAMoved, blackRookHMoved, enPassantTarget);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Sets which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets all valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        throw new RuntimeException("Not implemented");
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
        enPassantTarget = null;
        whiteKingMoved = false;
        whiteRookAMoved = false;
        whiteRookHMoved = false;
        blackKingMoved = false;
        blackRookAMoved = false;
        blackRookHMoved = false;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return board;
    }
}
