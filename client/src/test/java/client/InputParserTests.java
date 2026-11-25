package client;

import chess.ChessGame;
import chess.ChessPiece;
import chess.ChessPosition;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class InputParserTests {

    @Test
    public void parsePositionCorners() {
        Assertions.assertEquals(new ChessPosition(1, 1), InputParser.parsePosition("a1"));
        Assertions.assertEquals(new ChessPosition(8, 8), InputParser.parsePosition("h8"));
    }

    @Test
    public void parsePositionIgnoresCase() {
        Assertions.assertEquals(new ChessPosition(2, 5), InputParser.parsePosition("E2"));
    }

    @Test
    public void parsePositionRejectsBadSquares() {
        Assertions.assertNull(InputParser.parsePosition("z9"));
        Assertions.assertNull(InputParser.parsePosition("e22"));
        Assertions.assertNull(InputParser.parsePosition("e"));
        Assertions.assertNull(InputParser.parsePosition(""));
    }

    @Test
    public void parsePromotionAcceptsNamesAndLetters() {
        Assertions.assertEquals(ChessPiece.PieceType.QUEEN, InputParser.parsePromotion("queen"));
        Assertions.assertEquals(ChessPiece.PieceType.QUEEN, InputParser.parsePromotion("Q"));
        Assertions.assertEquals(ChessPiece.PieceType.KNIGHT, InputParser.parsePromotion("n"));
    }

    @Test
    public void parsePromotionRejectsPawnAndKing() {
        Assertions.assertNull(InputParser.parsePromotion("pawn"));
        Assertions.assertNull(InputParser.parsePromotion("king"));
    }

    @Test
    public void parseColorIgnoresCase() {
        Assertions.assertEquals(ChessGame.TeamColor.WHITE, InputParser.parseColor("WHITE"));
        Assertions.assertEquals(ChessGame.TeamColor.BLACK, InputParser.parseColor("black"));
    }

    @Test
    public void parseColorRejectsOtherColors() {
        Assertions.assertNull(InputParser.parseColor("red"));
    }
}
