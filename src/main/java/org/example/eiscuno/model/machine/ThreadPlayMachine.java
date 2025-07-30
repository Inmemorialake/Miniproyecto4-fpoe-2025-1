package org.example.eiscuno.model.machine;

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

            if (gameHandler.getMachinePlayer().getCardsPlayer().size() == 1 && !gameHandler.getIASaidUno()) {
                handleMachineUno();
            }

            gameHandler.checkWinner();
        } else {
            Card drawn = gameHandler.getDeck().takeCard();
            gameHandler.getMachinePlayer().addCard(drawn);

            Platform.runLater(updateMachineCardsCallback); // <-- Mostrar carta robada

            if (drawn.canBePlayedOn(topCard)) {
                playCard(drawn);
            } else {
                gameHandler.passTurnToHuman();
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

    private void handleMachineUno() {
        new Thread(() -> {
            sleepSafely(2000 + new Random().nextInt(2000));
            GamePauseManager.getInstance().waitIfPaused();

            Platform.runLater(() -> {
                if (!gameHandler.getHumanSaidUno()) {
                    gameHandler.setIASaidUno(true);
                    GamePauseManager.getInstance().pauseGame();

                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("¡UNO!");
                    alert.setHeaderText(null);
                    alert.setContentText("La máquina ha gritado ¡UNO!");
                    alert.setOnHidden(e -> GamePauseManager.getInstance().resumeGame());
                    alert.showAndWait();
                }
            });
        }).start();
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