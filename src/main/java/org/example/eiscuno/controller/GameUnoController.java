package org.example.eiscuno.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.GamePauseManager;
import org.example.eiscuno.model.common.PlayerStatsManager;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadUnoCallout;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.CountDownLatch;

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

    private GameHandler gameHandler;

    private int posInitCardToShow = 0;

    private ThreadUnoCallout threadUnoCallout;
    private ThreadPlayMachine threadPlayMachine;

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
        System.out.println("funka");
        initVariables();
        setChooseColorHumanBehaviour();
        System.out.println("funka");
        printHumanPlayerCards();
        printMachinePlayerCards();
        System.out.println("funka");
        if (!gameHandler.getTable().getCards().isEmpty()) {
            tableImageView.setImage(gameHandler.getCurrentCardOnTable().getImage());
            System.out.println("fakun");
        }
        startThreads();
        System.out.println("funka");
    }

    public void startThreads() {
        threadUnoCallout = new ThreadUnoCallout(gameHandler, visible -> unoButton.setVisible(visible));
        threadPlayMachine = new ThreadPlayMachine(gameHandler, tableImageView, this::printMachinePlayerCards);
        Thread t = new Thread(threadUnoCallout, "ThreadSingUNO");
        t.start();
        Thread s = new Thread(threadPlayMachine, "ThreadPlayMachine");
        s.start();

        Timeline unoCheckTimeline = new Timeline(new KeyFrame(Duration.seconds(0.5), event -> checkUnoConditions()));
        unoCheckTimeline.setCycleCount(Animation.INDEFINITE);
        unoCheckTimeline.play();
    }

    private void initVariables() {
        File saveFile = new File(PlayerStatsManager.getAppDataFolder(), "savegame.dat");

        if (saveFile.exists()) {
            try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(saveFile))) {
                this.gameHandler = (GameHandler) in.readObject();
                restoreCardVisuals();
                System.out.println("Partida cargada correctamente.");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
                System.out.println("Fallo al cargar partida, se crea una nueva.");
                createNewGame();
            }
        } else {
            createNewGame();
        }
    }

    private void createNewGame() {
        this.gameHandler = GameHandler.createNewGame();
    }

    private void restoreCardVisuals() {
        gameHandler.getHumanPlayer().getCardsPlayer().forEach(Card::restoreVisuals);
        gameHandler.getMachinePlayer().getCardsPlayer().forEach(Card::restoreVisuals);
        gameHandler.getTable().getCards().forEach(Card::restoreVisuals);
        gameHandler.getDeck().getAllCards().forEach(Card::restoreVisuals);
    }

    public void printHumanPlayerCards() {
        gridPaneCardsPlayer.getChildren().clear();
        Card[] visibleCards = gameHandler.getCurrentVisibleCardsHumanPlayer(posInitCardToShow);

        for (int i = 0; i < visibleCards.length; i++) {
            Card card = visibleCards[i];
            ImageView cardImageView = card.getCard();
            attachClickHandlerToCard(card, cardImageView); // Mover comportamiento a una función aparte
            gridPaneCardsPlayer.add(cardImageView, i, 0);
        }

        updateCurrentColorUI();
    }

    public void printMachinePlayerCards() {
        gridPaneCardsMachine.getChildren().clear();
        int numCards = gameHandler.getMachinePlayer().getCardsPlayer().size();
        for (int i = 0; i < numCards; i++) {
            ImageView cardBack = new ImageView(new Image(getClass().getResource("/org/example/eiscuno/cards-uno/card_uno.png").toExternalForm()));
            cardBack.setFitHeight(90);
            cardBack.setFitWidth(70);
            gridPaneCardsMachine.add(cardBack, i, 0);
        }
    }

    private void attachClickHandlerToCard(Card card, ImageView cardImageView) {
        cardImageView.setOnMouseClicked(event -> {
            boolean wasPlayed = gameHandler.handleHumanCardClick(card, () -> {
                tableImageView.setImage(card.getImage());
                printHumanPlayerCards(); // actualizar visualmente
                printMachinePlayerCards();
                saveGame();
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

    private void setChooseColorHumanBehaviour(){
        gameHandler.setColorChooser(() -> {
            final String[] selectedColor = new String[1];
            CountDownLatch latch = new CountDownLatch(1);

            Platform.runLater(() -> {
                GamePauseManager.getInstance().pauseGame(); // si lo usas
                ChoiceDialog<String> dialog = new ChoiceDialog<>("ROJO", Arrays.asList("ROJO", "VERDE", "AZUL", "AMARILLO"));
                dialog.setTitle("Cambio de color");
                dialog.setHeaderText(null);
                dialog.setContentText("Elige el color para continuar:");

                dialog.setOnHidden(event -> {
                    GamePauseManager.getInstance().resumeGame();
                });

                dialog.showAndWait().ifPresent(color -> selectedColor[0] = COLOR_MAP.getOrDefault(color.toUpperCase(), color.toUpperCase()));
                latch.countDown();
            });

            try {
                latch.await(); // bloquea hasta que el usuario seleccione
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            return selectedColor[0] != null ? selectedColor[0] : "RED"; // fallback
        });
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
            showAlert("UNO declarado", "¡Has declarado UNO correctamente!");
        } else if (gameHandler.getMachinePlayer().getCardsPlayer().size() == 1 && !gameHandler.getIASaidUno()) {
            gameHandler.eatCard(gameHandler.getMachinePlayer(), 1);
            showAlert("UNO callout a la máquina", "¡La máquina no dijo UNO! Le has hecho comer una carta.");
        }
    }

    private void updateCurrentColorUI() {
        String color = gameHandler.getCurrentCardOnTable().getColor();
        labelCurrentColor.setText("Color actual: " + (color != null ? color : "-"));
    }

    private void startUnoTimerForHuman() {
        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ignored) {}
            if (!gameHandler.getHumanSaidUno() && gameHandler.getHumanPlayer().getCardsPlayer().size() == 1) {
                Platform.runLater(() -> {
                    showAlert("¡No dijiste UNO!", "La máquina notó que no dijiste UNO. Comes una carta.");
                    gameHandler.eatCard(gameHandler.getHumanPlayer(), 1);
                    unoButton.setVisible(false);
                    printHumanPlayerCards();
                });
            }
        }).start();
    }

    private void checkUnoConditions() {
        int humanCards = gameHandler.getHumanPlayer().getCardsPlayer().size();
        int machineCards = gameHandler.getMachinePlayer().getCardsPlayer().size();

        boolean shouldShowButton = (humanCards == 1 && !gameHandler.getHumanSaidUno()) ||
                (machineCards == 1 && !gameHandler.getIASaidUno());

        Platform.runLater(() -> unoButton.setVisible(shouldShowButton));

        if (humanCards > 1 && gameHandler.getHumanSaidUno()) gameHandler.setHumanSaidUno(false);
        if (machineCards > 1 && gameHandler.getIASaidUno()) gameHandler.setIASaidUno(false);
    }

    private void showTurnError() {
        showAlert("Turno incorrecto", "No es tu turno. Espera a que la máquina juegue.");
    }

    private void showInvalidMoveError() {
        showAlert("Jugada inválida", "No puedes jugar esa carta. Debe coincidir en color, número o símbolo con la carta de la mesa.");
    }

    private void showInvalidTryToTakeCardError(){
        showAlert("Intento inválido", "No puedes tomar una carta si tienes cartas jugables. Juega una carta primero.");
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    public void saveGame() {
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(PlayerStatsManager.getAppDataFolder() + "/savegame.dat"))) {
            out.writeObject(gameHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Handles the "Back" button action to show the previous set of cards.
     *
     * @param event the action event
     */
    @FXML
    void onHandleBack(ActionEvent event) {
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
    void onHandleNext(ActionEvent event) {
        if (this.posInitCardToShow < gameHandler.getHumanPlayer().getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printHumanPlayerCards();
        }
    }
}