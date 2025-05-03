package Server;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import gui.ServerPortFrameController;

/**
 * The {@code ServerUI} class serves as the main entry point for the server application.
 * It initializes and displays the graphical user interface (GUI) for configuring 
 * and managing the server, and optionally starts the server on a specified port.
 *
 * <p>This application uses JavaFX for its GUI, and it integrates with the 
 * {@link EchoServer} class to handle client connections and server operations.</p>
 *
 * <h2>Key Features</h2>
 * <ul>
 *   <li>Loads and displays the server configuration screen from an FXML file.</li>
 *   <li>Allows the user to start the server on a specified or default port.</li>
 *   <li>Handles errors gracefully, logging details to the console for debugging.</li>
 * </ul>
 *
 * <h2>Dependencies</h2>
 * <p>Ensure that the required FXML file ({@code ServerPort.fxml}) is present in the 
 * correct directory, and that the {@code EchoServer} class is properly configured.</p>
 *
 * <h2>Example Usage</h2>
 * <pre>{@code
 * public static void main(String[] args) {
 *     ServerUI.launch(args); // Start the server application
 * }
 * }</pre>
 *
 * @version 1.0
 * @since 2025-01-26
 */
public class ServerUI extends Application {
	/**
     * The default port number on which the server listens.
     */
    final public static int DEFAULT_PORT = 5555;
    
    /**
     * The main entry point of the server application.
     * Launches the JavaFX application.
     *
     * @param args Command-line arguments passed to the application.
     * @throws Exception If an error occurs during the application launch.
     */
    public static void main(String[] args) throws Exception {
        launch(args);
    }

    /**
     * Initializes and displays the server interface.
     * Loads the FXML file for the server GUI, sets up the stage and scene, 
     * and optionally starts the server on a default or specified port.
     *
     * @param primaryStage The primary stage for the server GUI.
     * @throws Exception If an error occurs while initializing the GUI.
     */
    @Override
    public void start(Stage primaryStage) throws Exception {
        try {
            // Load the FXML file
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/ServerPort.fxml"));
            Parent root = loader.load();

            // Get the controller instance from the loader
            ServerPortFrameController aFrame = loader.getController();

            // Set up the scene and stage
            Scene scene = new Scene(root);
            primaryStage.setTitle("Server Menu");
            primaryStage.setScene(scene);
            primaryStage.show();

            // Optionally, run the server
            runServer("5555");

        } catch (IOException e) {
            // Show an error dialog
            showErrorDialog("Error loading the GUI", "An error occurred while loading the server interface.", e);
        }
    }

    /**
     * Runs the server on the specified port. If the port number is invalid or already in use, 
     * an error message is logged, and the server does not start.
     *
     * @param portStr The port number as a string.
     */
    public static void runServer(String portStr) {
        int port;
        try {
            port = Integer.parseInt(portStr);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number. Using default port " + DEFAULT_PORT);
            port = DEFAULT_PORT;
        }

        EchoServer sv = new EchoServer(port);
        try {
            sv.listen(); // Start listening for connections
        } catch (Exception ex) {
            System.err.println("Error: Could not listen for clients on port " + port);
            ex.printStackTrace();
        }
    }

    /**
     * Utility method to log error details to the console for debugging purposes.
     * This method does not display a graphical dialog to the user.
     *
     * @param title   The title of the error message.
     * @param message The error message to log.
     * @param e       The exception to log, including its stack trace.
     */
    private void showErrorDialog(String title, String message, Exception e) {
        System.err.println(title + ": " + message);
        e.printStackTrace();
    }
}
