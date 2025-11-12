package casey.lcbdev.ui;

import java.util.logging.Logger;
import casey.lcbdev.util.Logging;
import casey.lcbdev.model.game.Player;
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
import javafx.scene.Scene;
import javafx.scene.control.Label;
import java.util.function.Consumer;

import java.util.function.Supplier;
public class GameScreen extends BorderPane {
    private Board<Ship> board;
    private ShipPlacementHandler placementHandler;
    private ShipSelectorPane selector;
    private final Label statusLabel = new Label();
    private final Player player = new Player();
    private static final Logger logger = Logging.getLogger(GameScreen.class);

    public GameScreen() {
        logger.info("Building game screen.");
        initBoard();
        initSelector();
        var placedCallback = createPlacedCallback();
        createPlacementHandler(placedCallback);
        wireHandlerToBoard();
        layoutUI();
        setupKeyHandlers();

        this.setOnMouseClicked(e -> this.requestFocus());
        this.requestFocus();
    }

    // ---------- init helpers ----------

    private void initBoard() {
        logger.info("Board init.");
        board = new Board<>(10, 10, (x, y) -> new ShipCell(x, y));
        board.setPrefSize(600, 600);
    }

    private void initSelector() {
        logger.info("Selector init");
        selector = new ShipSelectorPane(new ShipSelectorPane.ShipSelectionListener() {
            @Override
            public void onShipSelected(Supplier<Ship> supplier, int length, String key) {
                if (player.hasPlaced(key)) {
                    statusLabel.setText("Already placed: " + key);
                    return;
                }
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
    }

    private Consumer<Ship> createPlacedCallback() {
        return placedShip -> {
            String key = mapShipToKey(placedShip);
            if (key == null) return;

            boolean added = player.addShip(key, placedShip);
            if (!added) {
                javafx.application.Platform.runLater(() ->
                    statusLabel.setText("Failed to record placement for: " + key)
                );
                return;
            }

            javafx.application.Platform.runLater(() -> {
                selector.markPlaced(key);

                selector.clearSelection();

                if (placementHandler != null) placementHandler.setShipSupplier(null, 0);

                statusLabel.setText("Placed: " + placedShip.getName());
            });

            if (player.allPlaced()) {
                if (placementHandler != null) placementHandler.setShipSupplier(null, 0);

                board.setHandler(new casey.lcbdev.model.board.DefaultBoardHandler<>());

                javafx.application.Platform.runLater(() -> {
                    selector.disableAll();
                    statusLabel.setText("All ships placed! Ready.");
                });
            }
        };
    }

    private void createPlacementHandler(Consumer<Ship> placedCallback) {
        logger.info("Placement callback handler creation for: " + placedCallback.toString());
        placementHandler = new ShipPlacementHandler(
                board,
                () -> new Destroyer(null),
                2,
                placedCallback
        );
    }

    private void wireHandlerToBoard() {
        board.setHandler(placementHandler);
    }

    // ---------- layout ----------

    private void layoutUI() {
        logger.info("Building layout UI.");
        HBox center = new HBox(10);
        center.setPadding(new Insets(8));
        center.getChildren().addAll(board, selector);
        HBox.setHgrow(board, Priority.ALWAYS);
        selector.setPrefWidth(200);

        this.setCenter(center);
        this.setBottom(statusLabel);
        BorderPane.setMargin(statusLabel, new Insets(6));
    }

    // ---------- key handlers ----------

    private void setupKeyHandlers() {
        logger.info("Setting up key handlers.");
        this.setOnKeyPressed(evt -> {
            if (evt.getCode() == KeyCode.R) {
                if (placementHandler != null) {
                    placementHandler.toggleOrientation();
                    statusLabel.setText("Orientation: " + placementHandler.getOrientation());
                }
            } else if (evt.getCode() == KeyCode.ESCAPE) {
                if (placementHandler != null) placementHandler.setShipSupplier(null, 0);
                selector.resetAll();
                statusLabel.setText("Cancelled selection");
            }
        });
    }

    // ---------- small helper(s) ----------

    /**
     * Heuristic mapping from Ship -> selector key used by ShipSelectorPane
     * (keeps previous mapping logic centralised).
     */
    private String mapShipToKey(Ship s) {
        logger.info("Mapping ship to key. SHIP = " + s.toString());
        if (s == null) return null;
        String name = s.getName().toLowerCase();
        int len = s.getLength();
        if (name.contains("carrier") || len == 5) return "carrier";
        if (name.contains("battleship") || len == 4) return "battleship";
        if (name.contains("destroyer") || (len == 3 && name.contains("destroyer"))) return "destroyer";
        if (name.contains("submarine") || (len == 3 && name.contains("sub"))) return "submarine";
        if (name.contains("patrol") || len == 2) return "patrolboat";
        return name.replaceAll("\\s", "").toLowerCase();
    }

    public Scene createScene(double w, double h) {
        logger.info("Create scene with dimensions: W="+w+" H="+h);
        Scene scene = new Scene(this, w, h);
        this.requestFocus();
        return scene;
    }

    public Board getBoard() {
        return this.board;
    }
}