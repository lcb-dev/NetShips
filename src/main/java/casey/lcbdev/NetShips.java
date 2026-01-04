package casey.lcbdev;

import java.util.Arrays;
import java.util.logging.Logger;
import casey.lcbdev.ui.GameScreen;
import casey.lcbdev.util.Logging;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class NetShips extends Application {

    private static final Logger logger = Logging.getLogger(NetShips.class);

    @Override
    public void start(Stage primaryStage) {
        logger.info("Starting NetShips.");
        Button startBtn = new Button("Start");
        startBtn.setOnAction(_ -> showGame(primaryStage));

        Button pveBtn = new Button("Start vs AI");
        pveBtn.setOnAction(_ -> showGameVsAI(primaryStage));

        Button quitBtn = new Button("Quit");
        quitBtn.setOnAction(_ -> Platform.exit());

        VBox menuRoot = new VBox(10);
        menuRoot.setPadding(new Insets(20));
        menuRoot.setAlignment(Pos.CENTER);
        menuRoot.getChildren().addAll(startBtn, pveBtn, quitBtn);

        Scene menuScene = new Scene(menuRoot, 800, 600);

        primaryStage.setTitle("NetShips");
        primaryStage.setScene(menuScene);
        primaryStage.show();
    }

    private void showGame(Stage stage) {
        logger.info("Opening new game screen.");
        Button backButton = new Button("Back");
        Label title = new Label("NetShips - New Game");
        HBox topBar = new HBox(10, backButton, title);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8));

        GameScreen gameScreen = new GameScreen(false);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(gameScreen);

        Scene gameScene = new Scene(root, 1800, 1600);
        stage.setScene(gameScene);

        backButton.setOnAction(e -> {
            logger.fine("Returning to menu.");
            backToMenu(stage);
        });

        gameScreen.requestFocus();
    }

    private void showGameVsAI(Stage stage) {
        Button backButton = new Button("Back");
        backButton.setOnAction(e -> backToMenu(stage));

        Label title = new Label("NetShips - Versus AI");
        HBox topBar = new HBox(10, backButton, title);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8));

        GameScreen gameScreen = new GameScreen(true);

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(gameScreen);

        Scene gameScene = new Scene(root, 1800, 1600);
        stage.setScene(gameScene);

        Platform.runLater(gameScreen::requestFocus);
    }


    private void backToMenu(Stage stage) {
        Button startBtn = new Button("Start");
        startBtn.setOnAction(e -> showGame(stage));

        Button pveBtn = new Button("Start vs AI");
        pveBtn.setOnAction(e -> showGameVsAI(stage));

        Button quitBtn = new Button("Quit");
        quitBtn.setOnAction(e -> {
            logger.info("Shutting down.");
            Platform.exit();
        });

        VBox menuRoot = new VBox(10);
        menuRoot.setPadding(new Insets(20));
        menuRoot.setAlignment(Pos.CENTER);
        menuRoot.getChildren().addAll(startBtn, quitBtn);

        Scene menuScene = new Scene(menuRoot, 800, 600);
        stage.setScene(menuScene);
    }

    public static void main(String[] args) {
        Logging.init();
        logger.info("Launching with args: " + Arrays.toString(args));
        launch(args);
    }
}