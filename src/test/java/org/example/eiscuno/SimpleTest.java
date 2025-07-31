package org.example.eiscuno;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba simple para verificar que la configuración básica funciona
 */
class SimpleTest extends BaseTest {

    @Test
    @DisplayName("Prueba básica de que JUnit funciona")
    void testBasicFunctionality() {
        // Arrange
        int a = 5;
        int b = 3;
        
        // Act
        int result = a + b;
        
        // Assert
        assertEquals(8, result);
        assertTrue(result > 0);
        assertNotNull("Test string");
    }

    @Test
    @DisplayName("Prueba de que JavaFX está disponible")
    void testJavaFXAvailable() {
        // Verificar que JavaFX está disponible
        assertNotNull(javafx.application.Platform.class);
        System.out.println("JavaFX está disponible para las pruebas");
    }
} 