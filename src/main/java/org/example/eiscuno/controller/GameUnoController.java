package org.example.eiscuno.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import org.example.eiscuno.model.card.Card;
import org.example.eiscuno.model.common.PlayerStatsManager;
import org.example.eiscuno.model.deck.Deck;
import org.example.eiscuno.model.game.GameUno;
import org.example.eiscuno.model.machine.ThreadPlayMachine;
import org.example.eiscuno.model.machine.ThreadSingUNOMachine;
import org.example.eiscuno.model.player.Player;
import org.example.eiscuno.model.table.Table;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import java.util.Arrays;
import javafx.scene.control.Label;

/**
 * Controller class for the Uno game.
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
    private Button unoButton;

    private boolean humanSaidUno = false;
    private boolean iaSaidUno = false;

    private Player humanPlayer;
    private Player machinePlayer;
    private Deck deck;
    private Table table;
    private GameUno gameUno;
    private int posInitCardToShow;
    private boolean isHumanTurn;
    private final Object turnLock = new Object();
    private boolean repeatTurn = false;

    private ThreadSingUNOMachine threadSingUNOMachine;
    private ThreadPlayMachine threadPlayMachine;

    private int cardsPlayedByHuman = 0; //To make the csv :)

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
     * Initializes the controller.
     */
    @FXML
    public void initialize() {
        initVariables();
        this.gameUno.startGame();
        printHumanPlayerCards();
        // Mostrar la carta inicial en la mesa
        tableImageView.setImage(gameUno.getCurrentCardOnTable().getImage());
        isHumanTurn = true;
        threadSingUNOMachine = new ThreadSingUNOMachine(this.humanPlayer.getCardsPlayer());
        Thread t = new Thread(threadSingUNOMachine, "ThreadSingUNO");
        t.start();
        threadPlayMachine = new ThreadPlayMachine(this.table, this.machinePlayer, this.tableImageView, this, this.deck);
        threadPlayMachine.start();
    }

    /**
     * Initializes the variables for the game.
     */
    private void initVariables() {
        this.humanPlayer = new Player("HUMAN_PLAYER");
        this.machinePlayer = new Player("MACHINE_PLAYER");
        this.deck = new Deck();
        this.table = new Table();
        this.gameUno = new GameUno(this.humanPlayer, this.machinePlayer, this.deck, this.table);
        this.posInitCardToShow = 0;
    }

    /**
     * Prints the human player's cards on the grid pane.
     */
    public void printHumanPlayerCards() {
        this.gridPaneCardsPlayer.getChildren().clear();
        printMachinePlayerCards();
        updateCurrentColorUI();
        Card[] currentVisibleCardsHumanPlayer = this.gameUno.getCurrentVisibleCardsHumanPlayer(this.posInitCardToShow);

        for (int i = 0; i < currentVisibleCardsHumanPlayer.length; i++) {
            final Card card = currentVisibleCardsHumanPlayer[i];
            final int index = i;
            ImageView cardImageView = card.getCard();

            cardImageView.setOnMouseClicked((MouseEvent event) -> {
                if (!isHumanTurn) {
                    showTurnError();
                    return;
                }
                Card topCard = gameUno.getCurrentCardOnTable();
                if (card.canBePlayedOn(topCard)) {
                    gameUno.playCard(card);
                    cardsPlayedByHuman++; //OMG
                    tableImageView.setImage(card.getImage());
                    humanPlayer.getCardsPlayer().remove(card);
                    printHumanPlayerCards();

                    // Si la carta es comodín o +4, NO pasar el turno aquí, solo dentro del callback de chooseColorAfterWild
                    applySpecialCardEffect(card, true);
                    if (!(card.isWildCard() || card.isPlusFour()) && !repeatTurn) {
                        isHumanTurn = false;
                        synchronized (turnLock) { turnLock.notifyAll(); }
                    }

                    // Si el humano se queda con una sola carta, empezamos la secuencia para decir uno
                    if (humanPlayer.getCardsPlayer().size() == 1) {
                        unoButton.setVisible(true);
                        humanSaidUno = false;
                        startUnoTimerForHuman();
                    } else {
                        unoButton.setVisible(false);
                    }

                    // Hacemos el check por si el jugador ha ganado
                    checkWinner();

                } else {
                    showInvalidMoveError();
                }
            });

            this.gridPaneCardsPlayer.add(cardImageView, i, 0);
        }
    }

    /**
     * Prints the machine player's cards on the grid pane.
     */
    private void printMachinePlayerCards() {
        this.gridPaneCardsMachine.getChildren().clear();
        int numCards = machinePlayer.getCardsPlayer().size();
        for (int i = 0; i < numCards; i++) {
            ImageView cardBack = new ImageView(new javafx.scene.image.Image(getClass().getResource("/org/example/eiscuno/cards-uno/card_uno.png").toExternalForm()));
            cardBack.setFitHeight(90);
            cardBack.setFitWidth(70);
            this.gridPaneCardsMachine.add(cardBack, i, 0);
        }
    }

    /**
     * Finds the position of a specific card in the human player's hand.
     *
     * @param card the card to find
     * @return the position of the card, or -1 if not found
     */
    private Integer findPosCardsHumanPlayer(Card card) {
        for (int i = 0; i < this.humanPlayer.getCardsPlayer().size(); i++) {
            if (this.humanPlayer.getCardsPlayer().get(i).equals(card)) {
                return i;
            }
        }
        return -1;
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
        if (this.posInitCardToShow < this.humanPlayer.getCardsPlayer().size() - 4) {
            this.posInitCardToShow++;
            printHumanPlayerCards();
        }
    }

    /**
     * Handles the action of taking a card.
     *
     * @param event the action event
     */
    @FXML
    void onHandleTakeCard(ActionEvent event) {
        if (!isHumanTurn) {
            showTurnError();
            return;
        }
        if (hasPlayableCard(humanPlayer)) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No puedes tomar carta");
            alert.setHeaderText(null);
            alert.setContentText("Aún tienes jugadas posibles. Juega una carta válida.");
            alert.showAndWait();
            return;
        }
        // No puede jugar, toma una carta y cede el turno
        gameUno.eatCard(humanPlayer, 1);
        printHumanPlayerCards();
        // Añadir un pequeño delay antes de ceder el turno
        new Thread(() -> {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            javafx.application.Platform.runLater(() -> {
                // Si repeatTurn era true (por reverse/skip), después de comer una carta, pon repeatTurn = false y cede el turno
                repeatTurn = false;
                isHumanTurn = false;
                synchronized (turnLock) { turnLock.notifyAll(); }
            });
        }).start();
    }

    /**
     * Handles the action of saying "Uno".
     *
     * @param event the action event
     */
    @FXML
    void onHandleUno(ActionEvent event) {
        if (humanPlayer.getCardsPlayer().size() == 1) {
            humanSaidUno = true;
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("UNO declarado");
            alert.setHeaderText(null);
            alert.setContentText("¡Has declarado UNO correctamente!");
            alert.showAndWait();
        }
        if (machinePlayer.getCardsPlayer().size() == 1 && threadPlayMachine.getIASaidUno() == false) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("UNO cantado a la IA!");
            alert.setHeaderText(null);
            alert.setContentText("Has cantado correctamente UNO! a la maquina, ahora comerá una carta.");
            gameUno.eatCard(machinePlayer, 1);
            threadPlayMachine.setIASaidUno(false); // reset por si vuelve a tener 1 carta
            printMachinePlayerCards();
            alert.showAndWait();
        }

        unoButton.setVisible(false);
    }

    public void makeUnoButtonVisible(){
        unoButton.setVisible(true);
    }

    public void makeUnoButtonInvisible(){
        if(humanPlayer.getCardsPlayer().size() != 1 && !humanSaidUno){
            unoButton.setVisible(false);
        }
    }

    // Mostrar mensaje de error si la jugada no es válida
    private void showInvalidMoveError() {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Jugada inválida");
        alert.setHeaderText(null);
        alert.setContentText("No puedes jugar esa carta. Debe coincidir en color, número o símbolo con la carta de la mesa.");
        alert.showAndWait();
    }

    private void jugarTurnoMaquina() {
        // Esperar un poco para simular el "pensar" de la máquina
        new Thread(() -> {
            try {
                Thread.sleep(1500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Card topCard = gameUno.getCurrentCardOnTable();
            Card cartaAJugar = null;
            for (Card card : machinePlayer.getCardsPlayer()) {
                if (card.canBePlayedOn(topCard)) {
                    cartaAJugar = card;
                    break;
                }
            }
            if (cartaAJugar != null) {
                gameUno.playCard(cartaAJugar);
                machinePlayer.getCardsPlayer().remove(cartaAJugar);
                final Card cartaFinal = cartaAJugar;
                javafx.application.Platform.runLater(() -> {
                    tableImageView.setImage(cartaFinal.getImage());
                    isHumanTurn = true;
                    printHumanPlayerCards();
                });
            } else {
                // No puede jugar, toma una carta
                gameUno.eatCard(machinePlayer, 1);
                javafx.application.Platform.runLater(() -> {
                    isHumanTurn = true;
                    printHumanPlayerCards();
                });
            }
        }).start();
    }

    private void showTurnError() {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("Turno incorrecto");
        alert.setHeaderText(null);
        alert.setContentText("No es tu turno. Espera a que la máquina juegue.");
        alert.showAndWait();
    }

    // Método para que el hilo de la máquina consulte si es su turno
    public boolean isMachineTurn() {
        return !isHumanTurn;
    }
    // Método para que el hilo de la máquina pase el turno al humano
    public void passTurnToHuman() {
        isHumanTurn = true;
        javafx.application.Platform.runLater(() -> {
            printHumanPlayerCards();
            printMachinePlayerCards();
            updateCurrentColorUI();
        });
    }
    // Método para que el hilo de la máquina actualice la carta en la mesa
    public void updateTable(Card card) {
        javafx.application.Platform.runLater(() -> {
            tableImageView.setImage(card.getImage());
            printMachinePlayerCards();
            updateCurrentColorUI();
        });
    }
    // Método para que el hilo de la máquina espere hasta que sea su turno
    public void waitForMachineTurn() {
        synchronized (turnLock) {
            while (isHumanTurn) {
                try { turnLock.wait(); } catch (InterruptedException ignored) {}
            }
        }
    }

    // Check if a player has any playable card
    private boolean hasPlayableCard(Player player) {
        Card topCard = gameUno.getCurrentCardOnTable();
        for (Card card : player.getCardsPlayer()) {
            if (card.canBePlayedOn(topCard)) {
                return true;
            }
        }
        return false;
    }

    public void applySpecialCardEffect(Card card, boolean playedByHuman) {
        // Efectos de cartas especiales UNO
        if (card.isPlusTwo()) {
            repeatTurn = false;
            if (playedByHuman) {
                gameUno.eatCard(machinePlayer, 2);
                printMachinePlayerCards();
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("+2 jugado");
                    alert.setHeaderText(null);
                    alert.setContentText("La máquina toma 2 cartas.");
                    alert.showAndWait();
                });
            } else {
                gameUno.eatCard(humanPlayer, 2);
                javafx.application.Platform.runLater(this::printHumanPlayerCards);
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("+2 recibido");
                    alert.setHeaderText(null);
                    alert.setContentText("Debes tomar 2 cartas.");
                    alert.showAndWait();
                });
            }
        } else if (card.isPlusFour()) {
            repeatTurn = false;
            if (playedByHuman) {
                // El humano juega +4, la máquina debe comer después de elegir el color
                chooseColorAfterWild(true, true);
            } else {
                gameUno.eatCard(humanPlayer, 4);
                javafx.application.Platform.runLater(this::printHumanPlayerCards);
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    alert.setTitle("+4 recibido");
                    alert.setHeaderText(null);
                    alert.setContentText("Debes tomar 4 cartas.");
                    alert.showAndWait();
                });
                chooseColorAfterWild(false, false);
            }
        } else if (card.isSpecial() && card.isSkipOrReverse()) {
            repeatTurn = true;
            boolean isReverse = card.getUrl().toLowerCase().contains("reverse");
            if (playedByHuman) {
                isHumanTurn = true;
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    if (isReverse) {
                        alert.setTitle("¡Reversa!");
                        alert.setHeaderText(null);
                        alert.setContentText("Jugaste una carta reversa. Juegas de nuevo.");
                    } else {
                        alert.setTitle("Turno anulado");
                        alert.setHeaderText(null);
                        alert.setContentText("La máquina pierde su turno. Juegas de nuevo.");
                    }
                    alert.showAndWait();
                });
            } else {
                isHumanTurn = false;
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.INFORMATION);
                    if (isReverse) {
                        alert.setTitle("¡Reversa!");
                        alert.setHeaderText(null);
                        alert.setContentText("La máquina jugó una carta reversa. Juega de nuevo.");
                    } else {
                        alert.setTitle("Turno anulado");
                        alert.setHeaderText(null);
                        alert.setContentText("Pierdes tu turno. La máquina juega de nuevo.");
                    }
                    alert.showAndWait();
                });
                if (!isHumanTurn && repeatTurn) {
                    synchronized (turnLock) { turnLock.notifyAll(); }
                }
            }
        } else if (card.isWildCard()) {
            repeatTurn = false;
            if (playedByHuman) {
                // El humano juega comodín, la máquina debe esperar la selección de color
                chooseColorAfterWild(true, false);
                // El turno se pasa a la máquina solo después de elegir el color (dentro del callback)
            } else {
                chooseColorAfterWild(false, false);
            }
        } else {
            repeatTurn = false;
        }
    }

    // Nuevo método para manejar el flujo tras +4 y comodín
    private void chooseColorAfterWild(boolean playedByHuman, boolean isPlusFour) {
        if (playedByHuman) {
            javafx.application.Platform.runLater(() -> {
                ChoiceDialog<String> dialog = new ChoiceDialog<>("ROJO", Arrays.asList("ROJO", "VERDE", "AZUL", "AMARILLO"));
                dialog.setTitle("Cambio de color");
                dialog.setHeaderText(null);
                dialog.setContentText("Elige el color para continuar:");
                dialog.showAndWait().ifPresent(color -> {
                    String englishColor = COLOR_MAP.getOrDefault(color.toUpperCase(), color.toUpperCase());
                    Card topCard = gameUno.getCurrentCardOnTable();
                    try {
                        java.lang.reflect.Field colorField = topCard.getClass().getDeclaredField("color");
                        colorField.setAccessible(true);
                        colorField.set(topCard, englishColor);
                    } catch (Exception e) { e.printStackTrace(); }
                    tableImageView.setImage(topCard.getImage());
                    updateCurrentColorUI();
                    // Si es +4, la máquina come después de elegir el color
                    if (isPlusFour) {
                        gameUno.eatCard(machinePlayer, 4);
                        printMachinePlayerCards();
                        javafx.application.Platform.runLater(() -> {
                            Alert alert = new Alert(Alert.AlertType.INFORMATION);
                            alert.setTitle("+4 jugado");
                            alert.setHeaderText(null);
                            alert.setContentText("La máquina toma 4 cartas.");
                            alert.showAndWait();
                        });
                    }
                    // Para comodín y +4: solo pasar el turno a la máquina después de elegir el color y si repeatTurn es false
                    if (!repeatTurn) {
                        isHumanTurn = false;
                        synchronized (turnLock) { turnLock.notifyAll(); }
                    }
                });
            });
        } else {
            String[] colors = {"RED", "GREEN", "BLUE", "YELLOW"};
            String color = colors[new java.util.Random().nextInt(colors.length)];
            Card topCard = gameUno.getCurrentCardOnTable();
            try {
                java.lang.reflect.Field colorField = topCard.getClass().getDeclaredField("color");
                colorField.setAccessible(true);
                colorField.set(topCard, color);
            } catch (Exception e) { e.printStackTrace(); }
            javafx.application.Platform.runLater(() -> {
                tableImageView.setImage(topCard.getImage());
                updateCurrentColorUI();
            });
        }
    }

    private void updateCurrentColorUI() {
        Card topCard = gameUno.getCurrentCardOnTable();
        String color = topCard.getColor();
        if (color == null) color = "-";
        labelCurrentColor.setText("Color actual: " + color);
    }

    public boolean isRepeatTurn() {
        return repeatTurn;
    }

    private void startUnoTimerForHuman() {
        new Thread(() -> {
            try {
                Thread.sleep(3000); // 3 segundos para decir UNO
            } catch (InterruptedException ignored) {}
            if (!humanSaidUno && humanPlayer.getCardsPlayer().size() == 1) {
                javafx.application.Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.WARNING);
                    alert.setTitle("¡No dijiste UNO!");
                    alert.setHeaderText(null);
                    alert.setContentText("La máquina notó que no dijiste UNO. Comes una carta.");
                    alert.showAndWait();
                    gameUno.eatCard(humanPlayer, 1);
                    unoButton.setVisible(false);
                    printHumanPlayerCards();
                });
            }
        }).start();
    }

    public void checkWinner() {
        if (humanPlayer.getCardsPlayer().isEmpty()) {
            showWinner(true);
        } else if (machinePlayer.getCardsPlayer().isEmpty()) {
            showWinner(false);
        }

    }

    private void showWinner(boolean playerWon) {
        javafx.application.Platform.runLater(() -> {
            String mensaje = playerWon
                    ? "¡Has ganado la partida! ¡Bien hecho!"
                    : "😓 La máquina ha ganado. ¡Suerte la próxima!";
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Fin del juego");
            alert.setHeaderText(null);
            alert.setContentText(mensaje);
            alert.showAndWait();

            threadPlayMachine.stopThread();
            PlayerStatsManager.updateStats(playerWon, cardsPlayedByHuman);
            System.exit(0);
        });
    }
}
