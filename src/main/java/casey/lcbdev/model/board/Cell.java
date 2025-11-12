package casey.lcbdev.model.board;

/**
 * Model class to represent a cell on a Board.
 * 
 * @param <T> occupant type
*/
public class Cell<T> {
    private final int posX;
    private final int posY;
    boolean occupied = false;
    boolean occupiable = true;
    private T occupant = null;

    public Cell(int x, int y) {
        this.posX = x;
        this.posY = y;
    }

    public int getX() { return this.posX; }
    public int getY() { return this.posY; }

    public boolean isOccupied() { return occupied; }
    
    public boolean isOccupiable() { return occupiable; }
    public void setOccupiable(boolean occupiable) { this.occupiable = occupiable; }

    public T getOccupant() { return occupant; }
    public boolean setOccupant(T occupant) {
        if (!occupiable) return false;
        this.occupant = occupant;
        this.occupied = occupant != null;
        return true;
    }

    public void clearOccupant() {
        this.occupant = null;
        this.occupied = false;
    }

    @Override
    public String toString() {
        return "Cell(" + posX + "," + posY + ")[occ=" + occupied + ", occuable=" + occupiable + "]";
    }
}
