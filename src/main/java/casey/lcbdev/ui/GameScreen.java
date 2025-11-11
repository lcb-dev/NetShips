package casey.lcbdev.ui;

import casey.lcbdev.model.board.Board;
import casey.lcbdev.model.board.ShipCell;
import casey.lcbdev.model.board.ShipPlacementHandler;
import casey.lcbdev.model.ships.Destroyer;
import casey.lcbdev.model.ships.Ship;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.Scene;
import javafx.scene.control.Label;

import java.util.function.Supplier;
public class GameScreen extends BorderPane {
    private final Board<Ship> board;
    private ShipPlacementHandler placementHandler;
    private final ShipSelectorPane selector;
    private final Label statusLabel = new Label("Place your ships");

    public GameScreen() {
        board = new Board<>(10, 10, (x, y) -> new ShipCell(x, y));
        board.setPrefSize(600, 600);

        selector = new ShipSelectorPane(new ShipSelectorPane.ShipSelectionListener() {
            @Override
            public void onShipSelected(Supplier<Ship> supplier, int length, String key) {
                if (placementHandler != null) {
                    placementHandler.setShipSupplier(supplier, length);
                    statusLabel.setText("Selected: " + key);
                }
            }

            @Override
            public void onShipDeselected() {
                if (placementHandler != null) {
                    placementHandler.setShipSupplier(null, 0);
                }
                statusLabel.setText("Deselected");
            }

            @Override
            public void onShipPlaced(String key) {
                selector.markPlaced(key);
                statusLabel.setText("Placed: " + key);
            }
        }, () -> {
            if (placementHandler != null) {
                placementHandler.toggleOrientation();
                statusLabel.setText("Orientation: " + placementHandler.getOrientation());
            }
        });

        java.util.function.Consumer<Ship> placedCallback = placedShip -> {
            String key = mapShipToKey(placedShip);
            if (key != null) {
                javafx.application.Platform.runLater(() -> {
                    selector.markPlaced(key);
                    statusLabel.setText("Placed: " + placedShip.getName());
                });
            }
        };

        placementHandler = new ShipPlacementHandler(
                board,
                () -> new Destroyer(null),
                2,
                placedCallback
        );

        board.setHandler(placementHandler);

        HBox center = new HBox(10);
        center.setPadding(new Insets(8));
        center.getChildren().addAll(board, selector);
        HBox.setHgrow(board, Priority.ALWAYS);
        board.setMaxSize(Region.USE_PREF_SIZE, Region.USE_PREF_SIZE);
        selector.setPrefWidth(200);

        this.setCenter(center);
        this.setBottom(statusLabel);
        BorderPane.setMargin(statusLabel, new Insets(6));

        this.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.R) {
                placementHandler.toggleOrientation();
                statusLabel.setText("Orientation: " + placementHandler.getOrientation());
            } else if (evt.getCode() == KeyCode.ESCAPE) {
                placementHandler.setShipSupplier(null, 0);
                selector.resetAll();
                statusLabel.setText("Cancelled selection");
            }
        });

        this.setOnMouseClicked(e -> this.requestFocus());
        this.requestFocus();
    }

    private String mapShipToKey(Ship s) {
        if (s == null) return null;
        String name = s.getName().toLowerCase();
        int len = s.getLength();
        if (name.contains("carrier") || len == 5) return "carrier";
        if (name.contains("battleship") || len == 4) return "battleship";
        if (name.contains("destroyer") || (len == 3 && name.contains("destroyer"))) return "destroyer";
        if (name.contains("submarine") || (len == 3 && name.contains("sub"))) return "submarine";
        if (name.contains("patrolboat") || len == 2) return "patrolboat";
        return name.replaceAll("\\s","").toLowerCase();
    }

    public Scene createScene(double w, double h) {
        Scene scene = new Scene(this, w, h);
        this.requestFocus();
        return scene;
    }
}
