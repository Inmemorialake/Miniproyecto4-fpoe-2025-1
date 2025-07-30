package org.example.eiscuno.model.machine;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.model.deck.Deck;

import java.util.Random;

public class ThreadPlayMachine extends Thread {
    private final Table table;
    private final Player machinePlayer;
    private final ImageView tableImageView;
    private final Deck deck;
    private final GameHandler gameHandler;
    private volatile boolean running = true;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView, GameHandler gameHandler, Deck deck) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.gameHandler = gameHandler;
        this.deck = deck;
    }

    @Override
    public void run() {
        while (running) {
            if (!running || gameHandler.isGameEnded()) break;

            if (!gameHandler.getHumanTurn()) {
                boolean repeat;
                do {
                    repeat = false;
                    try {
                        Thread.sleep(1500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    Card topCard = table.getCurrentCardOnTheTable();
                    Card cardToPlay = null;

                    for (Card card : machinePlayer.getCardsPlayer()) {
                        if (card.canBePlayedOn(topCard)) {
                            cardToPlay = card;
                            break;
                        }
                    }

                    if (cardToPlay != null) {
                        Card finalCardToPlay = cardToPlay;
                        Platform.runLater(() -> tableImageView.setImage(finalCardToPlay.getImage()));
                        gameHandler.applySpecialCardEffect(cardToPlay, false);
                        gameHandler.playCard(machinePlayer, cardToPlay);

                        if (cardToPlay.isPlusTwo() || cardToPlay.isPlusFour()) {
                            Platform.runLater(() -> gameHandler.getHumanPlayer().getCardsPlayer().forEach(Card::restoreVisuals));
                        }

                        repeat = cardToPlay.isSkipOrReverse();

                        if (machinePlayer.getCardsPlayer().size() == 1 && !gameHandler.getIASaidUno()) {
                            handleMachineUno();
                        }

                        gameHandler.checkWinner();
                    } else {
                        Card drawn = deck.takeCard();
                        machinePlayer.addCard(drawn);

                        if (drawn.canBePlayedOn(topCard)) {
                            gameHandler.playCard(machinePlayer, drawn);
                            Platform.runLater(() -> tableImageView.setImage(drawn.getImage()));
                            gameHandler.applySpecialCardEffect(drawn, false);

                            if (drawn.isPlusTwo() || drawn.isPlusFour()) {
                                Platform.runLater(() -> gameHandler.getHumanPlayer().getCardsPlayer().forEach(Card::restoreVisuals));
                            }

                            repeat = drawn.isSkipOrReverse();
                        } else {
                            repeat = false;
                            gameHandler.passTurnToHuman();
                            break;
                        }
                    }

                    if (!repeat) {
                        gameHandler.passTurnToHuman();
                    }
                } while (repeat);
            }
        }
    }

    private void handleMachineUno() {
        new Thread(() -> {
            try {
                Thread.sleep(2000 + new Random().nextInt(2000));
            } catch (InterruptedException ignored) {}
            Platform.runLater(() -> {
                if (!gameHandler.getHumanSaidUno()) {
                    gameHandler.setIASaidUno(true);
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("¡UNO!");
                    alert.setHeaderText(null);
                    alert.setContentText("La máquina ha gritado ¡UNO!");
                    alert.showAndWait();
                }
            });
        }).start();
    }

    public void stopThread() {
        running = false;
    }
}