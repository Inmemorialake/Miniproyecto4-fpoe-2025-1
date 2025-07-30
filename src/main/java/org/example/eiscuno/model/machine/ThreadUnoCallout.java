package org.example.eiscuno.model.machine;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;

import java.util.Random;
import java.util.function.Consumer;

public class ThreadUnoCallout extends Thread {
    private final GameHandler gameHandler;
    private final Consumer<Boolean> showUnoButtonCallback; // true = mostrar, false = ocultar
    private volatile boolean running = true;

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

            // Si alguien tiene más de una carta, se reinician los flags
            if (humanCards > 1 && gameHandler.getHumanSaidUno()) {
                gameHandler.setHumanSaidUno(false);
            }

            if (iaCards > 1 && gameHandler.getIASaidUno()) {
                gameHandler.setIASaidUno(false);
            }

            // Si el humano tiene 1 carta y no ha dicho UNO, mostrar botón y esperar penalización
            if (humanCards == 1 && !gameHandler.getHumanSaidUno()) {
                Platform.runLater(() -> showUnoButtonCallback.accept(true));

                new Thread(() -> {
                    try {
                        Thread.sleep(3000);
                        GamePauseManager.getInstance().waitIfPaused();

                        if (!gameHandler.getHumanSaidUno() && gameHandler.getHumanPlayer().getCardsPlayer().size() == 1) {
                            gameHandler.eatCard(gameHandler.getHumanPlayer(), 1);
                            Platform.runLater(() -> {
                                GamePauseManager.getInstance().pauseGame();
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("¡UNO no declarado!");
                                alert.setHeaderText(null);
                                alert.setContentText("¡Olvidaste gritar UNO! Tomas 1 carta como penalización.");
                                alert.setOnHidden(e -> GamePauseManager.getInstance().resumeGame());
                                alert.showAndWait();
                            });
                        }
                    } catch (InterruptedException ignored) {}
                }).start();
            } else {
                Platform.runLater(() -> showUnoButtonCallback.accept(false));
            }

            // Si la IA tiene 1 carta y no ha dicho UNO, hacer que lo diga en 2-4 segundos
            if (iaCards == 1 && !gameHandler.getIASaidUno()) {
                new Thread(() -> {
                    try {
                        Thread.sleep(2000 + new Random().nextInt(2000));
                        GamePauseManager.getInstance().waitIfPaused();

                        if (!gameHandler.getIASaidUno() && gameHandler.getMachinePlayer().getCardsPlayer().size() == 1) {
                            gameHandler.setIASaidUno(true);
                            Platform.runLater(() -> {
                                GamePauseManager.getInstance().pauseGame();
                                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                                alert.setTitle("¡UNO!");
                                alert.setHeaderText(null);
                                alert.setContentText("¡La máquina ha gritado UNO!");
                                alert.setOnHidden(e -> GamePauseManager.getInstance().resumeGame());
                                alert.showAndWait();
                            });
                        }
                    } catch (InterruptedException ignored) {}
                }).start();
            }

            try {
                Thread.sleep(500); // chequeo cada 0.5 segundos
            } catch (InterruptedException ignored) {}
        }
    }

    public void stopThread() {
        running = false;
    }
}
