package org.example.eiscuno.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.util.Duration;
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
    private Button unoButton;

    @FXML
    private BorderPane borderPane;

    @FXML
    private ImageView unoImageView;

    private boolean humanSaidUno = false;
    private boolean iaSaidUnoAuxiliar = false;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private int posInitCardToShow;
    private boolean isHumanTurn;
    private final Object turnLock = new Object();
    private boolean repeatTurn = false;

    private GameHandler gameHandler;

    private int posInitCardToShow = 0;

    private ThreadUnoCallout threadUnoCallout;
    private ThreadPlayMachine threadPlayMachine;
    private ThreadGameOver threadGameOver;

    private final Object turnLock = new Object();
    private boolean repeatTurn = false;

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

    @FXML
    public void initialize() {
        //Le pasamos este controller a la stage para que al cerrarse pueda usar nuestra funcion de cerrar
        try {
            GameUnoStage.getInstance().registerGameController(this);
        } catch (IOException e) {
            e.printStackTrace();
        }

        initVariables();
        gameHandler.setColorChooser(this::showColorDialog); // Le pasamos al gameHandler la funcion para que el usuario pueda elegir un color
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
            restoreCardVisuals(); //Cuando cargamos una partida, las visuales de las cartas se pierden por lo que tenemos que volver a ponerlas :3
            System.out.println("Partida cargada correctamente.");
        } else {
            System.out.println("Fallo al cargar partida, se crea una nueva.");
            createNewGame();
        }
    }

    private void createNewGame() {
        this.gameHandler = GameHandler.createNewGame(this::updateVisuals);
    }

    private void restoreCardVisuals() {
        gameHandler.getHumanPlayer().getCardsPlayer().forEach(Card::restoreVisuals);
        gameHandler.getMachinePlayer().getCardsPlayer().forEach(Card::restoreVisuals);
        gameHandler.getTable().getCards().forEach(Card::restoreVisuals);
        gameHandler.getDeck().getAllCards().forEach(Card::restoreVisuals);
    }

    public void updateVisuals(){
        printHumanPlayerCards();
        printMachinePlayerCards();
        updateCurrentColorUI();
    }

    public void printHumanPlayerCards() {
        gridPaneCardsPlayer.getChildren().clear();
        Card[] visibleCards = gameHandler.getCurrentVisibleCardsHumanPlayer(posInitCardToShow);

        for (int i = 0; i < visibleCards.length; i++) {
            final Card card = visibleCards[i];
            final int index = i;
            ImageView cardImageView = card.getCard();
            attachClickHandlerToCard(card, cardImageView); // Mover comportamiento a una función aparte
            cardImageView.setTranslateX(i * 90);
            this.gridPaneCardsPlayer.add(cardImageView, 0, 0);
        }
    }

    /**
     * Prints the machine player's cards on the grid pane.
     */
    private void printMachinePlayerCards() {
        this.gridPaneCardsMachine.getChildren().clear();
        int numCards = machinePlayer.getCardsPlayer().size();
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

    private void updateCurrentColorUI() {
        String color = gameHandler.getCurrentCardOnTable().getColor();
        labelCurrentColor.setText("Color actual: " + (color != null ? color : "-"));
    }

    private void attachClickHandlerToCard(Card card, ImageView cardImageView) {
        cardImageView.setOnMouseClicked(event -> {
            boolean wasPlayed = gameHandler.handleHumanCardClick(card, () -> {
                tableImageView.setImage(card.getImage());
                updateVisuals(); // actualizar visualmente
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

    public void shutdownApplication() {
        // Detener hilos que tengas vivos (ejemplo)
        if (threadPlayMachine != null) threadPlayMachine.stopThread();
        if (threadUnoCallout != null) threadUnoCallout.stopThread();
        if (threadGameOver != null) threadGameOver.stopThread();

        Platform.exit();
        System.exit(0);
    }

    @FXML
    void onHandleTakeCard(ActionEvent event) {
        if (!gameHandler.getHumanTurn()) {
            showTurnError();
            return;
        }
        if (gameHandler.hasPlayableCard(gameHandler.getHumanPlayer())) {
            showInvalidTryToTakeCardError();
            return;
        }
        gameHandler.eatCard(gameHandler.getHumanPlayer(), 1);
        printHumanPlayerCards();
        gameHandler.passTurnToMachine();
    }

    @FXML
    void onHandleUno(ActionEvent event) {
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

    @FXML
    private void handleExit() {
        shutdownApplication();
    }

    private void showTurnError() {
        DialogManager.showInfoDialog("Turno incorrecto", "No es tu turno. Espera a que la máquina juegue.");
        GamePauseManager.getInstance().pauseGame();
    }

    private void showInvalidMoveError() {
        DialogManager.showInfoDialog("Jugada inválida", "No puedes jugar esa carta. Debe coincidir en color, número o símbolo con la carta de la mesa.");
        GamePauseManager.getInstance().pauseGame();
    }

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
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printHumanPlayerCards();
        }
    }
}