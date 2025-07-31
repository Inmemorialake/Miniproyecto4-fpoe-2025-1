package org.example.eiscuno.view;

// Imports
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.ChoiceDialog;
import org.example.eiscuno.model.common.GamePauseManager;

import java.util.List;
import java.util.function.Consumer;

/**
 * DialogManager is a utility class for managing dialog boxes in the game.
 * It provides methods to show information dialogs and choice dialogs.
 */
public class DialogManager {

    // Private constructor to prevent instantiation
    private DialogManager() {}

    /**
     * Shows an information dialog with the specified title and content.
     * The game is paused while the dialog is displayed.
     *
     * @param title   The title of the dialog.
     * @param content The content of the dialog.
     */
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

    /**
     * Shows a choice dialog with the specified title, content, choices, and default choice.
     * The game is paused while the dialog is displayed.
     *
     * @param title         The title of the dialog.
     * @param content       The content of the dialog.
     * @param choices       The list of choices to display in the dialog.
     * @param defaultChoice The default choice to pre-select in the dialog.
     * @param onChoice      A callback function that is called with the selected choice when the user makes a selection.
     */
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
