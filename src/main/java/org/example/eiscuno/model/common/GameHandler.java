package org.example.eiscuno.model.common;

// Imports
import javafx.application.Platform;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.example.eiscuno.view.DialogManager;

import java.io.Serializable;
import java.util.*;

/**
 * Handles the game logic and state for the EISC Uno game.
 * This class manages players, deck, table, and game flow.
 */
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

    /**
     * Constructs a GameHandler with the specified players, deck, table, and game state.
     *

     * @param human The human player.
     * @param machine The machine player.
     * @param deck The deck of cards.
     * @param table The table where cards are played.
     * @param iaSaidUno Indicates if the AI has said UNO.
     * @param isHumanTurn Indicates if it's the human player's turn.
     * @param humanSaidUno Indicates if the human player has said UNO.
     * @param updateVisualCallback Callback to update the visual representation of the game.
     * @param resetCardScroll Callback to reset card scrolling in the UI.
     */
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

    /**
     * Creates a new game with default players, deck, and table.
     *
     * @param updateVisualCallback Callback to update the visual representation of the game.
     * @param resetCardScroll Callback to reset card scrolling in the UI.
     * @return A new instance of GameHandler.
     */
    public static GameHandler createNewGame(Runnable updateVisualCallback, Runnable resetCardScroll) {
        Player human = new Player("HUMAN_PLAYER");
        Player machine = new Player("MACHINE_PLAYER");
        Deck deck = new Deck();
        Table table = new Table();
        GameHandler handler = new GameHandler(human, machine, deck, table, false, true, false, updateVisualCallback, resetCardScroll);
        handler.startGame();
        return handler;
    }

    /**
     * Starts the game by dealing cards to players and setting the initial card on the table.
     * Deals 5 cards to the human player and 5 cards to the machine player.
     * The initial card on the table is a non-special card.
     */
    public void startGame() {
        for (int i = 0; i < 10; i++) {
            if (i < 5) {
                humanPlayer.addCard(deck.takeCard());
            } else {
                machinePlayer.addCard(deck.takeCard());
            }
        }

        // Set the initial card on the table, ensuring it's not a special card
        Card initialCard;
        do {
            initialCard = deck.takeCard();
        } while (initialCard.isSpecial());
        table.addCardOnTheTable(initialCard);
    }

    /**
     * Eats a specified number of cards from the deck and adds them to the player's hand.
     * If the deck is empty, it refills the deck with cards in use before taking cards.
     * @param player The player who will receive the cards.
     * @param numberOfCards The number of cards to take from the deck.
     */
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
                // If the deck is empty, refill it and try again
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

    /**
     * Handles the click event on a human player's card.
     * Validates if the card can be played on the current card on the table.
     * If valid, plays the card, applies its effect, and checks for a winner.
     * If the card is not a special card, it passes the turn to the machine player.
     *
     * @param card The card clicked by the human player.
     * @param onFinish A callback to run after handling the card click.
     * @return true if the card was played successfully, false otherwise.
     */
    public boolean handleHumanCardClick(Card card, Runnable onFinish) {
        if (!isHumanTurn) {
            return false
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
     * Gets the current visible cards for the human player starting from a specified position.
     * The cards are reversed to show the most recent cards first.
     *
     * @param posInitCardToShow The starting position of the card to show.
     * @return An array of visible cards for the human player
     */
    public Card[] getCurrentVisibleCardsHumanPlayer(int posInitCardToShow) {
        List<Card> allCards = new ArrayList<>(humanPlayer.getCardsPlayer());
        Collections.reverse(allCards);
        int totalCards = allCards.size();

        int numVisibleCards = Math.min(4, totalCards - posInitCardToShow);
        Card[] cards = new Card[numVisibleCards];
        for (int i = 0; i < numVisibleCards; i++) {
            cards[i] = allCards.get(posInitCardToShow + i);
        }
        return cards;
    }

    /**
     * Checks if the player has any playable card based on the current card on the table.
     *
     * @param player The player to check for playable cards.
     * @return true if the player has at least one playable card, false otherwise.
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
     * Plays a card from the player's hand and updates the game state.
     * The card is added to the table, removed from the player's hand, and the visual representation is updated.
     *
     * @param player The player who is playing the card.
     * @param card The card to be played.
     */
    public void playCard(Player player, Card card) {
        table.addCardOnTheTable(card);
        player.getCardsPlayer().remove(card);
        Platform.runLater(updateVisualCallback);
        GameSaver.save(this);
    }

    /**
     * Applies the effect of the played card and updates the game state.
     * Handles special cards like +2, +4, wild cards, skip, and reverse.
     * Updates the turn based on whether the card was played by a human or machine player.
     *
     * @param card The card that was played.
     * @param playedByHuman Indicates if the card was played by the human player.
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

            // Default choosse a random color for wild cards
            // If played by human, use the color chooser to select a color
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

            // If the card is a skip, reverse, +2, or +4, the turn remains with the same player
            isHumanTurn = playedByHuman;
        } else {
            isHumanTurn = !playedByHuman;
        }

        GameSaver.save(this);
    }

    /**
     * Sets the color chooser for selecting colors when playing wild cards
     * @param colorChooser The ColorChooser instance to set.
     */
    public void setColorChooser(ColorChooser colorChooser) {
        this.colorChooser = colorChooser;
    }

    /**
     * Checks if there is a winner in the game.
     * If the human player has no cards left, they win.
     * If the machine player has no cards left, they win.
     * Updates the game state accordingly and returns the winner.
     *
     * @return The type of the winner ("HUMAN" or "MACHINE"), or null if no winner yet.
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

    /**
     * Gets the last card played by the specified player.
     *
     * @param player The player whose last card is to be retrieved.
     * @return The last card played by the player, or null if the player has no cards.
     */
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