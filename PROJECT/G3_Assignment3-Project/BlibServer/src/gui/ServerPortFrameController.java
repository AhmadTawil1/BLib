package gui;

import Server.dbControl;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.List;
import java.util.ResourceBundle;
/**
 * The {@code ServerPortFrameController} class serves as the controller for the server's graphical user interface (GUI).
 * It manages user interactions with the server's control panel, including database connection, client monitoring,
 * and server shutdown actions.
 *
 * <p>This controller is linked to an FXML file ({@code ServerPort.fxml}) that defines the layout and structure of 
 * the server GUI. It integrates with the {@code dbControl} class to handle database operations and 
 * dynamically updates the GUI to reflect the server's state.</p>
 *
 * <h2>Key Features:</h2>
 * <ul>
 *   <li>Displays the server's IP address and connected clients in real time.</li>
 *   <li>Provides buttons to connect to or disconnect from the database.</li>
 *   <li>Redirects console output to a GUI {@code TextArea} for logging purposes.</li>
 *   <li>Supports server shutdown and exit functionality.</li>
 * </ul>
 *
 * <h2>Dependencies:</h2>
 * <ul>
 *   <li>The {@code dbControl} class for database connection and operations.</li>
 *   <li>An FXML file ({@code ServerPort.fxml}) for defining the GUI layout.</li>
 * </ul>
 *
 * <h2>Example Usage:</h2>
 * <pre>{@code
 * // Initialize the controller and display the server GUI
 * ServerPortFrameController controller = new ServerPortFrameController();
 * controller.initialize(null, null);
 * }</pre>
 *
 * @version 1.0
 * @since 2025-01-26
 */


public class ServerPortFrameController implements Initializable {
    private static ServerPortFrameController instance;
    /**
     * A label that displays the server's IP address.
     * This is updated during initialization to reflect the current local IP address.
     */
    @FXML
    private Label serverIpLabel;
    
    /**
     * A text field where the database username is entered.
     * Used to authenticate the database connection.
     */
    @FXML
    private TextField dbUsertxt;
    
    /**
     * A text field where the database password is entered.
     * Used to authenticate the database connection.
     */
    @FXML
    private TextField dbPasstxt;
    
    /**
     * A text field where the database name is entered.
     * Specifies the database to which the server will connect.
     */
    @FXML
    private TextField dbNametxt;
    
    /**
     * The "Connect" button in the GUI.
     * Triggers the {@code handleConnect(ActionEvent)} method to attempt a database connection.
     */
    @FXML
    private Button btnConnect;
    
    /**
     * The "Disconnect" button in the GUI.
     * Triggers the {@code handleDisconnect(ActionEvent)} method to disconnect from the database.
     */
    @FXML
    private Button btnDisconnect;
    
    /**
     * The "Exit" button in the GUI.
     * Triggers the {@code getExitBtn(ActionEvent)} method to shut down the server and exit the application.
     */
    @FXML
    private Button btnExit;
    
    /**
     * A label that displays the connection status of the database.
     * Updates dynamically based on the server's database connection state.
     */
    @FXML
    private Label statusLabel;
    
    /**
     * A text area that displays log messages from the server.
     * Captures console output and displays it in the GUI for real-time feedback.
     */
    @FXML
    private TextArea logArea;
    
    /**
     * A table view that displays a list of connected clients.
     * Shows the client ID, IP address, and connection status.
     */
    @FXML
    private TableView<ClientInfo> clientsTable;
    
    /**
     * A table column displaying the unique ID of each connected client.
     */
    @FXML
    private TableColumn<ClientInfo, String> clientIdColumn;
    
    /**
     * A table column displaying the IP address of each connected client.
     */
    @FXML
    private TableColumn<ClientInfo, String> ipColumn;
    
    /**
     * A table column displaying the connection status of each connected client.
     */
    @FXML
    private TableColumn<ClientInfo, String> statusColumn;

    private final ObservableList<ClientInfo> connectedClientsList = FXCollections.observableArrayList();
    /**
     * Initializes the server GUI controller.
     * This method is automatically called after the FXML file has been loaded.
     * It sets up the TableView, redirects console output to the GUI log area, 
     * and displays the server's IP address.
     *
     * @param location  The location used to resolve relative paths for the root object.
     * @param resources The resources used to localize the root object.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        instance = this;

        try {
            String currentIp = InetAddress.getLocalHost().getHostAddress();
            serverIpLabel.setText(currentIp);
        } catch (UnknownHostException e) {
            serverIpLabel.setText("Unable to fetch IP");
            e.printStackTrace();
        }

        PrintStream printStream = new PrintStream(new TextAreaOutputStream(this.logArea));
        System.setOut(printStream);

        // Configure the TableView columns
        clientIdColumn.setCellValueFactory(data -> 
            new SimpleStringProperty(String.valueOf(data.getValue().getClientId())));
        ipColumn.setCellValueFactory(data -> data.getValue().ipAddressProperty());
        statusColumn.setCellValueFactory(data -> data.getValue().statusProperty());

        // Bind the ObservableList to the TableView
        clientsTable.setItems(connectedClientsList);
    }
    /**
     * Gets the singleton instance of the {@code ServerPortFrameController}.
     *
     * @return The instance of the {@code ServerPortFrameController}.
     */
    public static ServerPortFrameController getInstance() {
        return instance;
    }
    /**
     * Updates the list of connected clients displayed in the TableView.
     * This method ensures thread-safe updates to the JavaFX UI by wrapping the operation
     * in a {@code Platform.runLater} call.
     *
     * @param clients A list of {@code ClientInfo} objects representing the connected clients.
     * @throws NullPointerException If the {@code clients} parameter is {@code null}.
     */

    public void updateClientsList(List<ClientInfo> clients) {
        Platform.runLater(() -> connectedClientsList.setAll(clients)); // Efficiently update the list
    }
    /**
     * Handles the action for the "Connect" button.
     * Attempts to connect to the database using the credentials provided in the GUI.
     *
     * @param event The event triggered by the "Connect" button.
     */
    @FXML
    public void handleConnect(ActionEvent event) {
        String user = dbUsertxt.getText();
        String password = dbPasstxt.getText();
        String dbName = dbNametxt.getText();

        if (user.isEmpty() || password.isEmpty() || dbName.isEmpty()) {
            statusLabel.setText("All fields are required.");
            statusLabel.setStyle("-fx-text-fill: red;");
            return;
        }

        try {
            dbControl.connect("localhost", "3306", dbName, user, password);
            statusLabel.setText("Connected to the database.");
            statusLabel.setStyle("-fx-text-fill: green;");
            btnConnect.setDisable(true);
            btnDisconnect.setDisable(false);
        } catch (Exception e) {
            statusLabel.setText("Failed to connect to the database.");
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    /**
     * Handles the action for the "Disconnect" button.
     * Disconnects from the database and updates the GUI status.
     *
     * @param event The event triggered by the "Disconnect" button.
     */
    @FXML
    public void handleDisconnect(ActionEvent event) {
        try {
            dbControl.disconnect();
            statusLabel.setText("Disconnected from the database.");
            statusLabel.setStyle("-fx-text-fill: red;");
            btnConnect.setDisable(false);
            btnDisconnect.setDisable(true);
        } catch (Exception e) {
            statusLabel.setText("Failed to disconnect from the database.");
            statusLabel.setStyle("-fx-text-fill: red;");
            e.printStackTrace();
        }
    }
    /**
     * Handles the action for the "Exit" button.
     * Shuts down the server and exits the application.
     *
     * @param event The event triggered by the "Exit" button.
     */
    @FXML
    public void getExitBtn(ActionEvent event) {
        System.out.println("Shutting down the server...");
        Platform.exit();
        System.exit(0);
    }

    
    
    /**
     * A custom {@code OutputStream} implementation that redirects console output
     * to a JavaFX {@code TextArea}. This class ensures that complete lines are
     * written to the {@code TextArea} to prevent truncation of messages.
     *
     * <p>The {@code TextAreaOutputStream} buffers characters until a full line
     * (ending with a newline character) is received. It then appends the line to
     * the {@code TextArea} on the JavaFX Application Thread, ensuring thread safety.</p>
     *
     * <h2>Key Features:</h2>
     * <ul>
     *   <li>Buffers text to ensure complete lines are appended to the {@code TextArea}.</li>
     *   <li>Handles multi-line chunks written in a single {@code write} call.</li>
     *   <li>Ensures thread-safe updates to the JavaFX UI using {@code Platform.runLater}.</li>
     * </ul>
     *
     * <h2>Example Usage:</h2>
     * <pre>{@code
     * TextArea logArea = new TextArea();
     * TextAreaOutputStream outputStream = new TextAreaOutputStream(logArea);
     * System.setOut(new PrintStream(outputStream));
     * }</pre>
     *
     * @version 1.0
     * @since 2025-01-26
     */

    private static class TextAreaOutputStream extends OutputStream {
        private final TextArea textArea;
        private final StringBuilder buffer = new StringBuilder();
        
        
        /**
         * Constructs a {@code TextAreaOutputStream} that redirects output to the
         * specified {@code TextArea}.
         *
         * @param textArea The {@code TextArea} to which output will be redirected.
         */

        public TextAreaOutputStream(TextArea textArea) {
            this.textArea = textArea;
        }
        
        
        /**
         * Writes a single byte to the output stream.
         * Buffers characters until a full line (ending with a newline character) is received,
         * then appends the line to the {@code TextArea}.
         *
         * @param b The byte to write.
         */
        
        @Override
        public void write(int b) {
            buffer.append((char) b);
            if (b == '\n') { // Only update the TextArea when a full line is received
                Platform.runLater(() -> {
                    textArea.appendText(buffer.toString());
                    buffer.setLength(0); // Clear the buffer
                });
            }
        }
        
        /**
         * Writes a sequence of bytes to the output stream.
         * Buffers the text and appends complete lines (if present) to the {@code TextArea}.
         *
         * @param b   The byte array containing the data to write.
         * @param off The start offset in the data.
         * @param len The number of bytes to write.
         */
        @Override
        public void write(byte[] b, int off, int len) {
            String chunk = new String(b, off, len);
            buffer.append(chunk);
            if (chunk.contains("\n")) { // Handle multiple lines at once
                Platform.runLater(() -> {
                    textArea.appendText(buffer.toString());
                    buffer.setLength(0); // Clear the buffer
                });
            }
        }
    }

}
