package casey.lcbdev.ui;

import java.util.logging.Logger;
import casey.lcbdev.util.Logging;
import casey.lcbdev.model.game.MatchController;
import casey.lcbdev.model.game.Player;
import casey.lcbdev.model.board.Board;
import casey.lcbdev.model.board.ShipCell;
import casey.lcbdev.model.board.ShipPlacementHandler;
import casey.lcbdev.model.board.ShotState;
import casey.lcbdev.model.board.Cell;
import casey.lcbdev.model.ships.Destroyer;
import casey.lcbdev.model.ships.Ship;
import casey.lcbdev.model.board.DefaultBoardHandler;
import casey.lcbdev.model.game.AttackResult;
import casey.lcbdev.model.game.AIPlayer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class GameScreen extends BorderPane {
    private Board<Ship> board;
    private Board<ShotState> opponentBoard;
    private ShipPlacementHandler placementHandler;
    private ShipSelectorPane selector;
    private final Label statusLabel = new Label();
    private final Player player = new Player();
    private MatchController matchController;
    private Player localPlayer;
    private Player remotePlayer;
    private boolean isPvAI;
    private AIPlayer aiAgent;
    private static final Logger logger = Logging.getLogger(GameScreen.class);

    public GameScreen() {
        logger.info("Constructing GameScreen");

        initBoard();
        initSelector();
        var placedCallback = createPlacedCallback();
        createPlacementHandler(placedCallback);
        wireHandlerToBoard();

        initOpponentBoard();
        layoutUI();
        setupKeyHandlers();

        this.setOnMouseClicked(e -> this.requestFocus());
        this.requestFocus();

        initMatchControllerForLocalTest();
    }

    public GameScreen(boolean vsai) {
        this();
        this.isPvAI = vsai;
        if (vsai) {
            aiAgent = new AIPlayer(10, 10);
            aiAgent.placeAllShipsRandomly();
            this.remotePlayer = aiAgent.getPlayerModel();
            initMatchControllerForLocalTest();
            statusLabel.setText("VS AI: place your ships. When ready, attack the opponent.");
        }
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

                board.setHandler(new DefaultBoardHandler<>());

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

        initOpponentBoard();

        HBox center = new HBox(10);
        center.setPadding(new Insets(8));

        center.getChildren().add(board);
        HBox.setHgrow(board, Priority.ALWAYS);

        center.getChildren().add(opponentBoard);
        HBox.setHgrow(opponentBoard, Priority.ALWAYS);

        center.getChildren().add(selector);
        selector.setPrefWidth(200);

        this.setCenter(center);

        this.setBottom(statusLabel);
        BorderPane.setMargin(statusLabel, new Insets(6));

        this.requestFocus();
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

    public Board<?> getBoard() {
        return this.board;
    }

    // ---------- Opponent ----------

    private void initMatchController() {
        localPlayer = player;
        remotePlayer = new Player();
        matchController = new MatchController(localPlayer, remotePlayer, localPlayer); 
    }

    private void initOpponentBoard() {
        opponentBoard = new Board<>(10, 10, (x, y) -> new Cell<ShotState>(x, y));
        opponentBoard.setPrefSize(600, 600);

        opponentBoard.forEachCell(c -> c.setOccupant(ShotState.UNKNOWN));

        opponentBoard.setHandler(new DefaultBoardHandler<ShotState>() {
            @Override
            public void onHoverEnter(Cell<ShotState> cell) {
                statusLabel.setText("Target: " + cell.getX() + "," + cell.getY());
            }

            @Override
            public void onHoverExit(Cell<ShotState> cell) {
                statusLabel.setText("");
            }

            @Override
            public void onClick(Cell<ShotState> cell, MouseButton button, int clickCount) {
                if (button == MouseButton.PRIMARY) {
                    requestAttack(cell.getX(), cell.getY());
                }
            }
        });
    }

    // ------------ Handling attack interactions ---------------

    private void maybePerformAIMoves() {
        if (!isPvAI || matchController == null || aiAgent == null) return;
        while (matchController.isTurn(remotePlayer) && !matchController.isAllSunk(localPlayer)) {
            AIPlayer.Coord c = aiAgent.pickNextAttack();
            if (c == null) {
                logger.warning("AI has no moves left");
                break;
            }

            AttackResult aiRes = matchController.attack(remotePlayer, localPlayer, c.x, c.y);
            ShipCell target = (ShipCell) board.getCell(c.x, c.y);
            if (target.isOccupied()) {
                target.setHit(true);
            } else {
                target.setIncomingShot(ShotState.MISS); 
            }
            logger.info("AI attacks " + c.x + "," + c.y + " -> " + aiRes.type);

            if (aiRes.type == AttackResult.Type.MISS) {
                matchController.endTurn();
            } else if (aiRes.type == AttackResult.Type.HIT || aiRes.type == AttackResult.Type.SUNK) {
                // If attacker hit, keep turn or no? (salvo)
            } else if (aiRes.type == AttackResult.Type.ALREADY || aiRes.type == AttackResult.Type.INVALID) {
                // Shouldn't happen.
            }

            if (matchController.isAllSunk(localPlayer)) {
                Platform.runLater(() -> statusLabel.setText("AI wins!"));
                break;
            }
        }

        Platform.runLater(() -> {
            board.refreshAllCells();
            opponentBoard.refreshAllCells();
        });
    }

    private void initMatchControllerForLocalTest() {
        this.localPlayer = this.player; 
        if (this.remotePlayer == null) {
            this.remotePlayer = new Player();
        }
        this.matchController = new MatchController(this.localPlayer, this.remotePlayer, this.localPlayer);
        logger.info("MatchController initialized for local testing. Current turn: local player");
    }

    private void requestAttack(int x, int y) {
        if (matchController == null) {
            statusLabel.setText("Match not initialized.");
            return;
        }

        if (!matchController.isTurn(localPlayer)) {
            statusLabel.setText("Not your turn!");
            return;
        }

        AttackResult res = matchController.attack(localPlayer, remotePlayer, x, y);
        switch (res.type) {
            case INVALID -> {
                statusLabel.setText("Invalid attack.");
                return;
            }
            case ALREADY -> {
                statusLabel.setText("Already attacked that cell.");
                return;
            }
            case MISS -> {
                Cell<ShotState> c = opponentBoard.getCell(x, y);
                c.setOccupant(ShotState.MISS);
                opponentBoard.refreshAllCells();
                statusLabel.setText("Miss at " + x + "," + y);
                matchController.endTurn();
            }
            case HIT -> {
                Cell<ShotState> c = opponentBoard.getCell(x, y);
                c.setOccupant(ShotState.HIT);
                opponentBoard.refreshAllCells();
                statusLabel.setText("Hit! (" + res.shipKey + ")");
            }
            case SUNK -> {
                Cell<ShotState> c = opponentBoard.getCell(x, y);
                c.setOccupant(ShotState.HIT);
                opponentBoard.refreshAllCells();
                statusLabel.setText("Sunk " + res.shipKey + "!");
                if (matchController.isAllSunk(remotePlayer)) {
                    statusLabel.setText("You win!");
                    opponentBoard.setHandler(new DefaultBoardHandler<>());
                }
            }
        }

        if (isPvAI && matchController != null && matchController.isTurn(remotePlayer)) {
            maybePerformAIMoves();
        }
    }
}