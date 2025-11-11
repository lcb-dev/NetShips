package casey.lcbdev.model.ships;

import casey.lcbdev.model.board.ShipCell;

public class Carrier extends Ship {
    public Carrier(ShipCell[] cellsOccupied) {
        super("Carrier", 5, cellsOccupied);
    }
}
