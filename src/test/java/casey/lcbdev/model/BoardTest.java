package casey.lcbdev.model;

import casey.lcbdev.model.board.Board;
import casey.lcbdev.model.board.ShipCell;
import casey.lcbdev.model.ships.Ship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest extends FxTestBase {

    private Board<Ship> board;

    @BeforeEach
    void setUp() {
        board = new Board<>(10, 10, (x, y) -> new ShipCell(x, y));
    }

    @Test
    void testCellAccess() {
        assertNotNull(board.getCell(0, 0));
        assertNull(board.getCell(-1, 0));
        assertNull(board.getCell(10, 10));
    }

    @Test
    void testForEachCell() {
        int[] count = {0};
        board.forEachCell(c -> count[0]++);
        assertEquals(100, count[0]);
    }
}
