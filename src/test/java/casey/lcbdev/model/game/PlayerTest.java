package casey.lcbdev.model.game;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import casey.lcbdev.model.board.ShipCell;
import casey.lcbdev.model.ships.Ship;

public class PlayerTest {
    @Test
    void testAddAndCheckAllPlaced() {
        Player player = new Player();
        ShipCell[] cells = { new ShipCell(0,0), new ShipCell(0,1), new ShipCell(0,2) };
        Ship ship = new Ship("destroyer",3,cells);
        assertTrue(player.addShip("destroyer", ship));
        assertFalse(player.allPlaced());
    }
}
