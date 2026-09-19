package chess;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final ChessGame.TeamColor pieceColor;
    private final PieceType type;

    public ChessPiece(ChessGame.TeamColor pieceColor, ChessPiece.PieceType type) {
        this.pieceColor = pieceColor;
        this.type = type;
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    private static final int[][] KNIGHT_OFFSETS = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2},
    };

    private static final int[][] KING_OFFSETS = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
    };

    private static final int[][] ROOK_DIRECTIONS = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};
    private static final int[][] BISHOP_DIRECTIONS = {{1, 1}, {1, -1}, {-1, 1}, {-1, -1}};
    private static final int[][] QUEEN_DIRECTIONS = {
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1},
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
    };

    @Override
    public boolean equals(Object obj){
        if (this == obj){
            return true;
        }
        if (!(obj instanceof ChessPiece that)){
            return false;
        }
        return pieceColor == that.pieceColor && type == that.type;
    }

    @Override
    public int hashCode(){
        return 31 * pieceColor.hashCode() + type.hashCode();
    }

    @Override
    public String toString(){
        return pieceColor + " " + type;
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {return type; }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        return switch (type){
            case KNIGHT -> steppingMoves(board, myPosition, KNIGHT_OFFSETS);
            case KING -> steppingMoves(board, myPosition, KING_OFFSETS);
            case ROOK -> slidingMoves(board, myPosition, ROOK_DIRECTIONS);
            case BISHOP -> slidingMoves(board, myPosition, BISHOP_DIRECTIONS);
            case QUEEN -> slidingMoves(board, myPosition, QUEEN_DIRECTIONS);
            case PAWN -> pawnMoves(board, myPosition);
        };
    }

    private Collection<ChessMove> steppingMoves(ChessBoard board, ChessPosition myPosition, int[][] offsets){
        var moves = new ArrayList<ChessMove>();
        for (var offset : offsets){
            int row = myPosition.getRow() + offset[0];
            int col = myPosition.getColumn() + offset[1];
            if (!onBoard(row, col)){
                continue;
            }
            var target = new ChessPosition(row, col);
            var occupant = board.getPiece(target);
            if (occupant == null || occupant.getTeamColor() != pieceColor){
                moves.add(new ChessMove(myPosition, target, null));
            }
        }
        return moves;
    }

    private Collection<ChessMove> slidingMoves(ChessBoard board, ChessPosition myPosition, int[][] directions){
        var moves = new ArrayList<ChessMove>();
        for (var direction : directions){
            int row = myPosition.getRow();
            int col = myPosition.getColumn();
            while (true){
                row += direction[0];
                col += direction[1];
                if (!onBoard(row, col)){
                    break;
                }
                var target = new ChessPosition(row, col);
                var occupant = board.getPiece(target);
                if (occupant == null){
                    moves.add(new ChessMove(myPosition, target, null));
                }
                else{
                    if (occupant.getTeamColor() != pieceColor){
                        moves.add(new ChessMove(myPosition, target, null));
                    }
                    break;
                }
            }
        }
        return moves;
    }

    private static boolean onBoard(int row, int col){
        return row >= 1 && row <= 8 && col >= 1 && col <= 8;
    }

    private Collection<ChessMove> pawnMoves(ChessBoard board, ChessPosition myPosition){
        var moves = new ArrayList<ChessMove>();
        int direction = pieceColor == ChessGame.TeamColor.WHITE ? 1 : -1;
        int startRow = pieceColor == ChessGame.TeamColor.WHITE ? 2 : 7;
        int promotionRow = pieceColor == ChessGame.TeamColor.WHITE ? 8 : 1;
        int row = myPosition.getRow();
        int col = myPosition.getColumn();

        int oneForwardRow = row + direction;
        if (onBoard(oneForwardRow, col) && board.getPiece(new ChessPosition(oneForwardRow, col)) == null){
            addPawnMove(moves, myPosition, new ChessPosition(oneForwardRow, col), promotionRow);

            int twoForwardRow = row + 2 * direction;
            if (row == startRow && board.getPiece(new ChessPosition(twoForwardRow, col)) == null){
                moves.add(new ChessMove(myPosition, new ChessPosition(twoForwardRow, col), null));
            }
        }

        for (int dc : new int[]{-1, 1}){
            int captureCol = col + dc;
            if (!onBoard(oneForwardRow, captureCol)){
                continue;
            }
            var target = new ChessPosition(oneForwardRow, captureCol);
            var occupant = board.getPiece(target);
            if (occupant != null && occupant.getTeamColor() != pieceColor){
                addPawnMove(moves, myPosition, target, promotionRow);
            }
        }

        return moves;
    }

    private void addPawnMove(List<ChessMove> moves, ChessPosition start, ChessPosition end, int promotionRow){
        if (end.getRow() == promotionRow){
            moves.add(new ChessMove(start, end, PieceType.QUEEN));
            moves.add(new ChessMove(start, end, PieceType.ROOK));
            moves.add(new ChessMove(start, end, PieceType.BISHOP));
            moves.add(new ChessMove(start, end, PieceType.KNIGHT));
        }
        else {
            moves.add(new ChessMove(start, end, null));
        }
    }
}
