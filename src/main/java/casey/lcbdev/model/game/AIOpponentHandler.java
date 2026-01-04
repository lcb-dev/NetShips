package casey.lcbdev.model.game;

import casey.lcbdev.util.Logging;
import java.util.logging.Logger;

/**
 * OpponentHandler implementation for local AI opponents.
 * Encapsulates AI behavior and keeps it separate from game flow logic.
 */
public class AIOpponentHandler implements GameManager.OpponentHandler {
    private static final Logger logger = Logging.getLogger(AIOpponentHandler.class);
    private final AIPlayer aiPlayer;

    public AIOpponentHandler(AIPlayer aiPlayer) {
        this.aiPlayer = aiPlayer;
    }

    @Override
    public void executeOpponentTurn(GameManager.OpponentMoveCallback callback) {
        AIPlayer.Coord coord = aiPlayer.pickNextAttack();
        
        if (coord == null) {
            logger.warning("AI has no moves left");
            return;
        }

        logger.info("AI attacking " + coord.x + "," + coord.y);
        callback.reportAttack(coord.x, coord.y);
    }

    @Override
    public boolean shouldAutoExecuteTurn() {
        return true; // AI always auto-executes
    }
}