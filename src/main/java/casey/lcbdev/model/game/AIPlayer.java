package casey.lcbdev.model.game;

import casey.lcbdev.model.board.ShipCell;
import casey.lcbdev.model.ships.*;
import java.util.*;

/**
 * Simple AI for local testing: random non-overlapping ship placement and random valid attacks.
 */
public class AIPlayer {
    private final Player aiModel = new Player();
    private final int rows;
    private final int cols;
    private final Random rnd = new Random();

    private final ArrayDeque<Coord> availableAttacks = new ArrayDeque<>();

    public AIPlayer(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        initAvailableAttacks();
    }

    public Player getPlayerModel() { return aiModel; }

    private void initAvailableAttacks() {
        List<Coord> coords = new ArrayList<>();
        for (int x = 0; x < cols; x++) {
            for (int y = 0; y < rows; y++) {
                coords.add(new Coord(x,y));
            }
        }
        Collections.shuffle(coords, rnd);
        availableAttacks.addAll(coords);
    }

    public Coord pickNextAttack() {
        return availableAttacks.pollFirst();
    }

    public void placeAllShipsRandomly() {
        Set<String> occupied = new HashSet<>();

        Map<String,Integer> shipSpec = Map.of(
            "carrier", 5,
            "battleship", 4,
            "destroyer", 3,
            "submarine", 3,
            "patrolboat", 2
        );

        for (Map.Entry<String,Integer> e : shipSpec.entrySet()) {
            String key = e.getKey();
            int len = e.getValue();
            boolean placed = false;
            int attempts = 0;
            while (!placed && attempts < 1000) {
                attempts++;
                boolean horiz = rnd.nextBoolean();
                int sx = horiz ? rnd.nextInt(cols - len + 1) : rnd.nextInt(cols);
                int sy = horiz ? rnd.nextInt(rows) : rnd.nextInt(rows - len + 1);

                List<Coord> candidate = new ArrayList<>();
                for (int i = 0; i < len; i++) {
                    int cx = horiz ? sx + i : sx;
                    int cy = horiz ? sy : sy + i;
                    candidate.add(new Coord(cx, cy));
                }

                boolean ok = true;
                for (Coord c : candidate) {
                    if (occupied.contains(c.x + "," + c.y)) { ok = false; break; }
                }
                if (!ok) continue;

                ShipCell[] cells = new ShipCell[len];
                for (int i = 0; i < candidate.size(); i++) {
                    Coord c = candidate.get(i);
                    cells[i] = new ShipCell(c.x, c.y);
                }

                Ship ship = shipForKey(key, cells);
                if (ship == null) continue; 

                boolean added = aiModel.addShip(key, ship);
                if (!added) continue;

                for (Coord c : candidate) occupied.add(c.x + "," + c.y);

                placed = true;
            }
            if (!placed) {
                throw new IllegalStateException("Failed to place ship " + key + " after many attempts");
            }
        }
    }

    private Ship shipForKey(String key, ShipCell[] cells) {
        return switch (key) {
            case "carrier" -> new Carrier(cells);
            case "battleship" -> new Battleship(cells);
            case "destroyer" -> new Destroyer(cells);
            case "submarine" -> new Submarine(cells);
            case "patrolboat" -> new PatrolBoat(cells);
            default -> null;
        };
    }

    public static final class Coord {
        public final int x, y;
        public Coord(int x, int y) { this.x = x; this.y = y; }
    }
}
