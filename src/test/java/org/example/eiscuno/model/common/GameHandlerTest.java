package org.example.eiscuno.model.common;

import org.example.eiscuno.BaseTest;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/**
 * Pruebas unitarias para la clase GameHandler
 */
class GameHandlerTest extends BaseTest {

    private GameHandler gameHandler;
    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;

    @BeforeEach
    void setUp() {
        humanPlayer = new Player("HUMAN_PLAYER");
        machinePlayer = new Player("MACHINE_PLAYER");
        deck = new Deck();
        table = new Table();
        
        // Crear callbacks vacíos para las pruebas
        Runnable updateVisualCallback = () -> {};
        Runnable resetCardScroll = () -> {};
        
        gameHandler = new GameHandler(
            humanPlayer, 
            machinePlayer, 
            deck, 
            table, 
            false, 
            true, 
            false, 
            updateVisualCallback, 
            resetCardScroll
        );
    }

    @Test
    @DisplayName("Debería crear un GameHandler correctamente")
    void testGameHandlerCreation() {
        assertNotNull(gameHandler);
        assertEquals(humanPlayer, gameHandler.getHumanPlayer());
        assertEquals(machinePlayer, gameHandler.getMachinePlayer());
        assertEquals(deck, gameHandler.getDeck());
        assertEquals(table, gameHandler.getTable());
        assertTrue(gameHandler.getHumanTurn());
        assertFalse(gameHandler.getIASaidUno());
        assertFalse(gameHandler.getHumanSaidUno());
        assertFalse(gameHandler.isGameEnded());
    }

    @Test
    @DisplayName("Debería crear un nuevo juego correctamente")
    void testCreateNewGame() {
        Runnable updateVisualCallback = () -> {};
        Runnable resetCardScroll = () -> {};
        
        GameHandler newGame = GameHandler.createNewGame(updateVisualCallback, resetCardScroll);
        
        assertNotNull(newGame);
        assertNotNull(newGame.getHumanPlayer());
        assertNotNull(newGame.getMachinePlayer());
        assertNotNull(newGame.getDeck());
        assertNotNull(newGame.getTable());
        assertTrue(newGame.getHumanTurn());
    }

    @Test
    @DisplayName("Debería iniciar el juego repartiendo cartas correctamente")
    void testStartGame() {
        gameHandler.startGame();
        
        // Verificar que cada jugador tiene 5 cartas
        assertEquals(5, humanPlayer.getCardsPlayer().size());
        assertEquals(5, machinePlayer.getCardsPlayer().size());
        
        // Verificar que hay una carta en la mesa
        assertNotNull(gameHandler.getCurrentCardOnTable());
        
        // Verificar que la carta inicial no es especial
        assertFalse(gameHandler.getCurrentCardOnTable().isSpecial());
    }

    @Test
    @DisplayName("Debería comer cartas correctamente")
    void testEatCard() {
        int initialHumanCards = humanPlayer.getCardsPlayer().size();
        int initialDeckSize = deck.getAllCards().size();
        
        gameHandler.eatCard(humanPlayer, 3);
        
        assertEquals(initialHumanCards + 3, humanPlayer.getCardsPlayer().size());
        assertEquals(initialDeckSize - 3, deck.getAllCards().size());
    }

    @Test
    @DisplayName("Debería manejar comer cartas cuando el mazo está vacío")
    void testEatCardWithEmptyDeck() {
        // Vaciar el mazo
        while (!deck.getAllCards().isEmpty()) {
            deck.takeCard();
        }
        
        // Agregar algunas cartas a la mesa para reponer
        Card card1 = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        Card card2 = new Card("/org/example/eiscuno/cards-uno/3_blue.png", "3", "BLUE");
        table.addCardOnTheTable(card1);
        table.addCardOnTheTable(card2);
        
        int initialHumanCards = humanPlayer.getCardsPlayer().size();
        
        // Esto debería reponer el mazo automáticamente
        gameHandler.eatCard(humanPlayer, 2);
        
        assertTrue(humanPlayer.getCardsPlayer().size() > initialHumanCards);
    }

    @Test
    @DisplayName("Debería verificar si un jugador tiene cartas jugables")
    void testHasPlayableCard() {
        // Crear una carta en la mesa
        Card tableCard = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        table.addCardOnTheTable(tableCard);
        
        // Agregar una carta jugable al jugador humano
        Card playableCard = new Card("/org/example/eiscuno/cards-uno/7_red.png", "7", "RED");
        humanPlayer.addCard(playableCard);
        
        assertTrue(gameHandler.hasPlayableCard(humanPlayer));
        
        // Agregar una carta no jugable
        Card nonPlayableCard = new Card("/org/example/eiscuno/cards-uno/3_blue.png", "3", "BLUE");
        humanPlayer.addCard(nonPlayableCard);
        
        // Debería seguir siendo true porque tiene una carta jugable
        assertTrue(gameHandler.hasPlayableCard(humanPlayer));
    }

    @Test
    @DisplayName("Debería verificar que un jugador no tiene cartas jugables")
    void testHasNoPlayableCard() {
        // Crear una carta en la mesa
        Card tableCard = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        table.addCardOnTheTable(tableCard);
        
        // Agregar solo cartas no jugables al jugador humano
        Card nonPlayableCard1 = new Card("/org/example/eiscuno/cards-uno/3_blue.png", "3", "BLUE");
        Card nonPlayableCard2 = new Card("/org/example/eiscuno/cards-uno/8_green.png", "8", "GREEN");
        humanPlayer.addCard(nonPlayableCard1);
        humanPlayer.addCard(nonPlayableCard2);
        
        assertFalse(gameHandler.hasPlayableCard(humanPlayer));
    }

    @Test
    @DisplayName("Debería jugar una carta correctamente")
    void testPlayCard() {
        // Crear una carta en la mesa
        Card tableCard = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        table.addCardOnTheTable(tableCard);
        
        // Agregar una carta al jugador
        Card cardToPlay = new Card("/org/example/eiscuno/cards-uno/7_red.png", "7", "RED");
        humanPlayer.addCard(cardToPlay);
        
        int initialPlayerCards = humanPlayer.getCardsPlayer().size();
        int initialTableCards = table.getCurrentCardOnTheTable() != null ? 1 : 0;
        
        gameHandler.playCard(humanPlayer, cardToPlay);
        
        assertEquals(initialPlayerCards - 1, humanPlayer.getCardsPlayer().size());
        assertFalse(humanPlayer.getCardsPlayer().contains(cardToPlay));
        assertEquals(cardToPlay, gameHandler.getCurrentCardOnTable());
    }

    @Test
    @DisplayName("Debería manejar el clic de carta del jugador humano correctamente")
    void testHandleHumanCardClick() {
        // Crear una carta en la mesa
        Card tableCard = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        table.addCardOnTheTable(tableCard);
        
        // Agregar una carta jugable al jugador
        Card playableCard = new Card("/org/example/eiscuno/cards-uno/7_red.png", "7", "RED");
        humanPlayer.addCard(playableCard);
        
        boolean result = gameHandler.handleHumanCardClick(playableCard, () -> {});
        
        assertTrue(result);
        assertFalse(humanPlayer.getCardsPlayer().contains(playableCard));
        assertEquals(playableCard, gameHandler.getCurrentCardOnTable());
    }

    @Test
    @DisplayName("No debería permitir jugar carta cuando no es el turno del humano")
    void testHandleHumanCardClickNotHumanTurn() {
        gameHandler.passTurnToMachine();
        
        Card card = new Card("/org/example/eiscuno/cards-uno/7_red.png", "7", "RED");
        humanPlayer.addCard(card);
        
        boolean result = gameHandler.handleHumanCardClick(card, () -> {});
        
        assertFalse(result);
        assertTrue(humanPlayer.getCardsPlayer().contains(card));
    }

    @Test
    @DisplayName("No debería permitir jugar carta no válida")
    void testHandleHumanCardClickInvalidCard() {
        // Crear una carta en la mesa
        Card tableCard = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        table.addCardOnTheTable(tableCard);
        
        // Agregar una carta no jugable al jugador
        Card nonPlayableCard = new Card("/org/example/eiscuno/cards-uno/3_blue.png", "3", "BLUE");
        humanPlayer.addCard(nonPlayableCard);
        
        boolean result = gameHandler.handleHumanCardClick(nonPlayableCard, () -> {});
        
        assertFalse(result);
        assertTrue(humanPlayer.getCardsPlayer().contains(nonPlayableCard));
    }


    @Test
    @DisplayName("Debería verificar ganador cuando el jugador humano no tiene cartas")
    void testCheckWinnerHumanWins() {
        // El jugador humano no tiene cartas
        String winner = gameHandler.checkWinner();
        
        assertEquals("HUMAN", winner);
        assertTrue(gameHandler.isGameEnded());
    }

    @Test
    @DisplayName("Debería verificar ganador cuando el jugador máquina no tiene cartas")
    void testCheckWinnerMachineWins() {
        // Agregar una carta al jugador humano para que no gane
        Card card = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        humanPlayer.addCard(card);
        
        // El jugador máquina no tiene cartas
        String winner = gameHandler.checkWinner();
        
        assertEquals("MACHINE", winner);
        assertTrue(gameHandler.isGameEnded());
    }

    @Test
    @DisplayName("Debería verificar que no hay ganador cuando ambos jugadores tienen cartas")
    void testCheckWinnerNoWinner() {
        // Ambos jugadores tienen cartas
        Card card1 = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        Card card2 = new Card("/org/example/eiscuno/cards-uno/3_blue.png", "3", "BLUE");
        humanPlayer.addCard(card1);
        machinePlayer.addCard(card2);
        
        String winner = gameHandler.checkWinner();
        
        assertNull(winner);
        assertFalse(gameHandler.isGameEnded());
    }

    @Test
    @DisplayName("Debería obtener la última carta del jugador")
    void testGetLastCard() {
        Card card1 = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        Card card2 = new Card("/org/example/eiscuno/cards-uno/3_blue.png", "3", "BLUE");
        
        humanPlayer.addCard(card1);
        humanPlayer.addCard(card2);
        
        Card lastCard = gameHandler.getLastCard(humanPlayer);
        
        assertEquals(card2, lastCard);
    }

    @Test
    @DisplayName("Debería obtener null cuando el jugador no tiene cartas")
    void testGetLastCardEmptyPlayer() {
        Card lastCard = gameHandler.getLastCard(humanPlayer);
        
        assertNull(lastCard);
    }

    @Test
    @DisplayName("Debería cambiar el turno correctamente")
    void testTurnChanges() {
        assertTrue(gameHandler.getHumanTurn());
        
        gameHandler.passTurnToMachine();
        assertFalse(gameHandler.getHumanTurn());
        
        gameHandler.passTurnToHuman();
        assertTrue(gameHandler.getHumanTurn());
    }

    @Test
    @DisplayName("Debería establecer y obtener el estado de UNO correctamente")
    void testUnoState() {
        assertFalse(gameHandler.getHumanSaidUno());
        assertFalse(gameHandler.getIASaidUno());
        
        gameHandler.setHumanSaidUno(true);
        gameHandler.setIASaidUno(true);
        
        assertTrue(gameHandler.getHumanSaidUno());
        assertTrue(gameHandler.getIASaidUno());
    }
} 