package casey.lcbdev.model.board;

import javafx.scene.input.MouseButton;

/**
 * Generic handler for board events.
 */
public interface BoardHandler<T> {
    void onHoverEnter(Cell<T> cell);
    void onHoverExit(Cell<T> cell);
    void onClick(Cell<T> cell, MouseButton button, int clickCount);
}
