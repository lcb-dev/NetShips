package casey.lcbdev.model.board;

import casey.lcbdev.model.ships.Ship;
import javafx.scene.input.MouseButton;

import java.util.List;
import java.util.ArrayList;

import java.util.function.Supplier;
import java.util.function.Consumer;

public class ShipPlacementHandler implements BoardHandler<Ship> {
    private final Board<Ship> board;
    private  Supplier<Ship> shipFactory;
    private final Consumer<Ship> placedCallback;
    private  int shipLength;

    private Orientation orientation = Orientation.HORIZONTAL;
    private List<Cell<Ship>> lastGhostCells = new ArrayList<>();

    public ShipPlacementHandler(Board<Ship> board, Supplier<Ship> shipFactory,int shipLength, Consumer<Ship> placedCallback ) {
        this.board = board;
        this.shipFactory = shipFactory;
        this.placedCallback = placedCallback;
        if(shipLength <= 0) throw new IllegalArgumentException("Ship length must be greater than 0");
        this.shipLength = shipLength;
    }

    public void setShipSupplier(Supplier<Ship> supplier, int length) {
        this.shipFactory = supplier;
        this.shipLength = length;
        clearGhost();
    }

    private void showGhostAt(Cell<Ship> start) {
        clearGhost();

        List<Cell<Ship>> candidate = computeCandidateCells(start);
        boolean valid = isCandidateValid(candidate);

        for(Cell<Ship> c : candidate) {
            CellView<Ship> view = board.getViewFor(c);
            if(view != null) {
                view.setGhostVisual(true, valid);
            }
        }
        lastGhostCells = candidate;
    }

    private void clearGhost() {
        for (Cell<Ship> c : lastGhostCells) {
            CellView<Ship> view = board.getViewFor(c);
            if (view != null) {
                view.clearGhost();
            }
        }
        lastGhostCells.clear();
    }

    private List<Cell<Ship>> computeCandidateCells(Cell<Ship> start) {
        List<Cell<Ship>> out = new ArrayList<>();
        if (start == null) return out;

        int sx = start.getX();
        int sy = start.getY();

        if (orientation == Orientation.HORIZONTAL) {
            for (int i = 0; i < shipLength; i++) {
                Cell<Ship> c = board.getCell(sx + i, sy);
                if (c == null) break;
                out.add(c);
            }
        } else { 
            for (int i = 0; i < shipLength; i++) {
                Cell<Ship> c = board.getCell(sx, sy + i);
                if (c == null) break;
                out.add(c);
            }
        }
        return out;
    }

    private boolean isCandidateValid(List<Cell<Ship>> candidate) {
        if (candidate.size() != shipLength) return false;
        for (Cell<Ship> c : candidate) {
            if (!c.isOccupiable() || c.isOccupied()) return false;
        }
        return true;
    }

    @Override
    public void onHoverEnter(Cell<Ship> cell) {
        showGhostAt(cell);
    }


    @Override
    public void onHoverExit(Cell<Ship> cell) {
        clearGhost();
    }


    @Override
    public void onClick(Cell<Ship> cell, MouseButton button, int clickCount) {
        if(button == MouseButton.SECONDARY) {
            orientation = orientation.toggle();
            showGhostAt(cell);
            return;
        }

        if (button == MouseButton.PRIMARY) {
            List<Cell<Ship>> candidate = computeCandidateCells(cell);
            boolean valid = isCandidateValid(candidate);
            if (valid && shipFactory != null) {
                Ship ship = shipFactory.get();
                String shipName = ship.getName();
                System.out.println(ship);
                System.out.println(shipName);
                ShipCell[] shipCells = candidate.stream()
                        .map(c -> (ShipCell)c)
                        .toArray(ShipCell[]::new);
                ship.placeOnCells(shipCells);
                board.refreshAllCells();
                if (placedCallback != null) placedCallback.accept(ship);
                clearGhost();
            }
        }
    }

    public Orientation getOrientation() { return orientation; }
    public void toggleOrientation() {
        orientation = orientation.toggle();
        clearGhost();
    }
    public void setOrientation(Orientation o) { this.orientation = o; }
}
