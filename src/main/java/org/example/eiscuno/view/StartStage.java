package org.example.eiscuno.view;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class StartStage extends Stage {
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

    public static StartStage getInstance() throws IOException {
        return StartStage.StartHolder.INSTANCE != null ?
                StartStage.StartHolder.INSTANCE :
                (StartStage.StartHolder.INSTANCE = new StartStage());
    }

    private static class StartHolder {
        private static StartStage INSTANCE;
    }
}
