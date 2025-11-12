package casey.lcbdev.model.game;

import casey.lcbdev.model.ships.Ship;
import casey.lcbdev.model.board.ShipCell;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class MatchController {
    private static final Logger logger = Logger.getLogger(MatchController.class.getName());

    private final Player playerA;
    private final Player playerB;
    private Player current; 

    private final Map<Player, Set<String>> attacked = new HashMap<>();

    public MatchController(Player a, Player b, Player starting) {
        this.playerA = Objects.requireNonNull(a);
        this.playerB = Objects.requireNonNull(b);
        this.current = starting == null ? a : starting;
        attacked.put(playerA, new HashSet<>());
        attacked.put(playerB, new HashSet<>());
    }

    public Player getCurrentPlayer() { return current; }

    public boolean isTurn(Player p) { return p == current; }

    public void endTurn() {
        current = (current == playerA) ? playerB : playerA;
    }

    public AttackResult attack(Player attacker, Player defender, int x, int y) {
        if (attacker == null || defender == null) return AttackResult.invalid();
        if (!isTurn(attacker)) {
            logger.warning("Attack attempted out of turn by attacker");
            return AttackResult.invalid();
        }
        if (x < 0 || y < 0 || x >= 100 || y >= 100) {
            return AttackResult.invalid();
        }

        Set<String> defSet = attacked.get(defender);
        if (defSet == null) {
            defSet = new HashSet<>();
            attacked.put(defender, defSet);
        }
        String coord = x + "," + y;
        if (defSet.contains(coord)) {
            logger.info("Coordinate already attacked: " + coord);
            return AttackResult.already();
        }

        defSet.add(coord);

        Map<String, Ship> placed = defender.getPlacedShips();
        for (Map.Entry<String, Ship> e : placed.entrySet()) {
            String key = e.getKey();
            Ship ship = e.getValue();
            ShipCell[] parts = ship.getCellsOccupied();
            if (parts == null) continue;
            for (ShipCell sc : parts) {
                if (sc == null) continue;
                if (sc.getX() == x && sc.getY() == y) {
                    if (sc.isHit()) {
                        logger.info("Ship cell already hit at " + coord);
                        return AttackResult.already();
                    }
                    boolean marked = ship.markHit(sc); 
                    if (!marked) {
                        logger.warning("Failed to mark hit on ship at " + coord);
                    }
                    boolean sunk = ship.isSunk();
                    logger.info("Attack result at " + coord + " -> HIT (ship=" + key + "), sunk=" + sunk);
                    if (sunk) {
                        return AttackResult.sunk(key);
                    } else {
                        return AttackResult.hit(key);
                    }
                }
            }
        }

        logger.info("Attack result at " + coord + " -> MISS");
        return AttackResult.miss();
    }

    public boolean isAllSunk(Player defender) {
        Map<String, Ship> placed = defender.getPlacedShips();
        for (Ship s : placed.values()) {
            if (!s.isSunk()) return false;
        }
        return true;
    }
}
