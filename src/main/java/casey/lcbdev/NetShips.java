package casey.lcbdev;

import casey.lcbdev.model.board.Board;
import casey.lcbdev.model.board.BoardHandler;
import casey.lcbdev.model.ships.Ship;
import casey.lcbdev.model.board.Cell;
import casey.lcbdev.model.board.ShipCell;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.HBox;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseButton;
import javafx.scene.Scene;


public class NetShips extends Application {

    private final int BOARD_ROWS = 10;
    private final int BOARD_COLS = 10;

    @Override
    public void start(Stage primaryStage) {
        Button startBtn = new Button("Start");
        startBtn.setOnAction(_ -> {
            System.out.println("Starting!");
            showGame(primaryStage);
        });

        Button quitBtn = new Button("Quit");
        quitBtn.setOnAction(_ -> {
            System.out.println("Quitting");
            Platform.exit();
        });

        VBox menuRoot = new VBox(10); 
        menuRoot.setPadding(new Insets(20));
        menuRoot.setAlignment(Pos.CENTER); 
        menuRoot.getChildren().addAll(startBtn, quitBtn);

        Scene menuScene = new Scene(menuRoot, 800, 600);

        primaryStage.setTitle("NetShips");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void showGame(Stage stage) {
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> backToMenu(stage));

        Label title = new Label("NetShips - New Game");
        HBox topBar = new HBox(10, backButton, title);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8));

        Board<Ship> board = getShipBoard(title);

        javafx.scene.layout.BorderPane root = new javafx.scene.layout.BorderPane();
        root.setTop(topBar);
        root.setCenter(board);

        Scene gameScene = new Scene(root, 800, 600);
        stage.setScene(gameScene);
    }

    private Board<Ship> getShipBoard(Label title) {
        Board<Ship> board = new Board<>(BOARD_ROWS, BOARD_COLS, (x,y) -> (Cell<Ship>) new ShipCell(x,y));
        board.setPrefSize(600, 600);
        board.setHandler(new BoardHandler<Ship>() {
            @Override
            public void onHoverEnter(Cell<Ship> cell) {
                title.setText("Hover: " + cell.getX() + "," + cell.getY());
            }

            @Override
            public void onHoverExit(Cell<Ship> cell) {
                title.setText("NetShips - New Game");
            }

            @Override
            public void onClick(Cell<Ship> cell, MouseButton button, int clickCount) {
                if (button == MouseButton.PRIMARY) {
                    System.out.println("Clicked cell: " + cell.getX() + "," + cell.getY());
                }
            }
        });
        return board;
    }

    private void backToMenu(Stage stage) {
        Button startBtn = new Button("Start");
        startBtn.setOnAction(e -> showGame(stage));

        Button quitBtn = new Button("Quit");
        quitBtn.setOnAction(e -> Platform.exit());

        // Use VBox for vertical layout with spacing
        VBox menuRoot = new VBox(10); // 10 pixels spacing
        menuRoot.setPadding(new Insets(20));
        menuRoot.setAlignment(Pos.CENTER); // center buttons
        menuRoot.getChildren().addAll(startBtn, quitBtn);

        Scene menuScene = new Scene(menuRoot, 800, 600);
        stage.setScene(menuScene);
    }


    static void main(String[] args) {
        launch(args);
    }
}
