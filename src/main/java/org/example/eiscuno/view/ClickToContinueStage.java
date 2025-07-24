package org.example.eiscuno.view;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class ClickToContinueStage extends Stage {
    public ClickToContinueStage() throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/example/eiscuno/click-to-continue.fxml"));
        Parent root;
        try {
            root = loader.load();
        } catch (IOException e) {
            // Re-throwing the caught IOException
            throw new IOException("Error while loading FXML file", e);
        }
        Scene scene = new Scene(root);
        // Configuring the stage
        setTitle("Uno"); // Sets the title of the stage
        setScene(scene); // Sets the scene for the stage
        setResizable(false); // Disallows resizing of the stage
        show(); // Displays the stage
    }

    public static ClickToContinueStage getInstance() throws IOException {
        return ClickToContinueStage.ClickToContinueHodler.INSTANCE != null ?
                ClickToContinueStage.ClickToContinueHodler.INSTANCE :
                (ClickToContinueStage.ClickToContinueHodler.INSTANCE = new ClickToContinueStage());
    }

    private static class ClickToContinueHodler {
        private static ClickToContinueStage INSTANCE;
    }
}
