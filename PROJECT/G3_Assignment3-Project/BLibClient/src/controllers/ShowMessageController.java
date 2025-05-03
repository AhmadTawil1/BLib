package controllers;

import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;

/**
 * A utility class for displaying different types of alert messages to the user.
 */
public class ShowMessageController {

    /**
     * Displays an alert dialog with the given information.
     *
     * @param title     The title of the alert dialog.
     * @param content   The content message of the alert dialog.
     * @param alertType The type of the alert (e.g., ERROR, INFORMATION).
     */
    public static void showAlert(String title, String content, Alert.AlertType alertType) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(null); // Optional: remove the header
        alert.setContentText(content);
        alert.showAndWait();
    }

    /**
     * Displays an informational message.
     *
     * @param title   The title of the message.
     * @param content The content of the message.
     */
    public static void showInfo(String title, String content) {
        showAlert(title, content, Alert.AlertType.INFORMATION);
    }

    /**
     * Displays an error message.
     *
     * @param title   The title of the message.
     * @param content The content of the message.
     */
    public static void showError(String title, String content) {
        showAlert(title, content, Alert.AlertType.ERROR);
    }

    /**
     * Displays a warning message.
     *
     * @param title   The title of the warning.
     * @param content The content of the warning.
     */
    public static void showWarning(String title, String content) {
        showAlert(title, content, Alert.AlertType.WARNING);
    }

    /**
     * Displays a confirmation dialog and waits for user response.
     *
     * @param title   The title of the confirmation dialog.
     * @param content The content of the confirmation dialog.
     * @return True if the user confirms, false otherwise.
     */
    public static boolean showConfirmation(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);

        return alert.showAndWait().filter(response -> response == ButtonType.OK).isPresent();
    }
}
