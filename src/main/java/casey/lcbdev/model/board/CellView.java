package casey.lcbdev.model.board;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

/**
 * Minimal default view for a Cell. 
 */
public class CellView<T> extends StackPane {
    final Cell<T> cell;
    private final Rectangle bg;
    private final Label marker;

    public CellView(Cell<T> cell) {
        this.cell = cell;
        this.bg = new Rectangle();
        this.bg.setStroke(Color.GRAY);
        this.bg.setFill(Color.WHITE);
        this.bg.setStrokeWidth(1.0);
        this.marker = new Label();
        this.getChildren().addAll(bg, marker);
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
        super.layoutChildren();
    }

    public void refresh() {
        if(cell.isOccupied() && cell.getOccupant() != null) {
            marker.setText(cell.getOccupant().toString());
        } else {
            marker.setText("");
        }
    }
}
