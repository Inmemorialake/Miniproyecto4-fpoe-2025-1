package org.example.eiscuno.model.threads;

// Imports
import javafx.application.Platform;
import org.example.eiscuno.view.DialogManager;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;

import java.util.Random;
import java.util.function.Consumer;

/**
 * ThreadUnoCallout is a thread that monitors the game state to handle the UNO callout for both human and AI players.
 * It shows a button for the UNO callout when a player has only one card left and has not called UNO yet.
 * If the player fails to call UNO in time, they receive a penalty.
 */
public class ThreadUnoCallout extends Thread {
    private final GameHandler gameHandler;
    private final Consumer<Boolean> showUnoButtonCallback; // true = show button, false = hide button
    private volatile boolean running = true;

    private boolean humanUnoHandled = false;
    private boolean iaUnoHandled = false;

    /**
     * Constructor for ThreadUnoCallout.
     * @param gameHandler The GameHandler instance that manages the game state.
     * @param showUnoButtonCallback A callback to show or hide the UNO button.
     */
    public ThreadUnoCallout(GameHandler gameHandler, Consumer<Boolean> showUnoButtonCallback) {
        this.gameHandler = gameHandler;
        this.showUnoButtonCallback = showUnoButtonCallback;
    }

    /*
    * This method is called when the thread starts.
    * It continuously checks the number of cards each player has and whether they have called UNO.
    * */
    @Override
    public void run() {
        while (running) {
            GamePauseManager.getInstance().waitIfPaused();

            int humanCards = gameHandler.getHumanPlayer().getCardsPlayer().size();
            int iaCards = gameHandler.getMachinePlayer().getCardsPlayer().size();

            // Reset the UNO callout flags if the number of cards is greater than 1
            if (humanCards > 1) {
                gameHandler.setHumanSaidUno(false);
                humanUnoHandled = false;
            }

            if (iaCards > 1) {
                gameHandler.setIASaidUno(false);
                iaUnoHandled = false;
            }

            // Hide the UNO button if neither player has only one card left or has called UNO
            if(!(humanCards == 1 && !gameHandler.getHumanSaidUno()) && !(iaCards == 1 && !gameHandler.getIASaidUno())){
                Platform.runLater(() -> showUnoButtonCallback.accept(false));
            }

            // --- HUMAN ---
            if (humanCards == 1 && !gameHandler.getHumanSaidUno() && !humanUnoHandled) {
                humanUnoHandled = true;
                Platform.runLater(() -> showUnoButtonCallback.accept(true));

                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        GamePauseManager.getInstance().waitIfPaused();
                        if (!gameHandler.getHumanSaidUno() && gameHandler.getHumanPlayer().getCardsPlayer().size() == 1) {
                            gameHandler.eatCard(gameHandler.getHumanPlayer(), 1);
                            DialogManager.showInfoDialog("¡La IA te dijo UNO!", "¡La IA te gritó UNO! Tomas 1 carta como penalización.");
                            GamePauseManager.getInstance().pauseGame();
                        }
                    } catch (InterruptedException ignored) {}
                }).start();
            }

            // --- AI ---
            if (iaCards == 1 && !gameHandler.getIASaidUno() && !iaUnoHandled) {
                iaUnoHandled = true;
                Platform.runLater(() -> showUnoButtonCallback.accept(true));

                new Thread(() -> {
                    try {
                        Thread.sleep(2000 + new Random().nextInt(2000));
                        GamePauseManager.getInstance().waitIfPaused();

                        if (!gameHandler.getIASaidUno() && gameHandler.getMachinePlayer().getCardsPlayer().size() == 1) {
                            gameHandler.setIASaidUno(true);
                            DialogManager.showInfoDialog("¡UNO!", "¡La IA gritó UNO!");
                            GamePauseManager.getInstance().pauseGame();
                        }
                    } catch (InterruptedException ignored) {}
                }).start();
            }

            try {
                Thread.sleep(500);
            } catch (InterruptedException ignored) {}
        }
    }

    /**
     * Stops the thread by setting the running flag to false.
     * This will cause the run method to exit its loop and terminate the thread.
     */
    public void stopThread() {
        running = false;
    }
}
