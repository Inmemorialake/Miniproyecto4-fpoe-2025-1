package org.example.eiscuno.model.common;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;

import java.util.List;
import java.util.function.Consumer;

public class DialogManager {

    private DialogManager() {}

    public static void showInfoDialog(String title, String content) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(content);
            alert.setOnHidden(e -> GamePauseManager.getInstance().resumeGame());
            alert.showAndWait();
        });
    }

    public static void showChoiceDialog(String title, String content, List<String> choices, String defaultChoice, Consumer<String> onChoice) {
        Platform.runLater(() -> {
            ChoiceDialog<String> dialog = new ChoiceDialog<>(defaultChoice, choices);
            dialog.setTitle(title);
            dialog.setHeaderText(null);
            dialog.setContentText(content);

            dialog.setOnHidden(e -> GamePauseManager.getInstance().resumeGame());

            dialog.showAndWait().ifPresent(onChoice);
        });
    }
}
