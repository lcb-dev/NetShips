package casey.lcbdev.model.board;

import casey.lcbdev.model.ships.Ship;

/**
 * ShipCell extends the generic Cell<Ship> with Battleship-specific state.
 */
public class ShipCell extends Cell<Ship> {
    private boolean hit = false;
    private ShotState incomingShot = ShotState.UNKNOWN;

    public ShipCell(int x, int y) { super(x, y); }

    public boolean isHit() { return hit; }
    public void setHit(boolean hit) { this.hit = hit; }

    public ShotState getIncomingShot() { return incomingShot; }
    public void setIncomingShot(ShotState s) { this.incomingShot = s; }

    @Override
    public String toString() {
        return "ShipCell(" + getX() + "," + getY() + ")[occ=" + isOccupied() +
               ", hit=" + hit + ", incoming=" + incomingShot + "]";
    }
}