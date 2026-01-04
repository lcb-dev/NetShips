package casey.lcbdev.model.game;

import casey.lcbdev.model.ships.Ship;
import casey.lcbdev.model.board.ShipCell;
import casey.lcbdev.util.Logging;

import java.util.logging.Logger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Core match logic controller. Handles turn management, attack resolution, and win conditions.
 * This class is completely UI-agnostic and can be used for local or (soon) online games.
 */
public class MatchController {
    private static final Logger logger = Logging.getLogger(MatchController.class);

    private final Player playerA;
    private final Player playerB;
    private Player currentTurn;

    private final Map<Player, Set<String>> attackedCoordinates = new HashMap<>();

    public MatchController(Player a, Player b, Player startingPlayer) {
        this.playerA = Objects.requireNonNull(a, "Player A cannot be null");
        this.playerB = Objects.requireNonNull(b, "Player B cannot be null");
        this.currentTurn = startingPlayer != null ? startingPlayer : a;
        
        attackedCoordinates.put(playerA, new HashSet<>());
        attackedCoordinates.put(playerB, new HashSet<>());
        
        logger.info("MatchController initialized. Starting player: " + 
                   (currentTurn == playerA ? "A" : "B"));
    }

    public Player getCurrentPlayer() {
        return currentTurn;
    }

    public boolean isTurn(Player player) {
        return player == currentTurn;
    }

    public void endTurn() {
        Player previous = currentTurn;
        currentTurn = (currentTurn == playerA) ? playerB : playerA;
        logger.fine("Turn ended. Previous: " + (previous == playerA ? "A" : "B") + 
                   ", Current: " + (currentTurn == playerA ? "A" : "B"));
    }

    /**
     * Process an attack from one player against another.
     * 
     * @param attacker The player making the attack
     * @param defender The player being attacked  
     * @param x X coordinate
     * @param y Y coordinate
     * @return Result of the attack
     */
    public AttackResult attack(Player attacker, Player defender, int x, int y) {
        if (attacker == null || defender == null) {
            logger.warning("Attack called with null player");
            return AttackResult.invalid();
        }
        
        if (!isTurn(attacker)) {
            logger.warning("Attack attempted out of turn");
            return AttackResult.invalid();
        }
        
        if (!isValidCoordinate(x, y)) {
            logger.warning("Invalid coordinates: " + x + "," + y);
            return AttackResult.invalid();
        }

        Set<String> defenderAttacked = attackedCoordinates.get(defender);
        if (defenderAttacked == null) {
            defenderAttacked = new HashSet<>();
            attackedCoordinates.put(defender, defenderAttacked);
        }
        
        String coord = coordToString(x, y);
        if (defenderAttacked.contains(coord)) {
            logger.fine("Coordinate already attacked: " + coord);
            return AttackResult.already();
        }

        defenderAttacked.add(coord);

        AttackResult result = resolveAttack(defender, x, y);
        logger.info("Attack at " + coord + " -> " + result.type + 
                   (result.shipKey != null ? " (" + result.shipKey + ")" : ""));
        
        return result;
    }

    private AttackResult resolveAttack(Player defender, int x, int y) {
        Map<String, Ship> placedShips = defender.getPlacedShips();
        
        for (Map.Entry<String, Ship> entry : placedShips.entrySet()) {
            String shipKey = entry.getKey();
            Ship ship = entry.getValue();
            ShipCell[] cells = ship.getCellsOccupied();
            
            if (cells == null) continue;
            
            for (ShipCell cell : cells) {
                if (cell == null) continue;
                
                if (cell.getX() == x && cell.getY() == y) {
                    if (cell.isHit()) {
                        logger.warning("Cell already marked as hit: " + x + "," + y);
                        return AttackResult.already();
                    }
                    
                    boolean marked = ship.markHit(cell);
                    if (!marked) {
                        logger.warning("Failed to mark hit on ship cell");
                    }
                    
                    if (ship.isSunk()) {
                        return AttackResult.sunk(shipKey);
                    } else {
                        return AttackResult.hit(shipKey);
                    }
                }
            }
        }

        return AttackResult.miss();
    }

    public boolean isAllSunk(Player player) {
        Map<String, Ship> ships = player.getPlacedShips();
        
        if (ships.isEmpty()) {
            return false; // No ships placed yet
        }
        
        for (Ship ship : ships.values()) {
            if (!ship.isSunk()) {
                return false;
            }
        }
        
        return true;
    }

    public boolean isGameOver() {
        return isAllSunk(playerA) || isAllSunk(playerB);
    }

    public Player getWinner() {
        if (!isGameOver()) return null;
        return isAllSunk(playerA) ? playerB : playerA;
    }

    public void reset(Player startingPlayer) {
        attackedCoordinates.get(playerA).clear();
        attackedCoordinates.get(playerB).clear();
        currentTurn = startingPlayer != null ? startingPlayer : playerA;
        logger.info("Match reset. Starting player: " + (currentTurn == playerA ? "A" : "B"));
    }

    // Utility
    private boolean isValidCoordinate(int x, int y) {
        return x >= 0 && x < 10 && y >= 0 && y < 10;
    }

    private String coordToString(int x, int y) {
        return x + "," + y;
    }

    public Player getPlayerA() {
        return playerA;
    }

    public Player getPlayerB() {
        return playerB;
    }

    public Set<String> getAttackedCoordinates(Player player) {
        return new HashSet<>(attackedCoordinates.getOrDefault(player, new HashSet<>()));
    }
}