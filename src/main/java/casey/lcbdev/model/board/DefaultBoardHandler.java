package casey.lcbdev.model.board;

import javafx.scene.input.MouseButton;
import java.util.logging.Logger;

import casey.lcbdev.util.Logging;

public class DefaultBoardHandler<T> implements BoardHandler<T> {
    private static final Logger logger = Logging.getLogger(DefaultBoardHandler.class);

    @Override
    public void onHoverEnter(Cell<T> cell) {
        // System.out.println("Hover enter: " + cell);
    }

    @Override
    public void onHoverExit(Cell<T> cell) {
        // System.out.println("Hover exit: " + cell);
    }

    @Override
    public void onClick(Cell<T> cell, MouseButton button, int clickCount) {
        logger.info("Clicked: " + cell + " btn=" + button + " clicks=" + clickCount);
    }
}
