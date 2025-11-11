package casey.lcbdev.model.ships;

import casey.lcbdev.model.board.ShipCell;

public class Submarine extends Ship {
    public Submarine(ShipCell[] cellsOccupied) {
        super("Submarine", 3, cellsOccupied);
    }
}
