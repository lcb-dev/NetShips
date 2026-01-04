package casey.lcbdev.ui;

import java.util.logging.Logger;
import casey.lcbdev.util.Logging;
import casey.lcbdev.model.game.GameManager;
import casey.lcbdev.model.game.Player;
import casey.lcbdev.model.game.AIPlayer;
import casey.lcbdev.model.game.AIOpponentHandler;
import casey.lcbdev.model.board.Board;
import casey.lcbdev.model.board.ShipCell;
import casey.lcbdev.model.board.ShipPlacementHandler;
import casey.lcbdev.model.board.ShotState;
import casey.lcbdev.model.board.Cell;
import casey.lcbdev.model.ships.Destroyer;
import casey.lcbdev.model.ships.Ship;
import casey.lcbdev.model.board.DefaultBoardHandler;
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
    private static final Logger logger = Logging.getLogger(GameScreen.class);
    
    // Boards
    private Board<Ship> playerBoard;
    private Board<ShotState> opponentBoard;
    
    // UI components
    private ShipPlacementHandler placementHandler;
    private ShipSelectorPane selector;
    private final Label statusLabel = new Label();
    
    // Game state
    private final Player localPlayer = new Player();
    private Player remotePlayer;
    private GameManager gameManager;
    private boolean isPvAI;
    private boolean placementComplete = false;

    public GameScreen() {
        this(false);
    }

    public GameScreen(boolean vsAI) {
        logger.info("Constructing GameScreen (vsAI=" + vsAI + ")");
        this.isPvAI = vsAI;
        
        initPlayerBoard();
        initOpponentBoard();
        initSelector();
        
        var placedCallback = createPlacedCallback();
        createPlacementHandler(placedCallback);
        playerBoard.setHandler(placementHandler);
        
        layoutUI();
        setupKeyHandlers();
        
        this.setOnMouseClicked(e -> this.requestFocus());
        this.requestFocus();
        
        if (vsAI) {
            initAIOpponent();
        } else {
            initLocalTestOpponent();
        }
    }

    // ========== Initialization ==========

    private void initPlayerBoard() {
        logger.info("Initializing player board");
        playerBoard = new Board<>(10, 10, (x, y) -> new ShipCell(x, y));
        playerBoard.setPrefSize(600, 600);
    }

    private void initOpponentBoard() {
        logger.info("Initializing opponent board");
        opponentBoard = new Board<>(10, 10, (x, y) -> new Cell<ShotState>(x, y));
        opponentBoard.setPrefSize(600, 600);
        
        opponentBoard.forEachCell(c -> c.setOccupant(ShotState.UNKNOWN));
        
        opponentBoard.setHandler(new DefaultBoardHandler<ShotState>() {
            @Override
            public void onHoverEnter(Cell<ShotState> cell) {
                if (placementComplete && gameManager != null) {
                    statusLabel.setText("Target: " + cell.getX() + "," + cell.getY());
                }
            }

            @Override
            public void onHoverExit(Cell<ShotState> cell) {
                // Keep current status
            }

            @Override
            public void onClick(Cell<ShotState> cell, MouseButton button, int clickCount) {
                if (button == MouseButton.PRIMARY && placementComplete && gameManager != null) {
                    gameManager.handleLocalAttack(cell.getX(), cell.getY());
                }
            }
        });
    }

    private void initSelector() {
        logger.info("Initializing ship selector");
        selector = new ShipSelectorPane(new ShipSelectorPane.ShipSelectionListener() {
            @Override
            public void onShipSelected(Supplier<Ship> supplier, int length, String key) {
                if (localPlayer.hasPlaced(key)) {
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

    private void initAIOpponent() {
        logger.info("Initializing AI opponent");
        AIPlayer aiAgent = new AIPlayer(10, 10);
        aiAgent.placeAllShipsRandomly();
        remotePlayer = aiAgent.getPlayerModel();
        
        AIOpponentHandler aiHandler = new AIOpponentHandler(aiAgent);
        gameManager = new GameManager(localPlayer, remotePlayer, aiHandler);
        gameManager.setBoardUpdater(createBoardUpdater());
        
        statusLabel.setText("VS AI: Place your ships. When ready, attack the opponent.");
    }

    private void initLocalTestOpponent() {
        logger.info("Initializing local test opponent");
        remotePlayer = new Player();
        gameManager = new GameManager(localPlayer, remotePlayer, null);
        gameManager.setBoardUpdater(createBoardUpdater());
        
        statusLabel.setText("Local mode: Place your ships.");
    }

    // ========== Ship Placement ==========

    private Consumer<Ship> createPlacedCallback() {
        return placedShip -> {
            String key = mapShipToKey(placedShip);
            if (key == null) {
                logger.warning("Could not map ship to key: " + placedShip);
                return;
            }

            boolean added = localPlayer.addShip(key, placedShip);
            if (!added) {
                Platform.runLater(() ->
                    statusLabel.setText("Failed to record placement for: " + key)
                );
                return;
            }

            Platform.runLater(() -> {
                selector.markPlaced(key);
                selector.clearSelection();
                if (placementHandler != null) placementHandler.setShipSupplier(null, 0);
                statusLabel.setText("Placed: " + placedShip.getName());
            });

            if (localPlayer.allPlaced()) {
                onPlacementComplete();
            }
        };
    }

    private void onPlacementComplete() {
        logger.info("All ships placed");
        placementComplete = true;
        
        if (placementHandler != null) {
            placementHandler.setShipSupplier(null, 0);
        }
        
        playerBoard.setHandler(new DefaultBoardHandler<>());
        
        Platform.runLater(() -> {
            selector.disableAll();
            statusLabel.setText(isPvAI ? "All ships placed! Attack the AI." : "All ships placed! Ready.");
        });
    }

    private void createPlacementHandler(Consumer<Ship> placedCallback) {
        logger.info("Creating placement handler");
        placementHandler = new ShipPlacementHandler(
            playerBoard,
            () -> new Destroyer(null),
            2,
            placedCallback
        );
    }

    // ========== UI Updates ==========

    private GameManager.BoardUpdater createBoardUpdater() {
        return new GameManager.BoardUpdater() {
            @Override
            public void updateLocalShipCell(int x, int y) {
                Platform.runLater(() -> {
                    ShipCell cell = (ShipCell) playerBoard.getCell(x, y);
                    if (cell == null) return;
                    
                    // If it's an empty cell (miss), mark it visually
                    // If it's an occupied cell (hit), Ship.markHit() already handled it
                    if (!cell.isOccupied()) {
                        cell.setIncomingShot(ShotState.MISS);
                    }
                    playerBoard.refreshAllCells();
                });
            }

            @Override
            public void updateOpponentShotCell(int x, int y, ShotState state) {
                Platform.runLater(() -> {
                    Cell<ShotState> cell = opponentBoard.getCell(x, y);
                    if (cell != null) {
                        cell.setOccupant(state);
                        opponentBoard.refreshAllCells();
                    }
                });
            }

            @Override
            public void showStatus(String message) {
                Platform.runLater(() -> statusLabel.setText(message));
            }

            @Override
            public void onGameOver(boolean localPlayerWon) {
                Platform.runLater(() -> {
                    statusLabel.setText(localPlayerWon ? "You win!" : "You lose!");
                    opponentBoard.setHandler(new DefaultBoardHandler<>());
                });
            }
        };
    }

    // ========== Layout ==========

    private void layoutUI() {
        logger.info("Building layout UI");

        HBox center = new HBox(10);
        center.setPadding(new Insets(8));

        center.getChildren().add(playerBoard);
        HBox.setHgrow(playerBoard, Priority.ALWAYS);

        center.getChildren().add(opponentBoard);
        HBox.setHgrow(opponentBoard, Priority.ALWAYS);

        center.getChildren().add(selector);
        selector.setPrefWidth(200);

        this.setCenter(center);
        this.setBottom(statusLabel);
        BorderPane.setMargin(statusLabel, new Insets(6));

        this.requestFocus();
    }

    // ========== Key Handlers ==========

    private void setupKeyHandlers() {
        logger.info("Setting up key handlers");
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

    // ========== Utilities ==========

    private String mapShipToKey(Ship s) {
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
        logger.info("Create scene with dimensions: W=" + w + " H=" + h);
        Scene scene = new Scene(this, w, h);
        this.requestFocus();
        return scene;
    }

    public Board<?> getBoard() {
        return this.playerBoard;
    }
}