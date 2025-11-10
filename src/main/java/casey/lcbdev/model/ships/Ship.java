package casey.lcbdev.model.ships;

import casey.lcbdev.model.board.ShipCell;

import java.util.Objects;
import java.util.stream.IntStream;

public class Ship {
    private final String name;
    private final int length;
    private ShipCell[] cellsOccupied;
    private boolean isSunk;
    private boolean[] hits;

    public Ship(String name, int length, ShipCell[] cellsOccupied) {
        this.name=Objects.requireNonNullElse(name, "Generic Ship");
        if(length <= 0) throw new IllegalArgumentException("Length must be positive.");
        this.length=length;

        if(cellsOccupied != null && cellsOccupied.length != length) {
            throw new IllegalArgumentException("Cells occupied length must equal ship length.");
        }
        this.cellsOccupied=cellsOccupied;
        this.hits = new boolean[length];
        this.isSunk=false;

        if(cellsOccupied != null) {
            linkCellsToThisShip();
        }
    }

    public void placeOnCells(ShipCell[] cells) {
        if(cells == null || cells.length != length) {
            throw new IllegalArgumentException("Cells must be non-null and length == ship length.");
        }

        this.cellsOccupied = cells;
        this.hits = new boolean[length];
        linkCellsToThisShip(); 
    }

    private void linkCellsToThisShip() {
        for (int i = 0; i < cellsOccupied.length; i++) {
            ShipCell c = cellsOccupied[i];
            if (c != null) {
                c.setOccupant(this);
            }
        }
    }

    public boolean markHit(ShipCell cell) {
        if(cellsOccupied == null) return false;
        for(int i=0;i<cellsOccupied.length;i++) {
            if(cellsOccupied[i] == cell) {
                hits[i] = true;
                cell.setHit(true);
                return true;
            }
        }
        return false;
    }

    public boolean isSunk() {
        if (cellsOccupied == null) return false;
        this.isSunk = IntStream.range(0, hits.length).allMatch(i -> hits[i]);
        return isSunk;
    }

    public ShipCell[] getCellsOccupied() { return cellsOccupied; }
    public int getLength() { return length; }
    public String getName() { return name; }
}
