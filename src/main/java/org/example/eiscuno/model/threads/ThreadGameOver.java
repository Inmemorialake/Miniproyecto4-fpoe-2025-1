package org.example.eiscuno.model.threads;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;
import org.example.eiscuno.model.common.PlayerStatsManager;
import org.example.eiscuno.view.DialogManager;

import java.io.File;

public class ThreadGameOver extends Thread {

    private final GameHandler gameHandler;

    public ThreadGameOver(GameHandler gameHandler) {
        this.gameHandler = gameHandler;
    }

    @Override
    public void run() {
        while (!gameHandler.isGameEnded()) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {
                Thread.currentThread().interrupt();
            }
        }

        // Mensajes según resultado
        String title, message;

        switch (gameHandler.checkWinner()) {
            case "HUMAN":
                title = "¡Has ganado!";
                message = "¡Felicidades! Has ganado el juego.";
                PlayerStatsManager.updateStats(true,0,false);
                System.out.println("El jugador humano ha ganado el juego.");
                break;
            case "MACHINE":
                title = "Has perdido";
                message = "Ha ganado la IA. Suerte la próxima vez.";
                PlayerStatsManager.updateStats(false,0,false);
                System.out.println("La IA ha ganado el juego.");
                break;
            default:
                title = "Error";
                message = "No se ha podido determinar el ganador del juego.";
                System.out.println("Error al determinar el ganador del juego.");
                break;
        }

        // Mostrar diálogo en hilo JavaFX
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);

            alert.setOnHidden(e -> {
                // 🔄 Al cerrar el diálogo
                File saveFile = new File(PlayerStatsManager.getAppDataFolder(), "savegame.dat");
                System.out.println("Eliminando archivo de guardado: " + saveFile.getAbsolutePath());

                if (saveFile.exists()) {
                    System.out.println("Archivo encontrado. Eliminando...");
                    boolean deleted = saveFile.delete();
                    System.out.println(deleted ? "Archivo eliminado." : "No se pudo eliminar el archivo.");
                } else {
                    System.out.println("Archivo de guardado no existe.");
                }

                System.out.println("Saliendo de la aplicación...");
                System.exit(0);
            });

            alert.show();
        });
        GamePauseManager.getInstance().pauseGame();
    }
}
