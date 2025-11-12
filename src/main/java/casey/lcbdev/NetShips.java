package casey.lcbdev;

import casey.lcbdev.ui.GameScreen;
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

    @Override
    public void start(Stage primaryStage) {
        Button startBtn = new Button("Start");
        startBtn.setOnAction(_ -> showGame(primaryStage));

        Button quitBtn = new Button("Quit");
        quitBtn.setOnAction(_ -> Platform.exit());

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
        Label title = new Label("NetShips - New Game");
        HBox topBar = new HBox(10, backButton, title);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(8));

        GameScreen gameScreen = new GameScreen();

        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(gameScreen);

        Scene gameScene = new Scene(root, 1800, 1600);
        stage.setScene(gameScene);

        backButton.setOnAction(e -> backToMenu(stage));

        gameScreen.requestFocus();
    }

    private void backToMenu(Stage stage) {
        Button startBtn = new Button("Start");
        startBtn.setOnAction(e -> showGame(stage));

        Button quitBtn = new Button("Quit");
        quitBtn.setOnAction(e -> Platform.exit());

        VBox menuRoot = new VBox(10);
        menuRoot.setPadding(new Insets(20));
        menuRoot.setAlignment(Pos.CENTER);
        menuRoot.getChildren().addAll(startBtn, quitBtn);

        Scene menuScene = new Scene(menuRoot, 800, 600);
        stage.setScene(menuScene);
    }

    public static void main(String[] args) {
        launch(args);
    }
}