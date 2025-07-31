package org.example.eiscuno.view;

// Imports
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

/**
 * ClickToContinueStage is a JavaFX Stage that displays a "Click to Continue" screen.
 * It is used to pause the game and wait for user interaction before proceeding.
 */
public class ClickToContinueStage extends Stage {
    /**
     * Constructor for ClickToContinueStage.
     * It loads the FXML file and sets up the stage with the scene.
     *
     * @throws IOException if there is an error loading the FXML file.
     */
    public ClickToContinueStage() throws IOException {
        // Loading the FXML file for the ClickToContinueStage
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eiscuno/click-to-continue.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            // Re-throwing the caught IOException
            throw new IOException("Error while loading FXML file", e); // This will allow the caller to handle the exception
        }
        Scene scene = new Scene(root);
        // Configuring the stage
        setTitle("Uno"); // Sets the title of the stage
        setScene(scene); // Sets the scene for the stage
        setResizable(false); // Disallows resizing of the stage
        
        // Agregar favicon
        Image icon = new Image(Objects.requireNonNull(getClass().getResource("/org/example/eiscuno/favicon.png")).toExternalForm());
        getIcons().add(icon);
        
        show(); // Displays the stage
    }

    /**
     * Returns an instance of ClickToContinueStage.
     * This method implements the Singleton pattern to ensure only one instance exists.
     *
     * @return The singleton instance of ClickToContinueStage.
     * @throws IOException if there is an error loading the FXML file.
     */
    public static ClickToContinueStage getInstance() throws IOException {
        return ClickToContinueStage.ClickToContinueHodler.INSTANCE != null ?
                ClickToContinueStage.ClickToContinueHodler.INSTANCE :
                (ClickToContinueStage.ClickToContinueHodler.INSTANCE = new ClickToContinueStage());
    }

    /**
     * Closes the ClickToContinueStage.
     * This method hides the stage and sets the instance to null.
     */
    private static class ClickToContinueHodler {
        private static ClickToContinueStage INSTANCE;
    }
}
