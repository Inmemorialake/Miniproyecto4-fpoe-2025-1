package org.example.eiscuno.view;

// Imports
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

/**
 * StartStage is a JavaFX Stage that represents the initial screen of the Uno game.
 * It is used to display the starting interface and wait for user interaction to begin the game.
 */
public class StartStage extends Stage {
    /**
     * Constructor for StartStage.
     * It loads the FXML file and sets up the stage with the scene.
     *
     * @throws IOException if there is an error loading the FXML file.
     */
    public StartStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eiscuno/start-stage.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            // Re-throwing the caught IOException
            throw new IOException("Error while loading FXML file", e);
        }
        Scene scene = new Scene(root);
        // Configuring the stage
        setTitle("1"); // Sets the title of the stage
        setScene(scene); // Sets the scene for the stage
        setResizable(false); // Disallows resizing of the stage
        show(); // Displays the stage
    }

    /**
     * Returns an instance of StartStage.
     * This method implements the Singleton pattern to ensure only one instance exists.
     *
     * @return The singleton instance of StartStage.
     * @throws IOException if there is an error loading the FXML file.
     */
    public static StartStage getInstance() throws IOException {
        return StartStage.StartHolder.INSTANCE != null ?
                StartStage.StartHolder.INSTANCE :
                (StartStage.StartHolder.INSTANCE = new StartStage());
    }

    /**
     * Closes the StartStage.
     * This method hides the stage and sets the instance to null.
     */
    private static class StartHolder {
        private static StartStage INSTANCE;
    }
}
