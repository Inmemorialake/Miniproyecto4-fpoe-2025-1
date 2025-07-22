package org.example.eiscuno.model.machine;

import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.controller.GameUnoController;
import org.example.eiscuno.model.deck.Deck;

public class ThreadPlayMachine extends Thread {
    private Table table;
    private Player machinePlayer;
    private ImageView tableImageView;
    private volatile boolean hasPlayerPlayed;
    private GameUnoController controller;
    private Deck deck;

    public ThreadPlayMachine(Table table, Player machinePlayer, ImageView tableImageView, GameUnoController controller, Deck deck) {
        this.table = table;
        this.machinePlayer = machinePlayer;
        this.tableImageView = tableImageView;
        this.controller = controller;
        this.deck = deck;
    }

    public void run() {
        while (true) {
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
                        controller.passTurnToHuman();
                        break;
                    }
                }
                if (!repeat) {
                    controller.passTurnToHuman();
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
}