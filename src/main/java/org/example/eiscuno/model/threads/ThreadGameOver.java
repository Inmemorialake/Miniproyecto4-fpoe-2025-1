package org.example.eiscuno.model.threads;

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
        switch (gameHandler.checkWinner()){
            case "HUMAN": // Human player wins
                DialogManager.showInfoDialog("¡Has ganado!", "¡Felicidades! Has ganado el juego.");
                System.out.println("El jugador humano ha ganado el juego.");
                GamePauseManager.getInstance().pauseGame();
                System.out.println("El juego ha terminado, se ha pausado el juego.");
                break;
            case "MACHINE": // Machine player wins
                DialogManager.showInfoDialog("Has perdido", "Ha ganado la IA. suerte la proxima vez.");
                System.out.println("La IA ha ganado el juego.");
                GamePauseManager.getInstance().pauseGame();
                System.out.println("El juego ha terminado, se ha pausado el juego.");
                break;
            default:
                System.out.println("No deberia usted estar viendo este mensaje, empiece a rezar");
                DialogManager.showInfoDialog("Error", "No se ha podido determinar el ganador del juego");
                System.out.println("Error al determinar el ganador del juego.");
                GamePauseManager.getInstance().pauseGame();
                System.out.println("El juego ha terminado, se ha pausado el juego.");
        }

        // Delete the save file if it exists
        File saveFile = new File(PlayerStatsManager.getAppDataFolder(), "savegame.dat");
        System.out.println("Eliminando archivo de guardado: " + saveFile.getAbsolutePath());
        if (saveFile.exists()) {
            System.out.println("Archivo de guardado encontrado, eliminando...");
            saveFile.delete();
            System.out.println("Archivo de guardado eliminado.");
        } else {
            System.out.println("andamos curseados. El archivo de guardado no existe.");
        }

        // Exit the application
        System.out.println("Saliendo de la aplicacion...");
        System.exit(0);

    }
}
