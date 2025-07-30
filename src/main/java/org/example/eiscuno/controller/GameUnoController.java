package org.example.eiscuno.controller;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.control.ChoiceDialog;
import javafx.scene.control.Label;
import javafx.util.Duration;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.common.GameHandler;
import org.example.eiscuno.model.common.PlayerStatsManager;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadSingUNOMachine;

import java.io.*;
import java.util.Arrays;

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

    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;

    private final Object turnLock = new Object();
    private boolean repeatTurn = false;
    private int posInitCardToShow = 0;

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
        System.out.println("funka");
        printHumanPlayerCards();
        System.out.println("funka");
        if (!gameHandler.getTable().getCards().isEmpty()) {
            tableImageView.setImage(gameHandler.getCurrentCardOnTable().getImage());
            System.out.println("fakun");
        }
        startThreads();
        System.out.println("funka");
    }

    public void startThreads() {
        threadSingUNOMachine = new ThreadSingUNOMachine(gameHandler.getHumanPlayer().getCardsPlayer());
        threadPlayMachine = new ThreadPlayMachine(gameHandler.getTable(), gameHandler.getMachinePlayer(), tableImageView, gameHandler, gameHandler.getDeck());
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
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
        printMachinePlayerCards();
        updateCurrentColorUI();
        Card[] currentVisibleCardsHumanPlayer = gameHandler.getGame().getCurrentVisibleCardsHumanPlayer(0);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            final Card card = currentVisibleCardsHumanPlayer[i];
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseClicked((MouseEvent event) -> {
                if (!gameHandler.getHumanTurn()) {
                    showTurnError();
                    return;
                }
                if (card.canBePlayedOn(gameHandler.getCurrentCardOnTable())) {
                    gameHandler.playCard(gameHandler.getHumanPlayer(), card);
                    PlayerStatsManager.updateStats(false, 1, true);
                    tableImageView.setImage(card.getImage());
                    printHumanPlayerCards();

                    if (card.isWildCard() || card.isPlusFour()) {
                        chooseColorAfterWild(true, card.isPlusFour());
                    } else if (!repeatTurn) {
                        gameHandler.passTurnToMachine();
                    }

                    if (gameHandler.getHumanPlayer().getCardsPlayer().size() == 1) {
                        gameHandler.setHumanSaidUno(false);
                        startUnoTimerForHuman();
                    }

                    gameHandler.checkWinner();
                    saveGame();
                } else {
                    showInvalidMoveError();
                }
            });

            gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
    }

    private void printMachinePlayerCards() {
        gridPaneCardsMachine.getChildren().clear();
        int numCards = gameHandler.getMachinePlayer().getCardsPlayer().size();
        for (int i = 0; i < numCards; i++) {
            ImageView cardBack = new ImageView(new javafx.scene.image.Image(getClass().getResource("/org/example/eiscuno/cards-uno/card_uno.png").toExternalForm()));
            cardBack.setFitHeight(90);
            cardBack.setFitWidth(70);
            gridPaneCardsMachine.add(cardBack, i, 0);
        }
    }

    @FXML
    void onHandleTakeCard(ActionEvent event) {
        if (!gameHandler.getHumanTurn()) {
            showTurnError();
            return;
        }
        if (gameHandler.hasPlayableCard(gameHandler.getHumanPlayer())) {
            showInvalidMoveError();
            return;
        }
        gameHandler.getGame().eatCard(gameHandler.getHumanPlayer(), 1);
        printHumanPlayerCards();
        gameHandler.passTurnToMachine();
    }

    @FXML
    void onHandleUno(ActionEvent event) {
        if (gameHandler.getHumanPlayer().getCardsPlayer().size() == 1) {
            gameHandler.setHumanSaidUno(true);
            showAlert("UNO declarado", "¡Has declarado UNO correctamente!");
        }
        if (gameHandler.getMachinePlayer().getCardsPlayer().size() == 1 && !gameHandler.getIASaidUno()) {
            showAlert("UNO cantado a la IA!", "Has cantado correctamente UNO! a la máquina, ahora comerá una carta.");
            gameHandler.getGame().eatCard(gameHandler.getMachinePlayer(), 1);
            gameHandler.setIASaidUno(false);
            printMachinePlayerCards();
        }
        unoButton.setVisible(false);
    }

    private void chooseColorAfterWild(boolean playedByHuman, boolean isPlusFour) {
        if (playedByHuman) {
            Platform.runLater(() -> {
                ChoiceDialog<String> dialog = new ChoiceDialog<>("ROJO", Arrays.asList("ROJO", "VERDE", "AZUL", "AMARILLO"));
                dialog.setTitle("Cambio de color");
                dialog.setHeaderText(null);
                dialog.setContentText("Elige el color para continuar:");
                dialog.showAndWait().ifPresent(color -> {
                    gameHandler.getCurrentCardOnTable().setColor(COLOR_MAP.getOrDefault(color.toUpperCase(), color.toUpperCase()));
                    tableImageView.setImage(gameHandler.getCurrentCardOnTable().getImage());
                    updateCurrentColorUI();
                    if (isPlusFour) {
                        gameHandler.getGame().eatCard(gameHandler.getMachinePlayer(), 4);
                        printMachinePlayerCards();
                        showAlert("+4 jugado", "La máquina toma 4 cartas.");
                    }
                    if (!repeatTurn) {
                        gameHandler.passTurnToMachine();
                    }
                });
            });
        } else {
            String[] colors = {"RED", "GREEN", "BLUE", "YELLOW"};
            String color = colors[new java.util.Random().nextInt(colors.length)];
            gameHandler.getCurrentCardOnTable().setColor(color);
            Platform.runLater(() -> {
                tableImageView.setImage(gameHandler.getCurrentCardOnTable().getImage());
                updateCurrentColorUI();
            });
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
                    gameHandler.getGame().eatCard(gameHandler.getHumanPlayer(), 1);
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