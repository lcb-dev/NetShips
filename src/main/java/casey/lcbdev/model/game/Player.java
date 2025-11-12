package casey.lcbdev.model.game;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

import casey.lcbdev.model.ships.Ship;
import casey.lcbdev.model.ships.ShipRegistry;

public class Player {
    private final Map<String, Ship> placedShips = new LinkedHashMap<>();

    public Player() {}

    /**
     * Attempt to add a ship, using a given key. 
     * @param key Ship key
     * @param ship Ship object
     * @return true on success, false if invalid or already placed.
     */
    public boolean addShip(String key, Ship ship) {
        if(key == null || ship == null) return false;
        if(!ShipRegistry.isValidKey(key)) return false;
        if(placedShips.containsKey(key)) return false;

        int expectedLen = ShipRegistry.lengthFor(key);
        if(expectedLen != ship.getLength()) return false;
        placedShips.put(key, ship);
        return true;
    }

    /**
     * Remove a placed ship.
     * @param key The ship key
     * @return Removed ship or empty if none.
     */
    public Optional<Ship> removeShip(String key) {
        if(key == null) return Optional.empty();
        return Optional.ofNullable(placedShips.remove(key));
    }

    /**
     * Check whether a ship of a given key is already placed.
     * @param key Ship key
     * @return If ship has been placed or not.
     */
    public boolean hasPlaced(String key) {
        return placedShips.containsKey(key);
    }

    /**
     * Used for ensuring ship placement maximum.
     * @return The number of placed ships.
     */
    public int placedCount() {
        return placedShips.size();
    }

    /**
     * Useful for checking if game can start - player must place all their ships.
     * @return All required ships placed?
     */
    public boolean allPlaced() {
        return placedShips.size() == ShipRegistry.SHIP_LENGTH_BY_KEY.size();
    }

    /**
     * Immutable view of placed ships.
     * @return unmodifiable map if placed ships.
     */
    public Map<String, Ship> getPlacedShips() {
        return Collections.unmodifiableMap(placedShips);
    }
}
