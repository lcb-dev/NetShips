package casey.lcbdev.model.ships;

import casey.lcbdev.model.board.ShipCell;

public class Destroyer extends Ship {
    public Destroyer(ShipCell[] cellsOccupied) {
        super("Destroyer", 3, cellsOccupied);
    }
}
