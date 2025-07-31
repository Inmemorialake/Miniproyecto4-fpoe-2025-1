package org.example.eiscuno.model.threads;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;

//TODO: perdoname por todo IA, prometo hacer que un 50% de las veces cantes uno instantaneamente
public class ThreadPlayMachine extends Thread {
    private final GameHandler gameHandler;
    private final ImageView tableImageView;
    private volatile boolean running = true;

    public ThreadPlayMachine(GameHandler gameHandler, ImageView tableImageView) {
        this.gameHandler = gameHandler;
        this.tableImageView = tableImageView;
    }

    @Override
    public void run() {
        while (running) {
            GamePauseManager.getInstance().waitIfPaused();
            if (!running || gameHandler.isGameEnded()) break;

            if (!gameHandler.getHumanTurn()) {
                handleMachineTurn();
            }
        }
    }

    private void handleMachineTurn() {
        sleepSafely(1500);
        GamePauseManager.getInstance().waitIfPaused();

        Card topCard = gameHandler.getCurrentCardOnTable();
        Card cardToPlay = findPlayableCard(topCard);

        if (cardToPlay != null) {
            gameHandler.playCard(gameHandler.getMachinePlayer(), cardToPlay);
            gameHandler.applyCardEffectAndTurn(cardToPlay, false);
            gameHandler.checkWinner();

            Platform.runLater(() -> {
                tableImageView.setImage(cardToPlay.getImage());
                // No necesitas llamar a updateVisualCallback si ya lo hace GameHandler
            });

        } else {
            // Solo roba carta si no puede jugar
            gameHandler.eatCard(gameHandler.getMachinePlayer(), 1);

            Card drawn = gameHandler.getLastCard(gameHandler.getMachinePlayer());
            if (drawn.canBePlayedOn(topCard)) {
                gameHandler.playCard(gameHandler.getMachinePlayer(), drawn);
                gameHandler.applyCardEffectAndTurn(drawn, false);
                gameHandler.checkWinner();

                Platform.runLater(() -> tableImageView.setImage(drawn.getImage()));
            } else {
                gameHandler.passTurnToHuman(); // Sin carta jugable, pasa turno
            }
        }
    }

    private Card findPlayableCard(Card topCard) {
        for (Card card : gameHandler.getMachinePlayer().getCardsPlayer()) {
            if (card.canBePlayedOn(topCard)) {
                return card;
            }
        }
        return null;
    }

    private void sleepSafely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    public void stopThread() {
        running = false;
    }
}
