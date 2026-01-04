package casey.lcbdev.model.game;

import casey.lcbdev.util.Logging;
import java.util.logging.Logger;

/**
 * Implementation of OpponentHandler for local computer opponent.
 * Encapsulate the AI behaviour to keep it separate from other game logic.
 * 
 * Part of a big refactor to get GameManager functional and code base more workable.
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
            logger.warning("AI has no moves left!");
            return;
        }

        logger.info("AI attacking " + coord.x + "," + coord.y);
        callback.reportAttack(coord.x, coord.y);
    }

    @Override
    public boolean shouldAutoExecuteTurn() {
        return true;    // AI will always auto attack on its turn, it controls itself. :)
    }
}
