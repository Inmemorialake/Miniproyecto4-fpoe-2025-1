package org.example.eiscuno.controller;

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

public class HomeUnoController {

    @FXML
    private ImageView unoLogo;

    @FXML
    private Label cardsLabel, gamesPlayedLabel, gamesWonLabel;

    @FXML
    public void initialize() {
        unoLogo.setImage(new Image(getClass().getResource(EISCUnoEnum.UNO.getFilePath()).toExternalForm()));

        // Leer desde el CSV
        File file = new File(PlayerStatsManager.getAppDataFolder(), "player_stats.csv");

        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                reader.readLine(); // encabezado
                String data = reader.readLine();
                if (data != null) {
                    String[] parts = data.split(",");
                    cardsLabel.setText("Cartas puestas: " + parts[2]);
                    gamesPlayedLabel.setText("Partidas jugadas: " + parts[0]);
                    gamesWonLabel.setText("Partidas ganadas: " + parts[1]);
                    // Podrías añadir también las cartas colocadas si quieres mostrar eso
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @FXML
    private void onPlayGame() {
        File saveFile = new File(PlayerStatsManager.getAppDataFolder(), "savegame.dat");

        // Si no existe partida guardada, lanza directamente la escena del juego
        if (!saveFile.exists()) {
            launchGameScene();
            return;
        }

        // Si existe, preguntamos al jugador
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Partida guardada encontrada");
        alert.setHeaderText("¿Qué deseas hacer?");
        alert.setContentText("Se encontró una partida guardada. ¿Quieres continuarla o comenzar una nueva?");

        ButtonType continuarBtn = new ButtonType("Continuar");
        ButtonType nuevaBtn = new ButtonType("Nueva partida");
        ButtonType cancelarBtn = new ButtonType("Cancelar", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(continuarBtn, nuevaBtn, cancelarBtn);

        Optional<ButtonType> result = alert.showAndWait();

        if (result.isPresent()) {
            if (result.get() == continuarBtn) {
                launchGameScene(); // simplemente lanza la escena, el controlador cargará el estado
            } else if (result.get() == nuevaBtn) {
                // Eliminar archivo guardado y lanzar escena
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
            // Si presiona cancelar, no se hace nada
        }
    }

    private void launchGameScene() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eiscuno/game-uno-view.fxml"));
            Parent root = loader.load();
            Scene newScene = new Scene(root);

            Stage currentStage = (Stage) unoLogo.getScene().getWindow();
            currentStage.setScene(newScene);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}