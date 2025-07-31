package org.example.eiscuno.controller;

// Imports
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.common.GameSaver;
import org.example.eiscuno.model.threads.ThreadGameOver;
import org.example.eiscuno.view.DialogManager;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;
import org.example.eiscuno.model.threads.ThreadPlayMachine;
import org.example.eiscuno.model.threads.ThreadUnoCallout;
import org.example.eiscuno.view.GameUnoStage;

import java.io.IOException;
import java.util.List;

/**
 * Controller for the Uno game, handling the game logic and UI updates.
 * This class manages the game state, player interactions, and visual updates.
 */
public class GameUnoController {

    @FXML
    private GridPane gridPaneCardsMachine;

    @FXML
    private GridPane gridPaneCardsPlayer;

    @FXML
    private ImageView tableImageView;

    @FXML
    private Label labelCurrentColor;

    @FXML
    private ImageView unoButton;

    @FXML
    private BorderPane borderPane;

    private boolean humanSaidUno = false;
    private boolean iaSaidUnoAuxiliar = false;
    private int posInitCardToShow;
    private boolean isHumanTurn;
    private final Object turnLock = new Object();
    private boolean repeatTurn = false;

    private GameHandler gameHandler;

    private ThreadUnoCallout threadUnoCallout;
    private ThreadPlayMachine threadPlayMachine;
    private ThreadGameOver threadGameOver;

    // Runnable to reset the card scroll position
    private final Runnable resetCardScroll = () -> {
        posInitCardToShow = 0;
    };

    /**
     * A map to convert color names from Spanish to English.
     * This is used for the color selection dialog when a player needs to choose a color.
     */
    private static final java.util.Map<String, String> COLOR_MAP = new java.util.HashMap<>();
    static {
        COLOR_MAP.put("ROJO", "RED");
        COLOR_MAP.put("VERDE", "GREEN");
        COLOR_MAP.put("AZUL", "BLUE");
        COLOR_MAP.put("AMARILLO", "YELLOW");
        COLOR_MAP.put("RED", "RED");
        COLOR_MAP.put("GREEN", "GREEN");
        COLOR_MAP.put("BLUE", "BLUE");
        COLOR_MAP.put("YELLOW", "YELLOW");
    }

    /**
     * Initializes the game controller.
     * This method is called when the FXML file is loaded.
     * It sets up the game handler, registers the controller, and initializes the UI.
     */
    @FXML
    public void initialize() {
        // Register the game controller with the GameUnoStage instance
        try {
            GameUnoStage.getInstance().registerGameController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initVariables();
        gameHandler.setColorChooser(this::showColorDialog); // Set the color chooser for the game handler
        updateVisuals();
        if (!gameHandler.getTable().getCards().isEmpty()) {
            tableImageView.setImage(gameHandler.getCurrentCardOnTable().getImage());
        }
        startThreads();

        //Background
        Image backgroundImage = new Image(getClass().getResource("/org/example/eiscuno/images/fondo.png").toExternalForm());
        BackgroundSize backgroundSize = new BackgroundSize(100, 100, true, true, false, true);
        BackgroundImage bgImage = new BackgroundImage(
                backgroundImage,
                BackgroundRepeat.NO_REPEAT,
                BackgroundRepeat.NO_REPEAT,
                BackgroundPosition.CENTER,
                backgroundSize
        );
        borderPane.setBackground(new Background(bgImage));
    }

    /**
     * Starts the threads that handle the game logic.
     * This method initializes and starts the threads responsible for handling UNO callouts,
     * playing the machine's turn, and checking for game over conditions.
     */
    public void startThreads() {
        threadUnoCallout = new ThreadUnoCallout(gameHandler, visible -> unoButton.setVisible(visible));
        threadPlayMachine = new ThreadPlayMachine(gameHandler, tableImageView);
        threadGameOver = new ThreadGameOver(gameHandler);
        Thread u = new Thread(threadGameOver, "ThreadGameOver");
        u.start();
        Thread t = new Thread(threadUnoCallout, "ThreadSingUNO");
        t.start();
        Thread s = new Thread(threadPlayMachine, "ThreadPlayMachine");
        s.start();
    }

    /**
     * Initializes the variables for the game.
     */
    private void initVariables() {
        this.gameHandler = GameSaver.load();

        if (gameHandler != null) {
            gameHandler.setUpdateVisualCallback(this::updateVisuals);
            gameHandler.setResetCardScroll(resetCardScroll);
            restoreCardVisuals(); // Restore visuals of cards from the saved game
            System.out.println("Partida cargada correctamente.");
        } else {
            System.out.println("Fallo al cargar partida, se crea una nueva.");
            createNewGame();
        }
    }

    /**
     * Creates a new game instance.
     * This method initializes a new GameHandler and sets the update visuals callback.
     */
    private void createNewGame() {
        this.gameHandler = GameHandler.createNewGame(this::updateVisuals, resetCardScroll);
    }

    /**
     * Restores the visuals of the cards for both players and the table.
     * This method is called when loading a saved game to ensure that all card visuals are correctly displayed.
     */
    private void restoreCardVisuals() {
        gameHandler.getHumanPlayer().getCardsPlayer().forEach(Card::restoreVisuals);
        gameHandler.getMachinePlayer().getCardsPlayer().forEach(Card::restoreVisuals);
        gameHandler.getTable().getCards().forEach(Card::restoreVisuals);
        gameHandler.getDeck().getAllCards().forEach(Card::restoreVisuals);
    }

    /**
     * Updates the visuals of the game, including player cards and the current color.
     * This method is called to refresh the UI after any game state change.
     */
    public void updateVisuals(){
        printHumanPlayerCards();
        printMachinePlayerCards();
        updateCurrentColorUI();
    }

    /**
     * Prints the human player's cards on the grid pane.
     * This method retrieves the visible cards for the human player and displays them in the UI.
     */
    public void printHumanPlayerCards() {
        gridPaneCardsPlayer.getChildren().clear();
        Card[] visibleCards = gameHandler.getCurrentVisibleCardsHumanPlayer(posInitCardToShow);

        for (int i = 0; i < visibleCards.length; i++) {
            final Card card = visibleCards[i];
            ImageView cardImageView = card.getCard();
            attachClickHandlerToCard(card, cardImageView);
            cardImageView.setTranslateX(i * 90);
            gridPaneCardsPlayer.add(cardImageView, 0, 0);
        }
    }

    /**
     * Prints the machine player's cards on the grid pane.
     */
    private void printMachinePlayerCards() {
        this.gridPaneCardsMachine.getChildren().clear();
        int numCards = gameHandler.getMachinePlayer().getCardsPlayer().size();
        if(numCards <= 8) {
            for (int i = 0; i < numCards; i++) {
                ImageView cardBack = new ImageView(new javafx.scene.image.Image(getClass().getResource("/org/example/eiscuno/cardReverse-removebg-preview.png").toExternalForm()));
                cardBack.setFitHeight(170);
                cardBack.setFitWidth(110);
                cardBack.setTranslateX(i * 75);
                this.gridPaneCardsMachine.add(cardBack, 0, 0);
            }

        } else {
            for (int i = 0; i < 8; i++) {
                ImageView cardBack = new ImageView(new javafx.scene.image.Image(getClass().getResource("/org/example/eiscuno/cardReverse-removebg-preview.png").toExternalForm()));
                cardBack.setFitHeight(170);
                cardBack.setFitWidth(110);
                cardBack.setTranslateX(i * 75);
                this.gridPaneCardsMachine.add(cardBack, 0, 0);
            }
        }
    }

    /**
     * Updates the UI to reflect the current color of the card on the table.
     * This method retrieves the color of the current card and updates the label accordingly.
     */
    private void updateCurrentColorUI() {
        String color = gameHandler.getCurrentCardOnTable().getColor();
        labelCurrentColor.setText("Color actual: " + (color != null ? color : "-"));
    }

    /**
     * Attaches a click handler to the card image view.
     * This method allows the player to click on a card to play it, and updates the table image view accordingly.
     *
     * @param card the card to attach the click handler to
     * @param cardImageView the ImageView representing the card
     */
    private void attachClickHandlerToCard(Card card, ImageView cardImageView) {
        cardImageView.setOnMouseClicked(event -> {
            boolean wasPlayed = gameHandler.handleHumanCardClick(card, () -> {
                tableImageView.setImage(card.getImage());
                updateVisuals(); // Update the visuals after playing the card
            });

            if (!wasPlayed) {
                if (!gameHandler.getHumanTurn()) {
                    showTurnError();
                } else {
                    showInvalidMoveError();
                }
            }
        });
    }

    /**
     * Shows a color selection dialog to the user.
     * This method allows the user to choose a color when playing a wild card.
     *
     * @return the selected color as a string
     */
    private String showColorDialog() {
        final String[] selectedColor = new String[1];

        ChoiceDialog<String> dialog = new ChoiceDialog<>("ROJO", List.of("ROJO", "VERDE", "AZUL", "AMARILLO"));
        dialog.setTitle("Cambio de color");
        dialog.setHeaderText(null);
        dialog.setContentText("Elige un color:");

        dialog.setOnHidden(e -> GamePauseManager.getInstance().resumeGame());

        dialog.showAndWait().ifPresent(color -> {
            selectedColor[0] = COLOR_MAP.getOrDefault(color.toUpperCase(), "RED");
        });

        return selectedColor[0] != null ? selectedColor[0] : "RED"; // fallback
    }

    /**
     * Shuts down the application gracefully.
     * This method stops all running threads and exits the application.
     */
    public void shutdownApplication() {
        // Stop all threads gracefully
        if (threadPlayMachine != null) threadPlayMachine.stopThread();
        if (threadUnoCallout != null) threadUnoCallout.stopThread();
        if (threadGameOver != null) threadGameOver.stopThread();

        Platform.exit();
        System.exit(0);
    }

    /**
     * Handles the action when the player clicks on the "Take Card" button.
     * This method checks if it's the player's turn and if they have playable cards.
     * If valid, it allows the player to take a card from the deck.
     *
     * @param event the mouse event triggered by clicking the button
     */
    @FXML
    void onHandleTakeCard(MouseEvent event) {
        if (!gameHandler.getHumanTurn()) {
            showTurnError();
            return;
        }
        if (gameHandler.hasPlayableCard(gameHandler.getHumanPlayer())) {
            showInvalidTryToTakeCardError();
            return;
        }
        gameHandler.eatCard(gameHandler.getHumanPlayer(), 1);
        posInitCardToShow = 0;
        printHumanPlayerCards();
        gameHandler.passTurnToMachine();
    }

    /**
     * Handles the action when the player clicks on the "UNO" button.
     * This method checks if the player has only one card left and has not declared UNO yet.
     * If valid, it declares UNO or calls out the machine for not declaring UNO.
     *
     * @param event the mouse event triggered by clicking the button
     */
    @FXML
    void onHandleUno(MouseEvent event) {
        if (gameHandler.getHumanPlayer().getCardsPlayer().size() == 1 && !gameHandler.getHumanSaidUno()) {
            gameHandler.setHumanSaidUno(true);
            DialogManager.showInfoDialog("UNO declarado", "¡Has declarado UNO correctamente!");
            GamePauseManager.getInstance().pauseGame();
            return;
        }
        if (gameHandler.getMachinePlayer().getCardsPlayer().size() == 1 && !gameHandler.getIASaidUno()) {
            gameHandler.eatCard(gameHandler.getMachinePlayer(), 1);
            DialogManager.showInfoDialog("UNO callout a la máquina", "¡La máquina no dijo UNO! Le has hecho comer una carta.");
            GamePauseManager.getInstance().pauseGame();
            return;
        }
    }

    /**
     * Handles the action when the player clicks on the "Exit" button.
     * This method shuts down the application gracefully.
     *
     * @param event the mouse event triggered by clicking the button
     */
    @FXML
    private void handleExit(MouseEvent event) {
        shutdownApplication();
    }

    /**
     * Shows an error dialog when it's not the player's turn.
     * This method is called when the player tries to play a card or take a card when it's not their turn.
     */
    private void showTurnError() {
        DialogManager.showInfoDialog("Turno incorrecto", "No es tu turno. Espera a que la máquina juegue.");
        GamePauseManager.getInstance().pauseGame();
    }

    /**
     * Shows an error dialog when the player tries to play an invalid move.
     * This method is called when the player attempts to play a card that does not match the current card on the table.
     */
    private void showInvalidMoveError() {
        DialogManager.showInfoDialog("Jugada inválida", "No puedes jugar esa carta. Debe coincidir en color, número o símbolo con la carta de la mesa.");
        GamePauseManager.getInstance().pauseGame();
    }

    /**
     * Shows an error dialog when the player tries to take a card while having playable cards.
     * This method is called when the player attempts to take a card while they still have playable cards in hand.
     */
    private void showInvalidTryToTakeCardError(){
        DialogManager.showInfoDialog("Intento inválido", "No puedes tomar una carta si tienes cartas jugables. Juega una carta primero.");
        GamePauseManager.getInstance().pauseGame();
    }

    /**
     * Handles the "Back" button action to show the previous set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleBack(MouseEvent event) {
        if (this.posInitCardToShow > 0) {
            this.posInitCardToShow--;
            printHumanPlayerCards();
        }
    }

    /**
     * Handles the "Next" button action to show the next set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleNext(MouseEvent event) {
        if (this.posInitCardToShow < gameHandler.getHumanPlayer().getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printHumanPlayerCards();
        }
    }
}