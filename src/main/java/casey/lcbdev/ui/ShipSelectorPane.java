package casey.lcbdev.ui;

import casey.lcbdev.model.ships.*;
import java.util.logging.Logger;
import casey.lcbdev.util.Logging;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ShipSelectorPane extends VBox {
    private static final Logger logger = Logging.getLogger(ShipSelectorPane.class);
    public static class ShipSpec {
        public final String name;
        public final int length;
        public final Supplier<Ship> supplier;
        public ShipSpec(String name, int length, Supplier<Ship> supplier) {
            this.name=name;
            this.length=length;
            this.supplier=supplier;
        }
    }

    private final Map<ToggleButton, String> buttonToKey = new LinkedHashMap<>();
    private final Map<String, ToggleButton> keyToButton = new LinkedHashMap<>();
    private final ToggleGroup toggleGroup = new ToggleGroup();
    private final Button rotateBtn = new Button("Rotate (R)");
    private final Label infoLabel = new Label("Select a ship");
    public interface ShipSelectionListener {
        void onShipSelected(Supplier<Ship> supplier, int length, String key);
        void onShipDeselected();
        void onShipPlaced(String key);
    }

    public ShipSelectorPane(ShipSelectionListener listener, Runnable onRotate) {
        setSpacing(8);
        setPadding(new Insets(8));
        getStyleClass().add("ship-selector");

        Map<String, ShipSpec> specs = new LinkedHashMap<>();
        specs.put("carrier", new ShipSpec("Carrier", 5, () -> new Carrier(null)));
        specs.put("battleship", new ShipSpec("Battleship", 4, () -> new Battleship(null)));
        specs.put("destroyer", new ShipSpec("Destroyer", 3, () -> new Destroyer(null)));
        specs.put("submarine", new ShipSpec("Submarine", 3, () -> new Submarine(null)));
        specs.put("patrolboat", new ShipSpec("Patrol boat", 2, () -> new PatrolBoat(null)));   

        for (Map.Entry<String, ShipSpec> e : specs.entrySet()) {
            String key = e.getKey();
            ShipSpec s = e.getValue();
            ToggleButton tb = new ToggleButton(s.name + " (" + s.length + ")");
            tb.setToggleGroup(toggleGroup);
            tb.setMaxWidth(Double.MAX_VALUE);
            tb.setUserData(key);
            getChildren().add(tb);

            buttonToKey.put(tb, key);
            keyToButton.put(key, tb);

            tb.setOnAction(event -> {
                if(tb.isSelected()) {
                    String placementText = "Placing: " + s.name + " (" + s.length + ")";
                    infoLabel.setText(placementText);
                    logger.info(placementText);
                    listener.onShipSelected(s.supplier, s.length, key);
                } else {
                    infoLabel.setText("Select a ship");
                    listener.onShipDeselected();
                }
            });
        }

        rotateBtn.setOnAction(event -> {
            if(onRotate != null) onRotate.run();
        });
        getChildren().addAll(rotateBtn, infoLabel);
    }

    public void markPlaced(String key) {
        ToggleButton tb = keyToButton.get(key);
        if (tb != null) {
            tb.setDisable(true);
            tb.setSelected(false);
        }
    }

    public void resetAll() {
        logger.info("Resetting toggle buttons.");
        for (ToggleButton tb : buttonToKey.keySet()) {
            tb.setDisable(false);
            tb.setSelected(false);
        }
        infoLabel.setText("Select a ship");
    }

    public void disableAll() {
        logger.info("Disabling toggle buttons.");
        for (ToggleButton tb : buttonToKey.keySet()) {
            tb.setDisable(true);
        }
        rotateBtn.setDisable(true);
        infoLabel.setText("Placement complete.");
    }

    public void enableAll(){
        logger.info("Enabling toggle buttons.");
        for (ToggleButton tb : buttonToKey.keySet()) {
            tb.setDisable(false);
            tb.setSelected(false);
        }
        rotateBtn.setDisable(false);
        infoLabel.setText("Select a ship to place.");
    }

    public void clearSelection() {
        toggleGroup.selectToggle(null);
        infoLabel.setText("Select a ship to place.");
    }
}
