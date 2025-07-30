package org.example.eiscuno.model.common;

import javafx.application.Platform;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameHandler implements Serializable {

    private final Player humanPlayer;
    private final Player machinePlayer;
    private final Deck deck;
    private final Table table;

    private boolean iaSaidUno;
    private boolean isHumanTurn;
    private boolean humanSaidUno;
    private boolean gameEnded;

    private transient ColorChooser colorChooser;

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
        } //Sexo anal

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

    public boolean handleHumanCardClick(Card card, Runnable onFinish) {
        if (!isHumanTurn) {
            return false; // para que el controlador sepa si fue un movimiento válido
        }

        if (!card.canBePlayedOn(getCurrentCardOnTable())) {
            return false;
        }

        playCard(humanPlayer, card);
        PlayerStatsManager.updateStats(false, 1, true);

        applySpecialCardEffect(card, true);

        checkWinner();

        // Verificar UNO
        if (humanPlayer.getCardsPlayer().size() == 1 && !humanSaidUno) {
            // lanzar temporizador si no dijo UNO
            new Thread(() -> {
                try {
                    Thread.sleep(3000);
                    if (!humanSaidUno && humanPlayer.getCardsPlayer().size() == 1) {
                        eatCard(humanPlayer, 1);
                    }
                } catch (InterruptedException ignored) {}
            }).start();
        }

        if (!card.isSkipOrReverse() && !card.isPlusTwo() && !card.isPlusFour()) {
            // Si no es una carta especial, pasamos el turno a la máquina
            passTurnToMachine();
        }

        if (onFinish != null) {
            Platform.runLater(onFinish);
        }

        return true;
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
        } else if (card.isPlusFour() || card.isWildCard()) {
            String newColor = "RED"; // por defecto

            if (playedByHuman && colorChooser != null) {
                newColor = colorChooser.chooseColor(); // popup si es humano
            } else {
                // color aleatorio para IA
                String[] colors = {"RED", "GREEN", "BLUE", "YELLOW"};
                newColor = colors[new Random().nextInt(colors.length)];
            }

            card.setColor(newColor);
        }

        if (card.isSkipOrReverse() || card.isPlusTwo() || card.isPlusFour()) {
            // El jugador que juega se queda con el turno (reverse/skip/PlusSomething)
            isHumanTurn = playedByHuman;
        }
    }

    public void setColorChooser(ColorChooser colorChooser) {
        this.colorChooser = colorChooser;
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