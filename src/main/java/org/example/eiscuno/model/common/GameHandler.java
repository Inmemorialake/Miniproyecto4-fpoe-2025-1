package org.example.eiscuno.model.common;

import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GameHandler implements Serializable {

    private final Player humanPlayer;
    private final Player machinePlayer;
    private final Deck deck;
    private final Table table;

    private boolean iaSaidUno;
    private boolean isHumanTurn;
    private boolean humanSaidUno;
    private boolean gameEnded;

    public GameHandler(Player human, Player machine, Deck deck, Table table,
                       boolean iaSaidUno, boolean isHumanTurn, boolean humanSaidUno) {
        this.humanPlayer = human;
        this.machinePlayer = machine;
        this.deck = deck;
        this.table = table;
        this.iaSaidUno = iaSaidUno;
        this.isHumanTurn = isHumanTurn;
        this.humanSaidUno = humanSaidUno;
        this.gameEnded = false;
    }

    public static GameHandler createNewGame() {
        Player human = new Player("HUMAN_PLAYER");
        Player machine = new Player("MACHINE_PLAYER");
        Deck deck = new Deck();
        Table table = new Table();
        GameHandler handler = new GameHandler(human, machine, deck, table, false, true, false);
        handler.startGame();
        return handler;
    }

    public void startGame() {
        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                humanPlayer.addCard(deck.takeCard());
            } else {
                machinePlayer.addCard(deck.takeCard());
            }
        }

        // Seleccionar carta inicial que no sea especial
        Card initialCard;
        do {
            initialCard = deck.takeCard();
        } while (initialCard.isSpecial());
        table.addCardOnTheTable(initialCard);
    }

    public void eatCard(Player player, int numberOfCards) {
        for (int i = 0; i < numberOfCards; i++) {
            try {
                player.addCard(deck.takeCard());
            } catch (IllegalStateException e) {
                // Si el mazo está vacío, lo rellenamos con las cartas en uso
                List<Card> inUse = new ArrayList<>();
                inUse.addAll(humanPlayer.getCardsPlayer());
                inUse.addAll(machinePlayer.getCardsPlayer());
                deck.refillDeck(inUse);
                player.addCard(deck.takeCard());
            }
        }
    }

    public void haveSungUno(String playerWhoSang) {
        if ("HUMAN_PLAYER".equals(playerWhoSang)) {
            machinePlayer.addCard(deck.takeCard());
        } else {
            humanPlayer.addCard(deck.takeCard());
        }
    }

    public Card[] getCurrentVisibleCardsHumanPlayer(int posInitCardToShow) {
        int totalCards = humanPlayer.getCardsPlayer().size();
        int numVisibleCards = Math.min(4, totalCards - posInitCardToShow);
        Card[] cards = new Card[numVisibleCards];
        for (int i = 0; i < numVisibleCards; i++) {
            cards[i] = humanPlayer.getCard(posInitCardToShow + i);
        }
        return cards;
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
            // El jugador que juega se queda con el turno (reverse/skip)
            isHumanTurn = playedByHuman;
        }
    }

    public void checkWinner() {
        if (humanPlayer.getCardsPlayer().isEmpty() || machinePlayer.getCardsPlayer().isEmpty()) {
            gameEnded = true;
        }
    }

    public Card getCurrentCardOnTable() {
        return table.getCurrentCardOnTheTable();
    }

    public Player getHumanPlayer() {
        return humanPlayer;
    }

    public Player getMachinePlayer() {
        return machinePlayer;
    }

    public Deck getDeck() {
        return deck;
    }

    public Table getTable() {
        return table;
    }

    public boolean getIASaidUno() {
        return iaSaidUno;
    }

    public void setIASaidUno(boolean iaSaidUno) {
        this.iaSaidUno = iaSaidUno;
    }

    public boolean getHumanSaidUno() {
        return humanSaidUno;
    }

    public void setHumanSaidUno(boolean humanSaidUno) {
        this.humanSaidUno = humanSaidUno;
    }

    public boolean getHumanTurn() {
        return isHumanTurn;
    }

    public void passTurnToHuman() {
        isHumanTurn = true;
    }

    public void passTurnToMachine() {
        isHumanTurn = false;
    }

    public boolean isGameEnded() {
        return gameEnded;
    }
}