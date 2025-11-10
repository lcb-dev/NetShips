package casey.lcbdev.model.board;

import casey.lcbdev.model.ships.Ship;

/**
 * ShipCell extends the generic Cell<Ship> with Battleship-specific state.
 */
public class ShipCell extends Cell<Ship> {
    private boolean hit = false; // whether this cell has been shot

    public ShipCell(int x, int y) {
        super(x, y);
    }

    public boolean isHit() { return hit; }
    public void setHit(boolean hit) { this.hit = hit; }

    @Override
    public String toString() {
        return "ShipCell(" + getX() + "," + getY() + ")[occ=" + isOccupied() + ", hit=" + hit + "]";
    }
}