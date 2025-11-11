package casey.lcbdev.model.ships;

import casey.lcbdev.model.board.ShipCell;

public class PatrolBoat extends Ship {
    public PatrolBoat(ShipCell[] cellsOccupied) {
        super("Patrol Boat", 2, cellsOccupied);
    }
}
