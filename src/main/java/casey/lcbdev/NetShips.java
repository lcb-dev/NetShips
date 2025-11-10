package casey.lcbdev;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.stage.Stage;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Button;
import javafx.scene.Scene;

public class NetShips extends Application {

    @Override
    public void start(Stage primaryStage) {
        Button startBtn = new Button();
        startBtn.setText("Start");
        startBtn.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent event) {
                System.out.println("Starting!");
            }
        });

        StackPane root = new StackPane();
        root.getChildren().add(startBtn);

        Scene scene = new Scene(root, 800, 600);

        primaryStage.setTitle("casey.lcbdev.NetShips");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
