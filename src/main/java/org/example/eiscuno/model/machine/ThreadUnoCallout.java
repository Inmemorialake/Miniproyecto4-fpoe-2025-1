package org.example.eiscuno.model.machine;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.example.eiscuno.model.common.DialogManager;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;

import java.util.Random;
import java.util.function.Consumer;

public class ThreadUnoCallout extends Thread {
    private final GameHandler gameHandler;
    private final Consumer<Boolean> showUnoButtonCallback; // true = mostrar, false = ocultar
    private volatile boolean running = true;

    private boolean humanUnoHandled = false;
    private boolean iaUnoHandled = false;

    public ThreadUnoCallout(GameHandler gameHandler, Consumer<Boolean> showUnoButtonCallback) {
        this.gameHandler = gameHandler;
        this.showUnoButtonCallback = showUnoButtonCallback;
    }

    @Override
    public void run() {
        while (running) {
            GamePauseManager.getInstance().waitIfPaused();

            int humanCards = gameHandler.getHumanPlayer().getCardsPlayer().size();
            int iaCards = gameHandler.getMachinePlayer().getCardsPlayer().size();

            // RESET FLAGS si ya no tienen una sola carta
            if (humanCards > 1) {
                gameHandler.setHumanSaidUno(false);
                humanUnoHandled = false;
                Platform.runLater(() -> showUnoButtonCallback.accept(false));
            }

            if (iaCards > 1) {
                gameHandler.setIASaidUno(false);
                iaUnoHandled = false;
            }

            // --- HUMANO ---
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

            // --- IA ---
            if (iaCards == 1 && !gameHandler.getIASaidUno() && !iaUnoHandled) {
                iaUnoHandled = true;

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

    public void stopThread() {
        running = false;
    }
}
