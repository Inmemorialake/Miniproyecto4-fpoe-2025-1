package org.example.eiscuno.controller;

// Imports
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for the start scene of the Uno game.
 * Handles the button click to transition to the game scene.
 */
public class StartController {

    /**
     * Initializes the controller.
     * This method can be used to set up any initial state or data.
     */
    @FXML
    private void handleButtonClicked(ActionEvent event) {
        // Load the game Uno scene and set it to the current stage
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eiscuno/game-uno-view.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace(); // Handle the exception if the FXML file cannot be loaded
        }
    }


}
