package casey.lcbdev.model.ships;

import casey.lcbdev.model.board.ShipCell;

public class Battleship extends Ship {
    public Battleship(ShipCell[] cellsOccupied) {
        super("Battleship", 4, cellsOccupied);
    }
}
