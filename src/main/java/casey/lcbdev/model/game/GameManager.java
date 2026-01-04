package casey.lcbdev.model.game;

import casey.lcbdev.model.board.ShotState;
import casey.lcbdev.util.Logging;
import javafx.application.Platform;

import java.util.logging.Logger;

public class GameManager {
    private static final Logger logger = Logging.getLogger(GameManager.class);

    private final MatchController matchController;
    private final Player localPlayer;
    private final Player opponentPlayer;
    private final AIPlayer aiAgent;
    private final boolean vsAI;

    private BoardUpdater boardUpdater;

    public interface BoardUpdater {
        void updatePlayerCell(int x, int y);
        void updateOpponentCell(int x, int y, ShotState state);
        void showStatus(String message);
    }

    public GameManager(Player local, Player opponent, boolean vsAI, AIPlayer ai, MatchController mc) {
        this.localPlayer = local;
        this.opponentPlayer = opponent;
        this.vsAI = vsAI;
        this.aiAgent = ai;
        this.matchController = mc;
    }

    public void setBoardUpdater(BoardUpdater updater) {
        this.boardUpdater = updater;
    }

    public void playerAttack(int x, int y) {
        if (!matchController.isTurn(localPlayer)) {
            if (boardUpdater != null) boardUpdater.showStatus("Not your turn!");
            return;
        }

        AttackResult res = matchController.attack(localPlayer, opponentPlayer, x, y);

        handleAttackResult(res, x, y, true);

        if (vsAI && matchController.isTurn(opponentPlayer)) {
            performAIMoves();
        }
    }

    private void handleAttackResult(AttackResult res, int x, int y, boolean isPlayerAttack) {
        switch (res.type) {
            case INVALID -> {
                if (boardUpdater != null) boardUpdater.showStatus("Invalid attack.");
            }
            case ALREADY -> {
                if (boardUpdater != null) boardUpdater.showStatus("Already attacked that cell.");
            }
            case MISS -> {
                if (isPlayerAttack) {
                    if (boardUpdater != null) boardUpdater.updateOpponentCell(x, y, ShotState.MISS);
                } else {
                    if (boardUpdater != null) boardUpdater.updatePlayerCell(x, y);
                }
                matchController.endTurn();
            }
            case HIT -> {
                if (isPlayerAttack) {
                    if (boardUpdater != null) boardUpdater.updateOpponentCell(x, y, ShotState.HIT);
                    if (boardUpdater != null) boardUpdater.showStatus("Hit! (" + res.shipKey + ")");
                } else {
                    if (boardUpdater != null) boardUpdater.updatePlayerCell(x, y);
                }
            }
            case SUNK -> {
                if (isPlayerAttack) {
                    if (boardUpdater != null) boardUpdater.updateOpponentCell(x, y, ShotState.HIT);
                    if (boardUpdater != null) boardUpdater.showStatus("Sunk " + res.shipKey + "!");
                    if (matchController.isAllSunk(opponentPlayer)) {
                        if (boardUpdater != null) boardUpdater.showStatus("You win!");
                    }
                } else {
                    if (boardUpdater != null) boardUpdater.updatePlayerCell(x, y);
                    if (matchController.isAllSunk(localPlayer)) {
                        if (boardUpdater != null) boardUpdater.showStatus("AI wins!");
                    }
                }
                matchController.endTurn();
            }
        }
    }

    private void performAIMoves() {
        while (vsAI && matchController.isTurn(opponentPlayer) && !matchController.isAllSunk(localPlayer)) {
            AIPlayer.Coord c = aiAgent.pickNextAttack();
            if (c == null) {
                logger.warning("AI has no moves left");
                break;
            }

            AttackResult res = matchController.attack(opponentPlayer, localPlayer, c.x, c.y);
            handleAttackResult(res, c.x, c.y, false);
        }

        if (boardUpdater != null) {
            Platform.runLater(() -> {
                for (int x = 0; x < 10; x++) {
                    for (int y = 0; y < 10; y++) {
                        boardUpdater.updatePlayerCell(x, y);
                    }
                }
            });
        }
    }
}
