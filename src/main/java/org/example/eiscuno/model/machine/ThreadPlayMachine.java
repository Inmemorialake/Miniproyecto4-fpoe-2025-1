package org.example.eiscuno.model.machine;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.model.deck.Deck;

import java.util.Random;

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
            GamePauseManager.getInstance().waitIfPaused(); //Se pausa si hay algun dialog corriendo que haya que esperar
            if (!running || gameHandler.isGameEnded()) break; //Deja de correr si el running es false o si el juego ha terminado

            if (!gameHandler.getHumanTurn()) { // Si no es el turno del humano, ejecutamos su ciclo de juego
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
            // El cambio de turno ya lo hace GameHandler.applySpecialCardEffect()
        } else {
            Card drawn = gameHandler.getDeck().takeCard();
            gameHandler.getMachinePlayer().addCard(drawn);

            if (drawn.canBePlayedOn(topCard)) {
                playCard(drawn);
            } else {
                gameHandler.passTurnToHuman(); // Ya no hay carta jugable
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
        gameHandler.applySpecialCardEffect(card, false);

        Platform.runLater(() -> {
            tableImageView.setImage(card.getImage());

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