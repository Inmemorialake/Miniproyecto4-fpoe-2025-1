package org.example.eiscuno.model.threads;

// Imports
import javafx.application.Platform;
import javafx.scene.image.ImageView;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;

/**
 * ThreadPlayMachine is a thread that handles the machine's turn in the game
 * It checks if it's the machine's turn, plays a card if possible, or draws a card if not.
 * The thread runs until the game ends or it is stopped.
 */
public class ThreadPlayMachine extends Thread {
    private final GameHandler gameHandler;
    private final ImageView tableImageView;
    private volatile boolean running = true;

    /**
     * Constructor for ThreadPlayMachine.
     * @param gameHandler The GameHandler instance that manages the game state.
     * @param tableImageView The ImageView representing the table where cards are played.
     */
    public ThreadPlayMachine(GameHandler gameHandler, ImageView tableImageView) {
        this.gameHandler = gameHandler;
        this.tableImageView = tableImageView;
    }

    /*
    * Starts the thread to handle the machine's turn.
    * It continuously checks if the machine can play a card or needs to draw one.
    * The thread will run until the game ends or it is stopped.
    * */
    @Override
    public void run() {
        while (running) {
            GamePauseManager.getInstance().waitIfPaused();
            if (!running || gameHandler.isGameEnded()) break;

            if (!gameHandler.getHumanTurn()) {
                handleMachineTurn();
            }
        }
    }

    /**
     * Handles the machine's turn by checking if it can play a card or needs to draw one.
     * If it can play a card, it plays the first playable card found.
     * If it cannot play any card, it draws a card and checks if it can play that card.
     */
    private void handleMachineTurn() {
        sleepSafely(1500);
        GamePauseManager.getInstance().waitIfPaused();

        Card topCard = gameHandler.getCurrentCardOnTable();
        Card cardToPlay = findPlayableCard(topCard);

        if (cardToPlay != null) {
            gameHandler.playCard(gameHandler.getMachinePlayer(), cardToPlay);
            gameHandler.applyCardEffectAndTurn(cardToPlay, false);
            gameHandler.checkWinner();

            Platform.runLater(() -> {
                tableImageView.setImage(cardToPlay.getImage());
            });

        } else {
            gameHandler.eatCard(gameHandler.getMachinePlayer(), 1);

            Card drawn = gameHandler.getLastCard(gameHandler.getMachinePlayer());
            if (drawn.canBePlayedOn(topCard)) {
                gameHandler.playCard(gameHandler.getMachinePlayer(), drawn);
                gameHandler.applyCardEffectAndTurn(drawn, false);
                gameHandler.checkWinner();

                Platform.runLater(() -> tableImageView.setImage(drawn.getImage()));
            } else {
                gameHandler.passTurnToHuman(); // Pass the turn to the human player if no playable card is drawn
            }
        }
    }

    /**
     * Finds a playable card from the machine's hand that can be played on the top card.
     * @param topCard The card currently on top of the table.
     * @return A playable card if found, otherwise null.
     */
    private Card findPlayableCard(Card topCard) {
        for (Card card : gameHandler.getMachinePlayer().getCardsPlayer()) {
            if (card.canBePlayedOn(topCard)) {
                return card;
            }
        }
        return null;
    }

    /**
     * Safely sleeps the thread for a specified number of milliseconds.
     * If interrupted, it catches the exception and does nothing.
     * @param millis The number of milliseconds to sleep.
     */
    private void sleepSafely(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException ignored) {}
    }

    /**
     * Stops the thread gracefully.
     * This method sets the running flag to false, allowing the thread to exit its loop.
     */
    public void stopThread() {
        running = false;
    }
}
