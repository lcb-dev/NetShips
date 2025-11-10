package casey.lcbdev.model.board;

import javafx.scene.input.MouseButton;

public class DefaultBoardHandler<T> implements BoardHandler<T> {
    @Override
    public void onHoverEnter(Cell<T> cell) {
        System.out.println("Hover enter: " + cell);
    }

    @Override
    public void onHoverExit(Cell<T> cell) {
        System.out.println("Hover exit: " + cell);
    }

    @Override
    public void onClick(Cell<T> cell, MouseButton button, int clickCount) {
        System.out.println("Clicked: " + cell + " btn=" + button + " clicks=" + clickCount);
    }
}
