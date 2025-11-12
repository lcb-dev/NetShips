package casey.lcbdev.model.board;

import casey.lcbdev.model.ships.Ship;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

import java.util.Objects;

/**
 * Minimal default view for a Cell. 
 */
public class CellView<T> extends StackPane {
    final Cell<T> cell;
    private final Rectangle bg;
    private final Label marker;
    private final Rectangle ghostOverlay = new Rectangle();
    private boolean showingGhost = false;

    public CellView(Cell<T> cell) {
        this.cell = cell;
        this.bg = new Rectangle();
        this.bg.setStroke(Color.GRAY);
        this.bg.setFill(Color.WHITE);
        this.bg.setStrokeWidth(1.0);
        this.marker = new Label();

        ghostOverlay.setMouseTransparent(true);
        ghostOverlay.setOpacity(0.5);
        ghostOverlay.setVisible(false);

        this.getChildren().addAll(bg, ghostOverlay, marker);
    }

    public void setHoverVisual(boolean hover) {
        bg.setFill(hover ? Color.CYAN : Color.WHITE);
    }

    public void setSelected(boolean selected) {
        bg.setStroke(selected ? Color.BLUE : Color.GRAY);
        bg.setStrokeWidth(selected ? 3.0 : 1.0);
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        bg.setWidth(w);
        bg.setHeight(h);
        ghostOverlay.setWidth(w);
        ghostOverlay.setHeight(h);
        super.layoutChildren();
    }

    /**
     * To handle when the player is trying to place a ship, show a helpful "ghost" overlay.
     * @param show Should overlay be shown
     * @param valid Is the placement valid. Green=yes, Red=no.
     */
    public void setGhostVisual(boolean show, boolean valid) {
        this.showingGhost = show;
        if(!show) {
            ghostOverlay.setVisible(false);
        } else {
            ghostOverlay.setFill(valid ? Color.GREEN : Color.RED);
            ghostOverlay.setVisible(true);
        }
    }

    public void clearGhost() {
        setGhostVisual(false, false);
    }

    /*
     * UI DRAWING
     */

    // @Deprecated
    // public void refresh() {
    //     if(cell.isOccupied() && cell.getOccupant() != null) {
    //         marker.setText(cell.getOccupant().toString());
    //     } else {
    //         marker.setText("");
    //     }
    // }

    public void refresh() {
        bg.setStroke(Color.GRAY);

        // Default cell visual - when nothing is in it.
        if(!cell.isOccupied() || cell.getOccupant() == null) {
            marker.setText("");
            marker.setVisible(false);
            bg.setFill(Color.WHITE);
            bg.setOpacity(1.0);
            return;
        }

        // Anything that isn't a ship... shouldn't happen, but handle it anyway.
        Object occ = cell.getOccupant();
        if(!(occ instanceof Ship)) {
            marker.setText(Objects.toString(occ));
            marker.setVisible(true);
            bg.setFill(Color.LIGHTGRAY);
            return;
        }

        Ship ship = (Ship) occ;

        int partIndex = getShipPartIndex(ship);
        String symbol = getSymbolForShip(ship);

        marker.setText(symbol + (partIndex+1));
        marker.setVisible(true);

        Color fill = getColorForShipLength(ship.getLength());
        bg.setFill(fill);
        bg.setOpacity(1.0);
    }

    private int getShipPartIndex(Ship ship) {
        ShipCell[] parts = ship.getCellsOccupied();
        if(parts == null) {
            return -1;
        }

        for(int i = 0;i < parts.length; i++) {
            ShipCell sc = parts[i];
            if (sc == null) {
                continue;
            }
            if (sc.getX() == cell.getX() && sc.getY() == cell.getY()) {
                return i;
            }
        }

        return -1;
    }

    private String getSymbolForShip(Ship ship) {
        String name = ship.getName();
        if (name == null || name.isBlank()) return "S";
        return name.substring(0, 1).toUpperCase();
    }

    private Color getColorForShipLength(int length) {
        return switch(length) {
            case 5 -> Color.AZURE;
            case 4 -> Color.DARKGREEN;
            case 3 -> Color.RED;
            case 2 -> Color.YELLOW;
            default -> Color.LIGHTGRAY;
        };
    }
}
