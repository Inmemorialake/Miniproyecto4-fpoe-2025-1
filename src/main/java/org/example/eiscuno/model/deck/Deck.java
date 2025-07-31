package org.example.eiscuno.model.deck;

import org.example.eiscuno.model.unoenum.EISCUnoEnum;
import org.example.eiscuno.model.card.Card;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents a deck of Uno cards.
 */
public class Deck implements Serializable {
    private Stack<Card> deckOfCards;

    /**
     * Constructs a new deck of Uno cards and initializes it.
     */
    public Deck() {
        deckOfCards = new Stack<>();
        initializeDeck();
    }

    /**
     * Initializes the deck with all possible cards.
     */
    private void initializeDeck() {
        for (EISCUnoEnum cardEnum : EISCUnoEnum.values()) {
            if (cardEnum.name().startsWith("GREEN_") ||
                    cardEnum.name().startsWith("YELLOW_") ||
                    cardEnum.name().startsWith("BLUE_") ||
                    cardEnum.name().startsWith("RED_") ||
                    cardEnum.name().startsWith("SKIP_") ||
                    cardEnum.name().startsWith("REVERSE_") ||
                    cardEnum.name().startsWith("TWO_WILD_DRAW_") ||
                    cardEnum.name().equals("FOUR_WILD_DRAW") ||
                    cardEnum.name().equals("WILD")) {

                Card card = new Card(cardEnum.getFilePath(), getCardValue(cardEnum.name()), getCardColor(cardEnum.name()));
                deckOfCards.push(card);
            }
        }
        Collections.shuffle(deckOfCards);
    }

    /**
     * Attempts to take a card from the deck.
     * If the deck is empty, throws exception (can be improved later).
     */
    public Card takeCard() {
        if (deckOfCards.isEmpty()) {
            throw new IllegalStateException("El mazo está vacío y no hay forma de reponerlo.");
        }
        return deckOfCards.pop();
    }

    /**
     * Devuelve todas las cartas actualmente en el mazo (para restaurarlas al cargar una partida).
     */
    public List<Card> getAllCards() {
        return new ArrayList<>(deckOfCards);
    }

    /**
     * Reemplaza el mazo con todas las cartas posibles menos las que están en uso.
     * @param excluded las cartas que NO deben incluirse (cartas en mano y en mesa).
     */
    public void refillDeck(List<Card> excluded) {
        Set<String> excludedUrls = excluded.stream()
                .map(Card::getUrl)
                .collect(Collectors.toSet());

        deckOfCards.clear();

        for (EISCUnoEnum cardEnum : EISCUnoEnum.values()) {
            if (cardEnum.name().startsWith("GREEN_") ||
                    cardEnum.name().startsWith("YELLOW_") ||
                    cardEnum.name().startsWith("BLUE_") ||
                    cardEnum.name().startsWith("RED_") ||
                    cardEnum.name().startsWith("SKIP_") ||
                    cardEnum.name().startsWith("REVERSE_") ||
                    cardEnum.name().startsWith("TWO_WILD_DRAW_") ||
                    cardEnum.name().equals("FOUR_WILD_DRAW") ||
                    cardEnum.name().equals("WILD")) {

                String url = cardEnum.getFilePath();
                if (!excludedUrls.contains(url)) {
                    Card card = new Card(url, getCardValue(cardEnum.name()), getCardColor(cardEnum.name()));
                    deckOfCards.push(card);
                }
            }
        }

        Collections.shuffle(deckOfCards);
    }

    private String getCardValue(String name) {
        if (name.endsWith("0")){
            return "0";
        } else if (name.endsWith("1")){
            return "1";
        } else if (name.endsWith("2")){
            return "2";
        } else if (name.endsWith("3")){
            return "3";
        } else if (name.endsWith("4")){
            return "4";
        } else if (name.endsWith("5")){
            return "5";
        } else if (name.endsWith("6")){
            return "6";
        } else if (name.endsWith("7")){
            return "7";
        } else if (name.endsWith("8")){
            return "8";
        } else if (name.endsWith("9")){
            return "9";
        } else {
            return null;
        }

    }

    private String getCardColor(String name){
        if(name.contains("GREEN")){
            return "GREEN";
        } else if(name.contains("YELLOW")){
            return "YELLOW";
        } else if(name.contains("BLUE")){
            return "BLUE";
        } else if(name.contains("RED")){
            return "RED";
        } else {
            return null;
        }
    }

    /**
     * Takes a specified number of cards from the deck.
     * @param cardCount the number of cards to take.
     * @return a list of taken cards.
     */
    public Object takeCards(int cardCount) {
        List<Card> takenCards = new ArrayList<>();
        for (int j = 0; j < cardCount; j++) {
            if (!deckOfCards.isEmpty()) {
                takenCards.add(deckOfCards.pop());
            } else {
                throw new IllegalStateException("El mazo está vacío y no hay forma de reponerlo.");
            }
        }
        return takenCards;
    }
}
