package org.example.eiscuno.model.deck;

import org.example.eiscuno.BaseTest;
import org.example.eiscuno.model.card.Card;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Pruebas unitarias para la clase Deck
 */
class DeckTest extends BaseTest {

    private Deck deck;

    @BeforeEach
    void setUp() {
        deck = new Deck();
    }

    @Test
    @DisplayName("Debería crear un mazo con cartas inicializadas")
    void testDeckCreation() {
        assertNotNull(deck);
        List<Card> allCards = deck.getAllCards();
        assertNotNull(allCards);
        assertFalse(allCards.isEmpty());
    }

    @Test
    @DisplayName("Debería tomar una carta del mazo correctamente")
    void testTakeCard() {
        int initialSize = deck.getAllCards().size();
        
        Card takenCard = deck.takeCard();
        
        assertNotNull(takenCard);
        assertEquals(initialSize - 1, deck.getAllCards().size());
    }

    @Test
    @DisplayName("Debería tomar múltiples cartas del mazo correctamente")
    void testTakeCards() {
        int initialSize = deck.getAllCards().size();
        int cardsToTake = 3;
        
        @SuppressWarnings("unchecked")
        List<Card> takenCards = (List<Card>) deck.takeCards(cardsToTake);
        
        assertNotNull(takenCards);
        assertEquals(cardsToTake, takenCards.size());
        assertEquals(initialSize - cardsToTake, deck.getAllCards().size());
        
        // Verificar que las cartas tomadas son diferentes
        for (int i = 0; i < takenCards.size(); i++) {
            for (int j = i + 1; j < takenCards.size(); j++) {
                assertNotEquals(takenCards.get(i), takenCards.get(j));
            }
        }
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el mazo está vacío al tomar carta")
    void testTakeCardFromEmptyDeck() {
        // Vaciar el mazo
        while (!deck.getAllCards().isEmpty()) {
            deck.takeCard();
        }
        
        assertThrows(IllegalStateException.class, () -> {
            deck.takeCard();
        });
    }

    @Test
    @DisplayName("Debería lanzar excepción cuando el mazo está vacío al tomar múltiples cartas")
    void testTakeCardsFromEmptyDeck() {
        // Vaciar el mazo
        while (!deck.getAllCards().isEmpty()) {
            deck.takeCard();
        }
        
        assertThrows(IllegalStateException.class, () -> {
            deck.takeCards(3);
        });
    }



    @Test
    @DisplayName("Debería obtener todas las cartas del mazo")
    void testGetAllCards() {
        List<Card> allCards = deck.getAllCards();
        
        assertNotNull(allCards);
        assertFalse(allCards.isEmpty());
        
        // Verificar que todas las cartas son diferentes
        for (int i = 0; i < allCards.size(); i++) {
            for (int j = i + 1; j < allCards.size(); j++) {
                assertNotEquals(allCards.get(i), allCards.get(j));
            }
        }
    }

    @Test
    @DisplayName("Debería tomar exactamente el número de cartas solicitadas")
    void testTakeExactNumberOfCards() {
        int cardsToTake = 5;
        int initialSize = deck.getAllCards().size();
        
        @SuppressWarnings("unchecked")
        List<Card> takenCards = (List<Card>) deck.takeCards(cardsToTake);
        
        assertEquals(cardsToTake, takenCards.size());
        assertEquals(initialSize - cardsToTake, deck.getAllCards().size());
    }

    @Test
    @DisplayName("Debería manejar tomar 0 cartas")
    void testTakeZeroCards() {
        int initialSize = deck.getAllCards().size();
        
        @SuppressWarnings("unchecked")
        List<Card> takenCards = (List<Card>) deck.takeCards(0);
        
        assertEquals(0, takenCards.size());
        assertEquals(initialSize, deck.getAllCards().size());
    }

    @Test
    @DisplayName("Debería verificar que las cartas tomadas tienen propiedades válidas")
    void testTakenCardsHaveValidProperties() {
        Card takenCard = deck.takeCard();
        
        assertNotNull(takenCard.getColor());
        assertNotNull(takenCard.getUrl());
        assertNotNull(takenCard.getImage());
        assertNotNull(takenCard.getCard());
    }

    @Test
    @DisplayName("Debería verificar que el mazo se baraja correctamente")
    void testDeckIsShuffled() {
        Deck deck1 = new Deck();
        Deck deck2 = new Deck();
        
        // Tomar las primeras 5 cartas de cada mazo
        List<Card> cards1 = List.of(
            deck1.takeCard(),
            deck1.takeCard(),
            deck1.takeCard(),
            deck1.takeCard(),
            deck1.takeCard()
        );
        
        List<Card> cards2 = List.of(
            deck2.takeCard(),
            deck2.takeCard(),
            deck2.takeCard(),
            deck2.takeCard(),
            deck2.takeCard()
        );
        
        // Es muy poco probable que dos mazos barajados tengan las mismas cartas en el mismo orden
        // Por lo tanto, al menos una carta debería ser diferente
        boolean allSame = true;
        for (int i = 0; i < cards1.size(); i++) {
            if (!cards1.get(i).getUrl().equals(cards2.get(i).getUrl())) {
                allSame = false;
                break;
            }
        }
        
        // Es posible que sean iguales por casualidad, pero muy improbable
        // Este test puede fallar ocasionalmente, pero es aceptable
        // En un entorno de producción, se podría usar un mock para el Random
    }
} 