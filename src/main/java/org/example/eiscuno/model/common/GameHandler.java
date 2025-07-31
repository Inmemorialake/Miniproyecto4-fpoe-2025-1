package org.example.eiscuno.model.common;

import javafx.application.Platform;
import javafx.scene.control.ChoiceDialog;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.view.DialogManager;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.CountDownLatch;

public class GameHandler implements Serializable {

    private final Player humanPlayer;
    private final Player machinePlayer;
    private final Deck deck;
    private final Table table;
    private transient Runnable updateVisualCallback;
    private transient Runnable resetCardScroll;

    private boolean iaSaidUno;
    private boolean isHumanTurn;
    private boolean humanSaidUno;
    private boolean gameEnded;

    private String winner;

    private transient ColorChooser colorChooser;

    public GameHandler(Player human, Player machine, Deck deck, Table table, boolean iaSaidUno, boolean isHumanTurn, boolean humanSaidUno, Runnable updateVisualCallback, Runnable resetCardScroll) {
        this.humanPlayer = human;
        this.machinePlayer = machine;
        this.deck = deck;
        this.table = table;
        this.updateVisualCallback = updateVisualCallback;
        this.resetCardScroll = resetCardScroll;
        this.iaSaidUno = iaSaidUno;
        this.isHumanTurn = isHumanTurn;
        this.humanSaidUno = humanSaidUno;
        this.gameEnded = false;
        this.winner = null;
    }

    public static GameHandler createNewGame(Runnable updateVisualCallback, Runnable resetCardScroll) {
        Player human = new Player("HUMAN_PLAYER");
        Player machine = new Player("MACHINE_PLAYER");
        Deck deck = new Deck();
        Table table = new Table();
        GameHandler handler = new GameHandler(human, machine, deck, table, false, true, false, updateVisualCallback, resetCardScroll);
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
                Platform.runLater(() -> {
                    if(resetCardScroll != null) resetCardScroll.run();
                    updateVisualCallback.run();
                });
                GameSaver.save(this);
            } catch (IllegalStateException e) {
                // Si el mazo está vacío, lo rellenamos con las cartas en uso
                List<Card> inUse = new ArrayList<>();
                deck.refillDeck(inUse);
                player.addCard(deck.takeCard());
                Platform.runLater(() -> {
                    if(resetCardScroll != null) resetCardScroll.run();
                    updateVisualCallback.run();
                });
                GameSaver.save(this);
            }
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

        applyCardEffectAndTurn(card, true);

        checkWinner();

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
        List<Card> allCards = new ArrayList<>(humanPlayer.getCardsPlayer()); // <-- Copia
        Collections.reverse(allCards);
        int totalCards = allCards.size();

        int numVisibleCards = Math.min(4, totalCards - posInitCardToShow);
        Card[] cards = new Card[numVisibleCards];
        for (int i = 0; i < numVisibleCards; i++) {
            cards[i] = allCards.get(posInitCardToShow + i);
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
        Platform.runLater(updateVisualCallback);
        GameSaver.save(this);
    }

    public void applyCardEffectAndTurn(Card card, boolean playedByHuman) {
        if (card.isPlusTwo()) {
            if (playedByHuman) {
                machinePlayer.addCards(deck.takeCards(2));
            } else {
                humanPlayer.addCards(deck.takeCards(2));
            }

            DialogManager.showInfoDialog("+2 Jugado", "Se repite el turno!");
            GamePauseManager.getInstance().pauseGame();

        } else if (card.isPlusFour() || card.isWildCard()) {
            if(card.isPlusFour()) {
                if (playedByHuman) {
                    machinePlayer.addCards(deck.takeCards(4));
                } else {
                    humanPlayer.addCards(deck.takeCards(4));
                }

                DialogManager.showInfoDialog("+4 Jugado", "Se repite el turno!");
                GamePauseManager.getInstance().pauseGame();
            }

            // Color aleatorio por defecto
            String[] colors = {"RED", "GREEN", "BLUE", "YELLOW"};
            String newColor = colors[new Random().nextInt(colors.length)];

            if (playedByHuman) {
                newColor = colorChooser.chooseColor();
            }

            card.setColor(newColor);
        } else if(card.isSkipOrReverse()) {
            DialogManager.showInfoDialog("Skip / Block jugado", "Se repite el turno!");
            GamePauseManager.getInstance().pauseGame();
        }

        if (card.isSkipOrReverse() || card.isPlusTwo() || card.isPlusFour()) {
            // El jugador que juega se queda con el turno (reverse/skip/PlusSomething)
            isHumanTurn = playedByHuman;
        } else {
            isHumanTurn = !playedByHuman;
        }

        GameSaver.save(this);
    }

    public void setColorChooser(ColorChooser colorChooser) {
        this.colorChooser = colorChooser;
    }

    public String checkWinner() {
        if (humanPlayer.getCardsPlayer().isEmpty()) {
            gameEnded = true;
            winner = "HUMAN";
            PlayerStatsManager.updateStats(true, 0, false);
        } else if (machinePlayer.getCardsPlayer().isEmpty()) {
            gameEnded = true;
            winner = "MACHINE";
            PlayerStatsManager.updateStats(false, 0, false);
        } else {
            winner = null; // No hay ganador aún
        }
        return winner;
    }

    public Card getLastCard(Player player) {
        List<Card> cards = player.getCardsPlayer();
        return cards.isEmpty() ? null : cards.get(cards.size() - 1);
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

    public void setUpdateVisualCallback(Runnable updateVisualCallback) {
        this.updateVisualCallback = updateVisualCallback;
    }

    public void setResetCardScroll(Runnable resetCardScroll) {
        this.resetCardScroll = resetCardScroll;
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