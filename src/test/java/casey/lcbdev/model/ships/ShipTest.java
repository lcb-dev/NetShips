package casey.lcbdev.model.ships;

import casey.lcbdev.model.board.ShipCell;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ShipTest {
    
    @Test
    void testShipInitialization() {
        ShipCell[] cells = { new ShipCell(0,0), new ShipCell(0,1), new ShipCell(0,2) };
        Ship ship = new Submarine(cells);

        assertEquals("Submarine", ship.getName());
        assertEquals(3, ship.getLength());
        assertArrayEquals(cells, ship.getCellsOccupied());
        assertFalse(ship.isSunk());
    }

    @Test
    void testMarkHitAndSunk() {
        ShipCell[] cells = { new ShipCell(0,0), new ShipCell(0,1), new ShipCell(0,2) };
        Ship ship = new Destroyer(cells);

        assertFalse(ship.isSunk());
        assertTrue(ship.markHit(cells[0]));
        assertFalse(ship.isSunk());
        assertTrue(ship.markHit(cells[1]));
        assertFalse(ship.isSunk());
        assertTrue(ship.markHit(cells[2]));
        assertTrue(ship.isSunk());
    }
}
