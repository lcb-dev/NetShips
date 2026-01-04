package casey.lcbdev.model.game;

import casey.lcbdev.model.board.ShotState;
import casey.lcbdev.util.Logging;

import java.util.logging.Logger;

/**
 * Central game coordinator that manages match flow and delegates to appropriate handlers.
 * This class is UI-agnostic and can work with local AI or remote network opponents.
 */
public class GameManager {
    private static final Logger logger = Logging.getLogger(GameManager.class);

    private final MatchController matchController;
    private final Player localPlayer;
    private final Player opponentPlayer;
    private final OpponentHandler opponentHandler;
    private BoardUpdater boardUpdater;

    public interface BoardUpdater {
        void updateLocalShipCell(int x, int y);
        void updateOpponentShotCell(int x, int y, ShotState state);
        void showStatus(String message);
        void onGameOver(boolean localPlayerWon);
    }

    public interface OpponentHandler {
        /**
         * Called when it becomes the opponent's turn.
         * Implementation should execute opponent's attack(s) and call back through GameManager.
         * @param callback Use this to report opponent's attacks back to the game
         */
        void executeOpponentTurn(OpponentMoveCallback callback);
        boolean shouldAutoExecuteTurn();
    }

    public interface OpponentMoveCallback {
        void reportAttack(int x, int y);
    }

    public GameManager(Player local, Player opponent, OpponentHandler handler) {
        this.localPlayer = local;
        this.opponentPlayer = opponent;
        this.opponentHandler = handler;
        this.matchController = new MatchController(localPlayer, opponentPlayer, localPlayer);
    }

    public void setBoardUpdater(BoardUpdater updater) {
        this.boardUpdater = updater;
    }

    // Local player
    public void handleLocalAttack(int x, int y) {
        if (!matchController.isTurn(localPlayer)) {
            updateStatus("Not your turn!");
            return;
        }

        AttackResult result = matchController.attack(localPlayer, opponentPlayer, x, y);
        processAttackResult(result, x, y, true);

        // Game over?
        if (matchController.isAllSunk(opponentPlayer)) {
            if (boardUpdater != null) {
                boardUpdater.onGameOver(true);
            }
            return;
        }

        if (result.type == AttackResult.Type.MISS && 
            opponentHandler != null && 
            opponentHandler.shouldAutoExecuteTurn()) {
            triggerOpponentTurn();
        }
    }

    public void handleOpponentAttack(int x, int y) {
        if (!matchController.isTurn(opponentPlayer)) {
            logger.warning("Opponent attacked out of turn");
            return;
        }

        AttackResult result = matchController.attack(opponentPlayer, localPlayer, x, y);
        processAttackResult(result, x, y, false);

        // Game over?
        if (matchController.isAllSunk(localPlayer)) {
            if (boardUpdater != null) {
                boardUpdater.onGameOver(false);
            }
        }
    }

    private void processAttackResult(AttackResult result, int x, int y, boolean isLocalAttack) {
        switch (result.type) {
            case INVALID -> updateStatus("Invalid attack at " + x + "," + y);
            
            case ALREADY -> updateStatus("Already attacked " + x + "," + y);
            
            case MISS -> {
                if (isLocalAttack) {
                    updateOpponentBoard(x, y, ShotState.MISS);
                    updateStatus("Miss at " + x + "," + y);
                } else {
                    updateLocalBoard(x, y);
                    updateStatus("Opponent missed");
                }
                matchController.endTurn();
            }
            
            case HIT -> {
                if (isLocalAttack) {
                    updateOpponentBoard(x, y, ShotState.HIT);
                    updateStatus("Hit! (" + result.shipKey + ")");
                } else {
                    updateLocalBoard(x, y);
                    updateStatus("Opponent hit your " + result.shipKey);
                }
            }
            
            case SUNK -> {
                if (isLocalAttack) {
                    updateOpponentBoard(x, y, ShotState.HIT);
                    updateStatus("Sunk " + result.shipKey + "!");
                } else {
                    updateLocalBoard(x, y);
                    updateStatus("Opponent sunk your " + result.shipKey + "!");
                }
                matchController.endTurn();
            }
        }
    }

    private void triggerOpponentTurn() {
        if (opponentHandler == null) return;
        
        opponentHandler.executeOpponentTurn(new OpponentMoveCallback() {
            @Override
            public void reportAttack(int x, int y) {
                handleOpponentAttack(x, y);
                
                if (matchController.isTurn(opponentPlayer) && 
                    !matchController.isAllSunk(localPlayer) &&
                    opponentHandler.shouldAutoExecuteTurn()) {
                    triggerOpponentTurn();
                }
            }
        });
    }

    private void updateLocalBoard(int x, int y) {
        if (boardUpdater != null) {
            boardUpdater.updateLocalShipCell(x, y);
        }
    }

    private void updateOpponentBoard(int x, int y, ShotState state) {
        if (boardUpdater != null) {
            boardUpdater.updateOpponentShotCell(x, y, state);
        }
    }

    private void updateStatus(String message) {
        if (boardUpdater != null) {
            boardUpdater.showStatus(message);
        }
    }

    public boolean isLocalPlayerTurn() {
        return matchController.isTurn(localPlayer);
    }

    public boolean isGameOver() {
        return matchController.isAllSunk(localPlayer) || matchController.isAllSunk(opponentPlayer);
    }

    public Player getLocalPlayer() {
        return localPlayer;
    }

    public Player getOpponentPlayer() {
        return opponentPlayer;
    }
}