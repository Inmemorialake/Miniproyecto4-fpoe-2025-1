package org.example.eiscuno.model.threads;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;

import java.util.Random;

public class ThreadPlayMachine extends Thread {
    private final GameHandler gameHandler;
    private final ImageView tableImageView;
    private final Runnable updateMachineCardsCallback; // <-- Nuevo callback
    private volatile boolean running = true;

    public ThreadPlayMachine(GameHandler gameHandler, ImageView tableImageView, Runnable updateMachineCardsCallback) {
        this.gameHandler = gameHandler;
        this.tableImageView = tableImageView;
        this.updateMachineCardsCallback = updateMachineCardsCallback;
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

            Platform.runLater(updateMachineCardsCallback); // <-- Mostrar carta robada
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
            updateMachineCardsCallback.run(); // <-- Mostrar carta jugada

            if (card.isPlusTwo() || card.isPlusFour()) {
                gameHandler.getHumanPlayer().getCardsPlayer().forEach(Card::restoreVisuals);
            }
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