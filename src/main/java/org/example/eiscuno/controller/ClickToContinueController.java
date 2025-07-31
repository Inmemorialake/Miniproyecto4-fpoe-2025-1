package org.example.eiscuno.controller;

// imports
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * Controller for handling click events to continue to the next scene.
 */
public class ClickToContinueController {

    /**
     * Handles mouse click events to transition to the start Uno scene.
     *
     * @param event the mouse event that triggered the action
     */
    @FXML
    private void handleClick(MouseEvent event) {
        // Load the start Uno scene
        // and set it to the current stage
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eiscuno/start-uno.fxml"));
            Parent root = loader.load();

            Stage stage = (Stage) ((javafx.scene.Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
        } catch (IOException e) { // Handle the exception if the FXML file cannot be loaded
            e.printStackTrace();
        }
    }
}
