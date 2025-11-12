package casey.lcbdev.model.ships;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ShipRegistry {
    public static final Map<String, Integer> SHIP_LENGTH_BY_KEY;
    static {
        Map<String,Integer> m = new LinkedHashMap<>();
        m.put("carrier", 5);
        m.put("battleship", 4);
        m.put("destroyer", 3);
        m.put("submarine", 3);
        m.put("patrolboat", 2);
        SHIP_LENGTH_BY_KEY = Collections.unmodifiableMap(m);
    }

    private ShipRegistry() {}

    public static boolean isValidKey(String key) {
        return SHIP_LENGTH_BY_KEY.containsKey(key);
    }

    public static int lengthFor(String key) {
        return SHIP_LENGTH_BY_KEY.getOrDefault(key, -1);
    }
}
