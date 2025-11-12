package casey.lcbdev.model.board;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Region;
import java.util.function.Consumer;
import java.util.function.BiFunction;

/**
 * A resizeable grid container of Cells (CellView<T>).
 * Manages Cells and auto sizes to fit region.
 * 
 * @param <T> occupant type
 */
public class Board<T> extends Region {
    private final int rows;
    private final int cols;
    private final double gap = 1.0;
    private final Cell<T>[] cells;
    private final CellView<T>[] views;
    private final ObjectProperty<BoardHandler<T>> handler = new SimpleObjectProperty<>(new DefaultBoardHandler<>());
    private final BiFunction<Integer,Integer, Cell<T>> cellFactory;
    private final boolean singleSelection = true;
    private Cell<T> selectedCell = null;

    @SuppressWarnings("unchecked")
    public Board(int rows, int cols, BiFunction<Integer,Integer, Cell<T>> cellFactory) {
        if (rows <= 0 || cols <= 0) throw new IllegalArgumentException("Value for rows and cols must be positive");
        this.rows=rows;
        this.cols=cols;
        this.cellFactory = cellFactory == null ? (x,y) -> new Cell<>(x,y) : cellFactory;

        this.cells = (Cell<T>[]) new Cell[rows * cols];
        this.views = (CellView<T>[]) new CellView[rows * cols];

        createGrid();
    }

    private void createGrid() {
        final int total = rows * cols;
        for (int i = 0; i < total; i++) {
            int x = i % cols;
            int y = i / cols;
            Cell<T> c = cellFactory.apply(x, y);
            cells[i] = c;

            CellView<T> view = new CellView<>(c);
            views[i] = view;

            setupViewInteractions(view, c);
            getChildren().add(view);
        }
    }

    private void setupViewInteractions(CellView<T> view, Cell<T> cell) {
        view.setOnMouseEntered(e -> {
            handler.get().onHoverEnter(cell);
            view.setHoverVisual(true);
        });
        view.setOnMouseExited(e -> {
            handler.get().onHoverExit(cell);
            view.setHoverVisual(false);
        });
        view.setOnMouseClicked(e -> {
            MouseButton btn = e.getButton();
            int clicks = e.getClickCount();
            handler.get().onClick(cell, btn, clicks);
            if (singleSelection && btn == MouseButton.PRIMARY) {
                select(cell);
            }
        });
    }

    private void select(Cell<T> cell) {
        if (selectedCell == cell) return;
        if (selectedCell != null) {
            CellView<T> prevView = getViewFor(selectedCell);
            if (prevView != null) prevView.setSelected(false);
        }
        selectedCell = cell;
        if (cell != null) {
            CellView<T> curView = getViewFor(cell);
            if (curView != null) curView.setSelected(true);
        }
    }

    public CellView<T> getViewFor(Cell<T> cell) {
        int idx = cell.getY() * cols + cell.getX();
        if (idx < 0 || idx >= views.length) return null;
        return views[idx];
    }

    public void forEachCell(Consumer<Cell<T>> consumer) {
        for (Cell<T> c : cells) {
            consumer.accept(c);
        }
    }

    public Cell<T> getCell(int x, int y) {
        if (x < 0 || x >= cols || y < 0 || y >= rows) return null;
        return cells[y * cols + x];
    }

    public void setHandler(BoardHandler<T> h) {
        handler.set(h == null ? new DefaultBoardHandler<>() : h);
    }

    public BoardHandler<T> getHandler() {
        return handler.get();
    }

    public void refreshAllCells() {
        for (CellView<T> view : views) {
            view.refresh();
        }
    }

    @Override
    protected double computePrefWidth(double height) {
        return 400;
    }

    @Override
    protected double computePrefHeight(double width) {
        return 400;
    }

    @Override
    protected void layoutChildren() {
        double w = getWidth();
        double h = getHeight();
        double cellW = (w - (cols - 1) * gap) / cols;
        double cellH = (h - (rows - 1) * gap) / rows;
        double size = Math.min(cellW, cellH);

        for (int i = 0; i < views.length; i++) {
            CellView<T> view = views[i];
            Cell<T> c = cells[i];
            double vx = c.getX() * (size + gap);
            double vy = c.getY() * (size + gap);
            view.resizeRelocate(vx, vy, size, size);
        }
    }
}
