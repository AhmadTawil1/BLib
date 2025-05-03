package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.animation.FadeTransition;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.Pane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import java.io.IOException;
import java.util.Optional;


/**
 * MainController serves as the entry point for the Library Management System application.
 * <p>
 * This class is responsible for:
 * <ul>
 *     <li>Initializing the client connection to the server.</li>
 *     <li>Setting up the primary stage and scene for the JavaFX application.</li>
 *     <li>Providing utility methods to load different views (FXML files) dynamically.</li>
 *     <li>Ensuring a responsive design by adapting the scene dimensions to the user's screen size.</li>
 * </ul>
 * </p>
 *
 * <p>Usage:
 * <ul>
 *     <li>Run the application via the {@link #main(String[])} method.</li>
 *     <li>The initial view loaded is the login screen.</li>
 * </ul>
 * </p>
 *
 * 
 * @version 1.0
 */
public class MainController extends Application {

	/** Singleton instance for managing client-server communication. */
    public static ClientControllerNew chat; 
    
    /** Server IP address. */
    private String host;
    
    /** Server port number. */
    private int port;
    
    /** The primary stage for the JavaFX application. */
    private static Stage primaryStage;
    
    /** The main scene used for loading different views dynamically. */
    private static Scene mainScene;

    
    /**
     * Entry point for the JavaFX application.
     * <p>
     * Initializes the primary stage, prompts the user for server details, sets up the scene,
     * and loads the initial login view.
     * </p>
     *
     * @param primaryStage The primary stage for this application.
     */
    @Override
    public void start(Stage primaryStage) {
        try {
            MainController.primaryStage = primaryStage;

            // Prompt the user for server connection details
            if (!getServerDetails()) {
                System.err.println("Server details not provided. Exiting application.");
                return;
            }

            // Initialize the client controller singleton
            chat = ClientControllerNew.getInstance(host, port);

            // Check connection with the server
            chat.accept(new Object[]{ClientAction.CHECK_CONNECTION});

            // Get screen dimensions
            double screenWidth = Screen.getPrimary().getBounds().getWidth();
            double screenHeight = Screen.getPrimary().getBounds().getHeight();

            // Create a scene with dimensions proportional to the screen size
            Pane root = new Pane(); // Temporary placeholder for the root node
            mainScene = new Scene(root, screenWidth * 0.5, screenHeight * 0.8); // Use 80% of the screen dimensions
            primaryStage.setScene(mainScene);

            // Set resizable options with minimum and maximum bounds
            primaryStage.setResizable(true);
            primaryStage.setMinWidth(screenWidth * 0.6); // Minimum width 60% of the screen
            primaryStage.setMinHeight(screenHeight * 0.6); // Minimum height 60% of the screen
            primaryStage.setMaxWidth(screenWidth); // Maximum width matches the screen
            primaryStage.setMaxHeight(screenHeight); // Maximum height matches the screen

            // Set the application title
            primaryStage.setTitle("Library Management System");
            primaryStage.show();

            // Load the initial view (login screen)
            loadView("/views/login.fxml", "Library Management System - Login");

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Failed to initialize the application: " + e.getMessage());
        }
    }

    /**
     * Prompts the user to enter server details (IP address and port).
     * <p>
     * Displays two dialogs:
     * <ul>
     *     <li>First dialog prompts for the server IP address.</li>
     *     <li>Second dialog prompts for the server port number.</li>
     * </ul>
     * If the user cancels or provides invalid input, the method returns false.
     * </p>
     *
     * @return True if the details are successfully entered; false otherwise.
     */
    private boolean getServerDetails() {
         // Dialog to prompt for server IP
        javafx.scene.control.TextInputDialog ipDialog = new javafx.scene.control.TextInputDialog("localhost");
        ipDialog.setTitle("Server Connection");
        ipDialog.setHeaderText("Enter the server IP address");
        ipDialog.setContentText("IP:");
        Optional<String> ipResult = ipDialog.showAndWait();
        if (ipResult.isPresent() && !ipResult.get().isEmpty()) {
            host = ipResult.get();
        } else {
            return false; // User canceled or entered invalid input
        }

        // Dialog to prompt for server port
        javafx.scene.control.TextInputDialog portDialog = new javafx.scene.control.TextInputDialog("5555");
        portDialog.setTitle("Server Connection");
        portDialog.setHeaderText("Enter the server port number");
        portDialog.setContentText("Port:");
        Optional<String> portResult = portDialog.showAndWait();
        if (portResult.isPresent() && !portResult.get().isEmpty()) {
            try {
                port = Integer.parseInt(portResult.get());
            } catch (NumberFormatException e) {
                System.err.println("Invalid port number entered.");
                return false; // Invalid port entered
            }
        } else {
            return false; // User canceled or entered invalid input
        }

        return true; // Server details successfully entered
    }

    /**
     * Loads a new FXML view into the main scene with a fade-in transition.
     * <p>
     * The method dynamically replaces the root node of the main scene and applies
     * a fade-in effect for a smooth transition.
     * </p>
     *
     * @param fxmlPath The path to the FXML file for the view.
     * @param title    The title to set for the primary stage.
     */
    public static void loadView(String fxmlPath, String title) {
        try {
            
            FXMLLoader loader = new FXMLLoader(MainController.class.getResource(fxmlPath));
            Pane root = loader.load();

            // Apply a smooth fade-in transition
            FadeTransition fadeIn = new FadeTransition(Duration.millis(300), root);
            fadeIn.setFromValue(0);
            fadeIn.setToValue(1);

            // Update the main scene's root and stage title
            mainScene.setRoot(root);
            primaryStage.setTitle(title);
            fadeIn.play(); // Start the fade-in effect
        } catch (IOException e) {
            System.err.println("Failed to load view: " + fxmlPath);
            e.printStackTrace();
        }
    }

    /**
     * Main method to launch the JavaFX application.
     *
     * @param args Command-line arguments (if any).
     */
    public static void main(String[] args) {
        launch(args);
    }
}
