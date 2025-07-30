package org.example.eiscuno.model.common;

import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

import java.io.Serializable;

public class GameHandler implements Serializable {
    private final Player humanPlayer;
    private final Player machinePlayer;
    private final Deck deck;
    private final Table table;
    private final GameUno game;

    private boolean iaSaidUno;
    private boolean isHumanTurn;
    private boolean humanSaidUno;
    private boolean gameEnded;

    public GameHandler(Player human, Player machine, Deck deck, Table table, GameUno game, boolean iaSaidUno, boolean isHumanTurn, boolean humanSaidUno) {
        this.humanPlayer = human;
        this.machinePlayer = machine;
        this.deck = deck;
        this.table = table;
        this.game = game;
        this.iaSaidUno = iaSaidUno;
        this.isHumanTurn = isHumanTurn;
        this.humanSaidUno = humanSaidUno;
        this.gameEnded = false;
    }

    public static GameHandler createNewGame() {
        Player human = new Player("Human");
        Player machine = new Player("Machine");
        Deck deck = new Deck();
        Table table = new Table();
        GameUno game = new GameUno(human, machine, deck, table);

        // Agregar una carta inicial a la mesa
        Card initialCard = deck.takeCard();
        table.addCardOnTheTable(initialCard);

        return new GameHandler(human, machine, deck, table, game, false, true, false);
    }

    public Player getHumanPlayer() { return humanPlayer; }
    public Player getMachinePlayer() { return machinePlayer; }
    public Deck getDeck() { return deck; }
    public Table getTable() { return table; }
    public GameUno getGame() { return game; }
    public boolean getIASaidUno() { return iaSaidUno; }
    public boolean getHumanTurn() { return isHumanTurn; }
    public boolean getHumanSaidUno() { return humanSaidUno; }
    public boolean isGameEnded() { return gameEnded; }

    public void setHumanSaidUno(boolean humanSaidUno) {
        this.humanSaidUno = humanSaidUno;
    }

    public void setIASaidUno(boolean iaSaidUno) {
        this.iaSaidUno = iaSaidUno;
    }

    public void passTurnToHuman() {
        isHumanTurn = true;
    }

    public void passTurnToMachine() {
        isHumanTurn = false;
    }

    public boolean hasPlayableCard(Player player) {
        Card topCard = table.getCurrentCardOnTheTable();
        for (Card card : player.getCardsPlayer()) {
            if (card.canBePlayedOn(topCard)) {
                return true;
            }
        }
        return false;
    }

    public void playCard(Player player, Card card) {
        table.addCardOnTheTable(card);
        player.getCardsPlayer().remove(card);
    }

    public void applySpecialCardEffect(Card card, boolean playedByHuman) {
        if (card.isPlusTwo()) {
            if (playedByHuman) {
                machinePlayer.addCards(deck.takeCards(2));
            } else {
                humanPlayer.addCards(deck.takeCards(2));
            }
        } else if (card.isPlusFour()) {
            if (playedByHuman) {
                machinePlayer.addCards(deck.takeCards(4));
            } else {
                humanPlayer.addCards(deck.takeCards(4));
            }
        } else if (card.isSkipOrReverse()) {
            // Manejo de skip/reverse
            isHumanTurn = playedByHuman;
        }
    }

    public void checkWinner() {
        if (humanPlayer.getCardsPlayer().isEmpty()) {
            gameEnded = true;
        } else if (machinePlayer.getCardsPlayer().isEmpty()) {
            gameEnded = true;
        }
    }

    public Card getCurrentCardOnTable() {
        return this.table.getCurrentCardOnTheTable();
    }
}