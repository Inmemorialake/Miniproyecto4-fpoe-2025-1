package org.example.eiscuno.controller;

// Imports
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.example.eiscuno.model.common.PlayerStatsManager;
import org.example.eiscuno.model.unoenum.EISCUnoEnum;
import org.example.eiscuno.view.GameUnoStage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Optional;

/**
 * Controller for the home scene of the Uno game.
 * Displays player statistics and handles game start logic.
 */
public class HomeUnoController {

    // FXML elements
    @FXML
    private ImageView unoLogo;

    @FXML
    private Label cardsLabel, gamesPlayedLabel, gamesWonLabel;

    /**
     * Initializes the controller by loading the Uno logo and player statistics from a CSV file.
     */
    @FXML
    public void initialize() {
        unoLogo.setImage(new Image(getClass().getResource(EISCUnoEnum.UNO.getFilePath()).toExternalForm()));

        // Read player statistics from CSV file
        File file = new File(PlayerStatsManager.getAppDataFolder(), "player_stats.csv");

        // If the file exist, read the statistics
        // and set the labels accordingly
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.readLine(); // Skip the header line
                String data = reader.readLine();
                if (data != null) {
                    String[] parts = data.split(",");
                    cardsLabel.setText(parts[2]);
                    cardsLabel.setStyle("-fx-font-weight: bold;");
                    gamesPlayedLabel.setText(parts[0]);
                    gamesPlayedLabel.setStyle("-fx-font-weight: bold;");
                    gamesWonLabel.setText( parts[1]);
                    gamesWonLabel.setStyle("-fx-font-weight: bold;");
                }
            } catch (IOException e) {
                e.printStackTrace(); // Handle the exception if the file cannot be read
            }
        }
    }

    /**
     * Handles the action when the "Play Game" button is clicked.
     * Checks for existing saved games and prompts the user accordingly.
     */
    @FXML
    private void onPlayGame() {
        // Check if there is a saved game file
        File saveFile = new File(PlayerStatsManager.getAppDataFolder(), "savegame.dat");

        // If the file does not exist, launch the game scene directly
        if (!saveFile.exists()) {
            launchGameScene();
            return;
        }

        // If the file exists, prompt the user with options
        // to continue the saved game, start a new game, or cancel
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Partida guardada encontrada");
        alert.setHeaderText("¿Qué deseas hacer?");
        alert.setContentText("Se encontró una partida guardada. ¿Quieres continuarla o comenzar una nueva?");

        // Custom button types for the alert
        ButtonType continuarBtn = new ButtonType("Continuar");
        ButtonType nuevaBtn = new ButtonType("Nueva partida");
        ButtonType cancelarBtn = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(continuarBtn, nuevaBtn, cancelarBtn);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == continuarBtn) {
                launchGameScene(); // Launch the game scene to continue the saved game
            } else if (result.get() == nuevaBtn) {
                // If the user chooses to start a new game,
                // delete the existing save file and launch the game scene
                if (saveFile.delete()) {
                    launchGameScene();
                } else {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Error");
                    error.setHeaderText(null);
                    error.setContentText("No se pudo eliminar la partida anterior.");
                    error.showAndWait();
                }
            }
            // If the user cancels, do nothing
        }
    }

    /**
     * Launches the game scene by loading the FXML file and setting it to the current stage.
     */
    private void launchGameScene() {
        // Load the game scene from the FXML file
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eiscuno/game-uno-view.fxml"));
            Parent root = loader.load();
            Scene newScene = new Scene(root);

            Stage currentStage = (Stage) unoLogo.getScene().getWindow();
            currentStage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception if the FXML file cannot be loaded
        }
    }
}