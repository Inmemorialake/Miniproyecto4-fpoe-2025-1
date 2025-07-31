package org.example.eiscuno;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;

import java.util.concurrent.CountDownLatch;

/**
 * Clase base para todas las pruebas que requieren JavaFX
 * Inicializa el toolkit de JavaFX antes de ejecutar las pruebas
 */
public abstract class BaseTest {

    private static boolean javaFXInitialized = false;

    @BeforeAll
    static void initJfxToolkit() throws InterruptedException {
        if (javaFXInitialized) {
            return; // Ya inicializado
        }
        
        try {
            // Arranca el toolkit de JavaFX una sola vez
            CountDownLatch latch = new CountDownLatch(1);
            Platform.startup(latch::countDown);
            latch.await();
            
            javaFXInitialized = true;
            System.out.println("JavaFX inicializado correctamente para las pruebas");
            
        } catch (Exception e) {
            throw new RuntimeException("Error crítico inicializando JavaFX: " + e.getMessage(), e);
        }
    }
} 