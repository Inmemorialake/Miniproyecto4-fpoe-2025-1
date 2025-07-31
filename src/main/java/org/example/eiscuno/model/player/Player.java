package org.example.eiscuno.model.player;

// Imports
import org.example.eiscuno.model.card.Card;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Represents a player in the Uno game.
 */
public class Player implements IPlayer, Serializable {
    private ArrayList<Card> cardsPlayer;
    private String typePlayer;

    /**
     * Constructs a new Player object with an empty hand of cards.
     */
    public Player(String typePlayer){
        this.cardsPlayer = new ArrayList<Card>();
        this.typePlayer = typePlayer;
    };

    /**
     * Adds a card to the player's hand.
     *
     * @param card The card to be added to the player's hand.
     */
    @Override
    public void addCard(Card card){
        cardsPlayer.add(card);
    }

    /**
     * Retrieves all cards currently held by the player.
     *
     * @return An ArrayList containing all cards in the player's hand.
     */
    @Override
    public ArrayList<Card> getCardsPlayer() {
        return cardsPlayer;
    }

    /**
     * Removes a card from the player's hand based on its index.
     *
     * @param index The index of the card to remove.
     */
    @Override
    public void removeCard(int index) {
        cardsPlayer.remove(index);
    }

    /**
     * Retrieves a card from the player's hand based on its index.
     *
     * @param index The index of the card to retrieve.
     * @return The card at the specified index in the player's hand.
     */
    @Override
    public Card getCard(int index){
        return cardsPlayer.get(index);
    }

    /**
     * Returns the type of player (e.g., "human", "computer").
     * @return The type of player as a String.
     */
    public String getTypePlayer() {
        return typePlayer;
    }

    /**
     * Adds cards to the player's hand.
     * This method can accept either a single Card object or an ArrayList of Card objects.
     *
     * @param o The card(s) to be added, either a single Card or an ArrayList of Cards.
     */
    public void addCards(Object o) {
        if (o instanceof ArrayList) {
            ArrayList<Card> cards = (ArrayList<Card>) o;
            cardsPlayer.addAll(cards);
        } else if (o instanceof Card) {
            cardsPlayer.add((Card) o);
        } else {
            throw new IllegalArgumentException("Unsupported type for adding cards");
        }
    }
}