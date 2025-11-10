package casey.lcbdev.model.ships;

import casey.lcbdev.model.board.Cell;

public abstract class Ship {
    private String name = "Generic Ship";
    private int length;
    private Cell<Ship>[] cellsOccupied;
    private boolean isSunk;

    public Ship(String name, byte length, Cell<Ship>[] cellsOccupied) {
        this.name=name;
        this.length=length;
        this.cellsOccupied=cellsOccupied;
        this.isSunk=false;
    }

    public Cell<Ship>[] getCellsOccupied() {
        return this.cellsOccupied;
    }
}
