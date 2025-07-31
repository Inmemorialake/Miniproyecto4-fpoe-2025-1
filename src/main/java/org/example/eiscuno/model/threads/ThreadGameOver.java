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
                GamePauseManager.getInstance().pauseGame();
                break;
            case "MACHINE": // Machine player wins
                DialogManager.showInfoDialog("Has perdido", "Ha ganado la IA. suerte la proxima vez.");
                GamePauseManager.getInstance().pauseGame();
                break;
            default:
                System.out.println("No deberia usted estar viendo este mensaje, empiece a rezar");
                DialogManager.showInfoDialog("Error", "No se ha podido determinar el ganador del juego");
                GamePauseManager.getInstance().pauseGame();
        }

        // Delete the save file if it exists
        File saveFile = new File(PlayerStatsManager.getAppDataFolder(), "savegame.dat");
        if (saveFile.exists()) {
            saveFile.delete();
        } else {
            System.out.println("andamos curseados. El archivo de guardado no existe.");
        }

        // Exit the application
        System.exit(0);

    }
}
