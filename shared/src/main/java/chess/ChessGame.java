package chess;

import java.util.Collection;
import java.util.ArrayList;
import java.util.Objects;
import java.util.List;

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

    /**
     * Creates a new game with a standard starting board and white to move
     */
    public ChessGame() {
        board = new ChessBoard();
        board.resetBoard();
        teamTurn = TeamColor.WHITE;
    }

    /**
     * Two games are equal if their boards, turn, and castling flags all match
     */
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
                && blackRookHMoved == that.blackRookHMoved;
    }

    /**
     * Hashes the same fields used in equals
     */
    @Override
    public int hashCode() {
        return Objects.hash(board, teamTurn, whiteKingMoved, whiteRookAMoved, whiteRookHMoved,
                blackKingMoved, blackRookAMoved, blackRookHMoved);
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
        var piece = board.getPiece(startPosition);
        if (piece == null) {
            return null;
        }

        var legalMoves = new ArrayList<ChessMove>();
        for (var move : piece.pieceMoves(board, startPosition)) {
            var simulatedBoard = board.copy();
            executeMove(simulatedBoard, move);
            if (!isInCheck(piece.getTeamColor(), simulatedBoard)) {
                legalMoves.add(move);
            }
        }
        return legalMoves;
    }

    /**
     * Applies a move to the given board without any legality checks,
     * swapping in the promotion piece if the move has one
     */
    private void executeMove(ChessBoard b, ChessMove move) {
        var piece = b.getPiece(move.getStartPosition());
        var placedPiece = move.getPromotionPiece() != null
                ? new ChessPiece(piece.getTeamColor(), move.getPromotionPiece())
                : piece;
        b.addPiece(move.getStartPosition(), null);
        b.addPiece(move.getEndPosition(), placedPiece);
    }

    /**
     * Makes a move in the chess game
     *
     * @param move chess move to perform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        var piece = board.getPiece(move.getStartPosition());
        if (piece == null || piece.getTeamColor() != teamTurn) {
            throw new InvalidMoveException("No piece belonging to the current team at " + move.getStartPosition());
        }

        var legalMoves = validMoves(move.getStartPosition());
        if (legalMoves == null || !legalMoves.contains(move)) {
            throw new InvalidMoveException("Invalid move: " + move);
        }

        executeMove(board, move);
        teamTurn = opponent(teamTurn);
    }

    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        return isInCheck(teamColor, board);
    }

    /**
     * Determines if the given team's king is under attack on the given board
     * (used to test hypothetical boards in validMoves)
     */
    private boolean isInCheck(TeamColor teamColor, ChessBoard b) {
        var kingPosition = findKing(b, teamColor);
        return kingPosition != null && isSquareAttacked(b, kingPosition, opponent(teamColor));
    }

    /**
     * Returns the positions of every piece belonging to the given team
     */
    private static List<ChessPosition> positionsOf(ChessBoard b, TeamColor team) {
        var positions = new ArrayList<ChessPosition>();
        for (int row = 1; row <= 8; row++) {
            for (int col = 1; col <= 8; col++) {
                var pos = new ChessPosition(row, col);
                var piece = b.getPiece(pos);
                if (piece != null && piece.getTeamColor() == team) {
                    positions.add(pos);
                }
            }
        }
        return positions;
    }

    /**
     * Finds the given team's king, or null if it isn't on the board
     */
    private ChessPosition findKing(ChessBoard b, TeamColor teamColor) {
        for (var pos : positionsOf(b, teamColor)) {
            if (b.getPiece(pos).getPieceType() == ChessPiece.PieceType.KING) {
                return pos;
            }
        }
        return null;
    }

    /**
     * Determines if any piece of the given team can move onto the target square
     */
    private boolean isSquareAttacked(ChessBoard b, ChessPosition target, TeamColor byTeam) {
        for (var pos : positionsOf(b, byTeam)) {
            for (var move : b.getPiece(pos).pieceMoves(b, pos)) {
                if (move.getEndPosition().equals(target)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns the other team's color
     */
    private static TeamColor opponent(TeamColor color) {
        return color == TeamColor.WHITE ? TeamColor.BLACK : TeamColor.WHITE;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        return isInCheck(teamColor) && hasNoValidMoves(teamColor);
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves while not in check.
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        return !isInCheck(teamColor) && hasNoValidMoves(teamColor);
    }

    /**
     * Determines if none of the given team's pieces have a legal move
     */
    private boolean hasNoValidMoves(TeamColor teamColor) {
        for (var pos : positionsOf(board, teamColor)) {
            if (!validMoves(pos).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Sets this game's chessboard to a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        this.board = board;
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
