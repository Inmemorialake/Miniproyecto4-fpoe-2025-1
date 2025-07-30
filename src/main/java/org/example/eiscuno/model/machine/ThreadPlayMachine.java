package org.example.eiscuno.model.machine;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.controller.GameUnoController;
import org.example.eiscuno.model.deck.Deck;

import java.util.Random;

public class ThreadPlayMachine extends Thread {
    private Table table;
    private Player machinePlayer;
    private ImageView tableImageView;
    private volatile boolean hasPlayerPlayed;
    private GameUnoController controller;
    private Deck deck;
    private Boolean iaSaidUno = false;
    private Boolean running = true;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView, GameUnoController controller, Deck deck, Boolean iaSaidUno) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.controller = controller;
        this.deck = deck;
        this.iaSaidUno = iaSaidUno;
    }

    public void run() {
        while (running) {
            if (!running) break; //Cositas, no tocar xD

            controller.waitForMachineTurn();
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
                    table.addCardOnTheTable(cardToPlay);
                    machinePlayer.getCardsPlayer().remove(cardToPlay);
                    controller.updateTable(cardToPlay);
                    controller.applySpecialCardEffect(cardToPlay, false);

                    if (cardToPlay.isPlusTwo() || cardToPlay.isPlusFour()) {
                        javafx.application.Platform.runLater(() -> controller.printHumanPlayerCards());
                    }
                    repeat = controller.isRepeatTurn();

                    // Si la maquina se queda con una sola carta, hacemos la secuencia para que cante uno
                    if (machinePlayer.getCardsPlayer().size() == 1 && !iaSaidUno) {
                        new Thread(() -> {
                            try {
                                Thread.sleep(2000 + new Random().nextInt(2000)); // 2 a 4 seg
                            } catch (InterruptedException ignored) {}
                            Platform.runLater(() -> {
                                if(!controller.getHumanSaidUno()) {
                                    iaSaidUno = true;
                                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                    alert.setTitle("¡UNO!");
                                    alert.setHeaderText(null);
                                    alert.setContentText("La máquina ha gritado ¡UNO!");

                                    //Guardamos la partida
                                    controller.saveGame();

                                    alert.showAndWait();
                                }
                            });
                        }).start();
                    }

                    //Checkeamos si ha ganado despues de haber hecho la jugada
                    controller.checkWinner();

                } else {
                    // No valid card to play, draw a card
                    Card drawn = deck.takeCard();
                    machinePlayer.addCard(drawn);
                    if (drawn.canBePlayedOn(topCard)) {
                        table.addCardOnTheTable(drawn);
                        machinePlayer.getCardsPlayer().remove(drawn);
                        controller.updateTable(drawn);
                        controller.applySpecialCardEffect(drawn, false);
                        if (drawn.isPlusTwo() || drawn.isPlusFour()) {
                            javafx.application.Platform.runLater(() -> controller.printHumanPlayerCards());
                        }
                        repeat = controller.isRepeatTurn();
                    } else {
                        // Si repeatTurn era true (por reverse/skip), después de comer una carta, pon repeatTurn = false y pasa el turno
                        repeat = false;
                        controller.passTurnToHuman();

                        //Guardamos la partida
                        controller.saveGame();

                        break;
                    }
                }
                if (!repeat) {
                    controller.passTurnToHuman();

                    //Guardamos la partida
                    controller.saveGame();
                }
            } while (repeat);
        }
    }

    private void putCardOnTheTable(){
        int index = (int) (Math.random() * machinePlayer.getCardsPlayer().size());
        Card card = machinePlayer.getCard(index);
        table.addCardOnTheTable(card);
        tableImageView.setImage(card.getImage());
    }

    public void setHasPlayerPlayed(boolean hasPlayerPlayed) {
        this.hasPlayerPlayed = hasPlayerPlayed;
    }

    public Boolean getIASaidUno() {
        return iaSaidUno;
    }

    public Boolean getRunning() { return running; }

    public void setRunning(Boolean aBoolean) { this.running = aBoolean; }

    public void stopThread(){
        running = false;
    }

    public void setIASaidUno(boolean iaSaidUno) { this.iaSaidUno = iaSaidUno; }
}