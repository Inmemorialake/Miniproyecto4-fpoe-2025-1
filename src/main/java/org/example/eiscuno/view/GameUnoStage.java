package org.example.eiscuno.view;

// Imports
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.example.eiscuno.controller.GameUnoController;

import java.io.IOException;
import java.util.Objects;

/**
 * Represents the main stage of the Uno game application.
 * This stage displays the game interface to the user.
 */
public class GameUnoStage extends Stage {

    private GameUnoController controller;

    public void registerGameController(GameUnoController controller) {
        this.controller = controller;
    }

    /**
     * Constructs a new instance of GameUnoStage.
     *
     * @throws IOException if an error occurs while loading the FXML file for the game interface.
     */
    public GameUnoStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eiscuno/home-uno-view.fxml"));
        Parent root;
        try {
            root = loader.load();
            System.out.println("La mamasita se carga en el root");
        } catch (IOException e) {
            // Re-throwing the caught IOException
            throw new IOException("Error while loading FXML file", e);
        }
        Scene scene = new Scene(root);
        System.out.println("La mamasita se crea dentro de una nueva escena");
        // Configuring the stage
        setTitle("EISC Uno"); // Sets the title of the stage
        setScene(scene); // Sets the scene for the stage
        setResizable(false); // Disallows resizing of the stage
        
        // Agregar favicon
        Image icon = new Image(Objects.requireNonNull(getClass().getResource("/org/example/eiscuno/favicon.png")).toExternalForm());
        getIcons().add(icon);

        this.setOnCloseRequest(event -> {
            if (controller != null) {
                controller.shutdownApplication();
            }
        });

        show(); // Displays the stage
        System.out.println("La mamasita se muestra");
    }

    /**
     * Closes the instance of GameUnoStage.
     * This method is used to clean up resources when the game stage is no longer needed.
     */
    public static void deleteInstance() {
        GameUnoStageHolder.INSTANCE.close();
        GameUnoStageHolder.INSTANCE = null;
    }

    /**
     * Retrieves the singleton instance of GameUnoStage.
     *
     * @return the singleton instance of GameUnoStage.
     * @throws IOException if an error occurs while creating the instance.
     */
    public static GameUnoStage getInstance() throws IOException {
        return GameUnoStageHolder.INSTANCE != null ?
                GameUnoStageHolder.INSTANCE :
                (GameUnoStageHolder.INSTANCE = new GameUnoStage());
    }

    /**
     * Holder class for the singleton instance of GameUnoStage.
     * This class ensures lazy initialization of the singleton instance.
     */
    private static class GameUnoStageHolder {
        private static GameUnoStage INSTANCE;
    }
}
