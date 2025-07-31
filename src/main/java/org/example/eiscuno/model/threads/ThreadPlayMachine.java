package org.example.eiscuno.model.threads;

import javafx.application.Platform;
import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;

public class ThreadPlayMachine extends Thread {
    private final GameHandler gameHandler;
    private final ImageView tableImageView;
    private final Runnable updateVisualsCallback; // <-- Nuevo callback
    private volatile boolean running = true;

    public ThreadPlayMachine(GameHandler gameHandler, ImageView tableImageView, Runnable updateVisualsCallback) {
        this.gameHandler = gameHandler;
        this.tableImageView = tableImageView;
        this.updateVisualsCallback = updateVisualsCallback;
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
            playCard(cardToPlay);
            gameHandler.checkWinner();
        } else {
            Card drawn = gameHandler.getDeck().takeCard();
            gameHandler.getMachinePlayer().addCard(drawn);

            Platform.runLater(updateVisualsCallback); // <-- Mostrar carta robada
            gameHandler.passTurnToHuman();
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

    private void playCard(Card card) {
        GamePauseManager.getInstance().waitIfPaused();

        gameHandler.playCard(gameHandler.getMachinePlayer(), card);
        gameHandler.applyCardEffectAndTurn(card, false);

        Platform.runLater(() -> {
            tableImageView.setImage(card.getImage());
            updateVisualsCallback.run(); // <-- Mostrar carta jugada
        });
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