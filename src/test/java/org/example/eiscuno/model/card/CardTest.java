package org.example.eiscuno.model.card;

import org.example.eiscuno.BaseTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas unitarias para la clase Card
 */
class CardTest extends BaseTest {

    private Card redCard;
    private Card blueCard;
    private Card wildCard;
    private Card plusTwoCard;
    private Card plusFourCard;
    private Card skipCard;
    private Card reverseCard;

    @BeforeEach
    void setUp() {
        // Crear cartas de prueba
        redCard = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        blueCard = new Card("/org/example/eiscuno/cards-uno/3_blue.png", "3", "BLUE");
        wildCard = new Card("/org/example/eiscuno/cards-uno/wild.png", null, "BLACK");
        plusTwoCard = new Card("/org/example/eiscuno/cards-uno/2_wild_draw_blue.png", "2", "BLUE");
        plusFourCard = new Card("/org/example/eiscuno/cards-uno/4_wild_draw.png", "4", "BLACK");
        skipCard = new Card("/org/example/eiscuno/cards-uno/skip_red.png", null, "RED");
        reverseCard = new Card("/org/example/eiscuno/cards-uno/reverse_green.png", null, "GREEN");
    }

    @Test
    @DisplayName("Debería crear una carta correctamente con todos sus atributos")
    void testCardCreation() {
        // Arrange & Act ya hecho en setUp()
        
        // Assert
        assertEquals("5", redCard.getValue());
        assertEquals("RED", redCard.getColor());
        assertEquals("/org/example/eiscuno/cards-uno/5_red.png", redCard.getUrl());
        assertNotNull(redCard.getImage());
        assertNotNull(redCard.getCard());
    }

    @Test
    @DisplayName("Debería identificar correctamente una carta salvaje")
    void testIsWildCard() {
        assertTrue(wildCard.isWildCard());
        assertFalse(redCard.isWildCard());
        assertFalse(blueCard.isWildCard());
    }

    @Test
    @DisplayName("Debería identificar correctamente una carta +4")
    void testIsPlusFour() {
        assertTrue(plusFourCard.isPlusFour());
        assertFalse(wildCard.isPlusFour());
        assertFalse(redCard.isPlusFour());
    }

    @Test
    @DisplayName("Debería identificar correctamente una carta +2")
    void testIsPlusTwo() {
        assertTrue(plusTwoCard.isPlusTwo());
        assertFalse(wildCard.isPlusTwo());
        assertFalse(redCard.isPlusTwo());
    }

    @Test
    @DisplayName("Debería identificar correctamente una carta skip")
    void testIsSkip() {
        assertTrue(skipCard.isSkip());
        assertFalse(redCard.isSkip());
        assertFalse(wildCard.isSkip());
    }

    @Test
    @DisplayName("Debería identificar correctamente una carta reverse")
    void testIsReverse() {
        assertTrue(reverseCard.isReverse());
        assertFalse(redCard.isReverse());
        assertFalse(wildCard.isReverse());
    }

    @Test
    @DisplayName("Debería identificar correctamente una carta skip o reverse")
    void testIsSkipOrReverse() {
        assertTrue(skipCard.isSkipOrReverse());
        assertTrue(reverseCard.isSkipOrReverse());
        assertFalse(redCard.isSkipOrReverse());
        assertFalse(wildCard.isSkipOrReverse());
    }

    @Test
    @DisplayName("Debería identificar correctamente una carta especial")
    void testIsSpecial() {
        assertTrue(wildCard.isSpecial());
        assertTrue(plusFourCard.isSpecial());
        assertTrue(plusTwoCard.isSpecial());
        assertTrue(skipCard.isSpecial());
        assertTrue(reverseCard.isSpecial());
        assertFalse(redCard.isSpecial());
    }

    @Test
    @DisplayName("Debería poder jugar una carta del mismo color")
    void testCanBePlayedOnSameColor() {
        Card redCard2 = new Card("/org/example/eiscuno/cards-uno/7_red.png", "7", "RED");
        
        assertTrue(redCard2.canBePlayedOn(redCard));
        assertTrue(redCard.canBePlayedOn(redCard2));
    }

    @Test
    @DisplayName("Debería poder jugar una carta del mismo valor")
    void testCanBePlayedOnSameValue() {
        Card blueCard5 = new Card("/org/example/eiscuno/cards-uno/5_blue.png", "5", "BLUE");
        
        assertTrue(blueCard5.canBePlayedOn(redCard)); // Mismo valor (5)
        assertTrue(redCard.canBePlayedOn(blueCard5)); // Mismo valor (5)
    }

    @Test
    @DisplayName("No debería poder jugar una carta de color y valor diferentes")
    void testCannotBePlayedOnDifferentColorAndValue() {
        Card greenCard8 = new Card("/org/example/eiscuno/cards-uno/8_green.png", "8", "GREEN");
        
        assertFalse(greenCard8.canBePlayedOn(redCard));
        assertFalse(redCard.canBePlayedOn(greenCard8));
    }

    @Test
    @DisplayName("Debería poder jugar una carta salvaje en cualquier carta")
    void testWildCardCanBePlayedOnAnyCard() {
        assertTrue(wildCard.canBePlayedOn(redCard));
        assertTrue(wildCard.canBePlayedOn(blueCard));
        assertTrue(wildCard.canBePlayedOn(skipCard));
    }

    @Test
    @DisplayName("Debería poder jugar una carta +4 en cualquier carta")
    void testPlusFourCanBePlayedOnAnyCard() {
        assertTrue(plusFourCard.canBePlayedOn(redCard));
        assertTrue(plusFourCard.canBePlayedOn(blueCard));
        assertTrue(plusFourCard.canBePlayedOn(skipCard));
    }

    @Test
    @DisplayName("Debería poder jugar una carta +2 en otra carta +2")
    void testPlusTwoCanBePlayedOnPlusTwo() {
        Card plusTwoRed = new Card("/org/example/eiscuno/cards-uno/2_wild_draw_red.png", "2", "RED");
        
        assertTrue(plusTwoRed.canBePlayedOn(plusTwoCard));
        assertTrue(plusTwoCard.canBePlayedOn(plusTwoRed));
    }

    @Test
    @DisplayName("Debería poder jugar una carta skip en otra carta skip")
    void testSkipCanBePlayedOnSkip() {
        Card skipBlue = new Card("/org/example/eiscuno/cards-uno/skip_blue.png", null, "BLUE");
        
        assertTrue(skipBlue.canBePlayedOn(skipCard));
        assertTrue(skipCard.canBePlayedOn(skipBlue));
    }

    @Test
    @DisplayName("Debería poder jugar una carta reverse en otra carta reverse")
    void testReverseCanBePlayedOnReverse() {
        Card reverseBlue = new Card("/org/example/eiscuno/cards-uno/reverse_blue.png", null, "BLUE");
        
        assertTrue(reverseBlue.canBePlayedOn(reverseCard));
        assertTrue(reverseCard.canBePlayedOn(reverseBlue));
    }

    @Test
    @DisplayName("Debería poder cambiar el color de una carta")
    void testSetColor() {
        wildCard.setColor("GREEN");
        assertEquals("GREEN", wildCard.getColor());
        
        wildCard.setColor("BLUE");
        assertEquals("BLUE", wildCard.getColor());
    }

    @Test
    @DisplayName("Debería establecer color BLACK por defecto cuando se pasa null")
    void testSetColorWithNull() {
        wildCard.setColor(null);
        assertEquals("BLACK", wildCard.getColor());
    }

    @Test
    @DisplayName("Debería manejar excepciones en canBePlayedOn correctamente")
    void testCanBePlayedOnWithException() {
        Card invalidCard = new Card("/org/example/eiscuno/cards-uno/5_red.png", "5", "RED");
        
        // Crear una carta con valores null para provocar excepción
        Card nullCard = new Card("/org/example/eiscuno/cards-uno/5_red.png", null, null);
        
        assertFalse(nullCard.canBePlayedOn(invalidCard));
    }
} 