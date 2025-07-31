package org.example.eiscuno.model.threads;

// Imports
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;
import org.example.eiscuno.model.common.GameSaver;
import org.example.eiscuno.model.common.PlayerStatsManager;

/**
 * ThreadGameOver is a thread that monitors the game state and displays a message when the game ends.
 * It checks for the game status every 500 milliseconds and shows an alert with the result.
 */
public class ThreadGameOver extends Thread {

    private final GameHandler gameHandler;
    private volatile boolean running = true;

    /**
     * Constructor for ThreadGameOver.
     * @param gameHandler The GameHandler instance that manages the game state.
     */
    public ThreadGameOver(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    /**
     * Stops the thread gracefully.
     * This method sets the running flag to false and interrupts the thread if it is sleeping.
     */
    public void stopThread() {
        running = false;
        this.interrupt(); // In case the thread is sleeping, we interrupt it to wake it up
    }

    /**
     * Runs the thread, checking the game state periodically.
     * If the game has ended, it displays an alert with the result.
     */
    @Override
    public void run() {
        while (running && !gameHandler.isGameEnded()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // If the thread is interrupted, we check if we should stop running
                if (!running) return;
                Thread.currentThread().interrupt(); // Re-interrupt the thread to maintain the interrupted status
            }
        }

        if (!running) return; // Exit if the thread has been stopped

        // Message variables for the alert
        String title, message;

        // Determine the winner and prepare the alert message
        switch (gameHandler.checkWinner()) {
            // "HUMAN" for human player, "MACHINE" for AI player, or null if no winner
            case "HUMAN":
                title = "¡Has ganado!";
                message = "¡Felicidades! Has ganado el juego.";
                PlayerStatsManager.updateStats(true, 0, false);
                System.out.println("El jugador humano ha ganado el juego.");
                break;
            case "MACHINE":
                title = "Has perdido";
                message = "Ha ganado la IA. Suerte la próxima vez.";
                PlayerStatsManager.updateStats(false, 0, false);
                System.out.println("La IA ha ganado el juego.");
                break;
            default:
                title = "Error";
                message = "No se ha podido determinar el ganador del juego.";
                System.out.println("Error al determinar el ganador del juego.");
                break;
        }

        // Display the alert on the JavaFX Application Thread
        // This is necessary because JavaFX UI components must be accessed on the JavaFX Application Thread
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            alert.setOnHidden(e -> {
                GameSaver.deleteSaveFile();
                System.out.println("Saliendo de la aplicación...");
                System.exit(0);
            });

            alert.show();
        });

        GamePauseManager.getInstance().pauseGame();
    }
}