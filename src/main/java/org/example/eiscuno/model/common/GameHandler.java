package org.example.eiscuno.model.common;

// Imports
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

/**
 * GameHandler class manages the game state, including players, deck, and table.
 * It handles game logic such as starting the game, playing cards, applying card effects,
 * checking for winners, and managing turns.
 * It also provides methods for interacting with the game, such as handling card clicks
 * and checking for playable cards.
 * This class is serializable to allow saving and loading game state.
 * It also supports a color chooser for wild cards and plus four cards.
 */
public class GameHandler implements Serializable {

    private final Player humanPlayer;
    private final Player machinePlayer;
    private final Deck deck;
    private final Table table;
    private transient Runnable updateVisualCallback;

    private boolean iaSaidUno;
    private boolean isHumanTurn;
    private boolean humanSaidUno;
    private boolean gameEnded;

    private String winner;

    private transient ColorChooser colorChooser;

    /**
     * Constructs a GameHandler with the specified players, deck, table, and game state.
     *
     * @param human the human player
     * @param machine the machine player
     * @param deck the deck of cards
     * @param table the table where cards are played
     * @param iaSaidUno indicates if the AI has said Uno
     * @param isHumanTurn indicates if it's the human player's turn
     * @param humanSaidUno indicates if the human player has said Uno
     * @param updateVisualCallback callback to update the visual representation of the game
     */
    public GameHandler(Player human, Player machine, Deck deck, Table table, boolean iaSaidUno, boolean isHumanTurn, boolean humanSaidUno, Runnable updateVisualCallback) {
        this.humanPlayer = human;
        this.machinePlayer = machine;
        this.deck = deck;
        this.table = table;
        this.updateVisualCallback = updateVisualCallback;
        this.iaSaidUno = iaSaidUno;
        this.isHumanTurn = isHumanTurn;
        this.humanSaidUno = humanSaidUno;
        this.gameEnded = false;
        this.winner = null;
    }

    /**
     * Creates a new game with a human player and a machine player.
     * Initializes the deck and table, and starts the game.
     *
     * @param updateVisualCallback callback to update the visual representation of the game
     * @return a new GameHandler instance
     */
    public static GameHandler createNewGame(Runnable updateVisualCallback) {
        Player human = new Player("HUMAN_PLAYER");
        Player machine = new Player("MACHINE_PLAYER");
        Deck deck = new Deck();
        Table table = new Table();
        GameHandler handler = new GameHandler(human, machine, deck, table, false, true, false, updateVisualCallback);
        handler.startGame();
        return handler;
    }

    /**
     * Starts the game by dealing cards to players and setting the initial card on the table.
     * Deals 5 cards to the human player and 5 cards to the machine player.
     * Selects an initial card that is not a special card to start the game.
     */
    public void startGame() {
        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                humanPlayer.addCard(deck.takeCard());
            } else {
                machinePlayer.addCard(deck.takeCard());
            }
        }

        // Selection of the initial card on the table
        // We ensure that the initial card is not a special card
        Card initialCard;
        do {
            initialCard = deck.takeCard();
        } while (initialCard.isSpecial());
        table.addCardOnTheTable(initialCard);
    }

    /**
     * Eats a specified number of cards from the deck and adds them to the player's hand.
     * If the deck is empty, it refills the deck with cards in use before taking cards.
     *
     * @param player the player who will receive the cards
     * @param numberOfCards the number of cards to eat
     */
    public void eatCard(Player player, int numberOfCards) {
        for (int i = 0; i < numberOfCards; i++) {
            try {
                player.addCard(deck.takeCard());
                Platform.runLater(updateVisualCallback);
                GameSaver.save(this);
            } catch (IllegalStateException e) {
                // If the deck is empty, we refill it with cards in use
                List<Card> inUse = new ArrayList<>();
                deck.refillDeck(inUse);
                player.addCard(deck.takeCard());
                Platform.runLater(updateVisualCallback);
                GameSaver.save(this);
            }
        }
    }

    /**
     * Handles the click on a card by the human player.
     * Checks if it's the human's turn and if the card can be played on the current card on the table.
     * If valid, plays the card, applies its effect, and checks for a winner.
     * If the card is not a special card, it passes the turn to the machine player.
     *
     * @param card the card clicked by the human player
     * @param onFinish callback to run after handling the click
     * @return true if the card was played successfully, false otherwise
     */
    public boolean handleHumanCardClick(Card card, Runnable onFinish) {
        if (!isHumanTurn) {
            return false; // If it's not the human's turn, return false
        }

        if (!card.canBePlayedOn(getCurrentCardOnTable())) {
            return false;
        }

        playCard(humanPlayer, card);
        PlayerStatsManager.updateStats(false, 1, true);

        applyCardEffectAndTurn(card, true);

        checkWinner();

        if (!card.isSkipOrReverse() && !card.isPlusTwo() && !card.isPlusFour()) {
            // If the card is not a special card, pass the turn to the machine player
            passTurnToMachine();
        }

        if (onFinish != null) {
            Platform.runLater(onFinish);
        }

        return true;
    }

    /**
     * Returns the current visible cards of the human player starting from a specified position.
     * This method is used to display the cards in the UI, showing a maximum of 4 cards at a time.
     * If there are fewer than 4 cards available, it returns only the available cards.
     * @param posInitCardToShow the starting position of the card to show
     * @return an array of Card objects representing the visible cards of the human player
     */
    public Card[] getCurrentVisibleCardsHumanPlayer(int posInitCardToShow) {
        int totalCards = humanPlayer.getCardsPlayer().size();
        int numVisibleCards = Math.min(4, totalCards - posInitCardToShow);
        Card[] cards = new Card[numVisibleCards];
        for (int i = 0; i < numVisibleCards; i++) {
            cards[i] = humanPlayer.getCard(posInitCardToShow + i);
        }
        return cards;
    }

    /**
     * Checks if the player has any playable cards based on the current card on the table.
     * A card is playable if it matches the color or number of the top card on the table.
     *
     * @param player the player to check for playable cards
     * @return true if the player has at least one playable card, false otherwise
     */
    public boolean hasPlayableCard(Player player) {
        Card topCard = table.getCurrentCardOnTheTable();
        for (Card card : player.getCardsPlayer()) {
            if (card.canBePlayedOn(topCard)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Plays a card from the player's hand and adds it to the table.
     * The card is removed from the player's hand and the visual representation is updated.
     * The game state is saved after playing the card.
     *
     * @param player the player who plays the card
     * @param card the card to be played
     */
    public void playCard(Player player, Card card) {
        table.addCardOnTheTable(card);
        player.getCardsPlayer().remove(card);
        Platform.runLater(updateVisualCallback);
        GameSaver.save(this);
    }

    /**
     * Applies the effect of the played card and manages the turn based on the card type.
     * If the card is a Plus Two, Plus Four, or Wild Card, it adds cards to the opponent's hand
     * and may change the color of the card. It also handles Skip and Reverse effects.
     *
     * @param card the card that was played
     * @param playedByHuman indicates if the card was played by the human player
     */
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

            // Default random color for wild cards
            // If played by human, use color chooser to select a color
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
            // If the card is a Skip, Reverse, Plus Two, or Plus Four,
            // the turn remains with the same player who played the card
            isHumanTurn = playedByHuman;
        } else {
            isHumanTurn = !playedByHuman;
        }

        GameSaver.save(this);
    }

    /**
     * Sets the color chooser for wild cards and plus four cards.
     * This allows the user to select a color when playing these special cards.
     * @param colorChooser the ColorChooser instance to set
     */
    public void setColorChooser(ColorChooser colorChooser) {
        this.colorChooser = colorChooser;
    }

    /**
     * Checks if there is a winner in the game.
     * If the human player has no cards left, the game ends with the human as the winner.
     * If the machine player has no cards left, the game ends with the machine as the winner.
     * Updates player statistics accordingly.
     *
     * @return the winner of the game, or null if there is no winner yet
     */
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
            winner = null; // No winner yet
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