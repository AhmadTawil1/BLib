package controllers;

import client.ChatClientNew;
import client.ClientControllerNew;
import enums.ClientAction;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

/**
 * Controller for managing the login functionality of the Library Management System.
 * <p>
 * This controller handles:
 * <ul>
 *     <li>User login by validating input credentials and sending login requests to the server.</li>
 *     <li>Navigation to different dashboards based on user roles (Subscriber or Librarian).</li>
 *     <li>Guest access functionality for searching books without logging in.</li>
 *     <li>Exit functionality for logging out and closing the application.</li>
 * </ul>
 * </p>
 *
 * <p>Usage:
 * <ul>
 *     <li>Bind this controller to the corresponding login FXML file.</li>
 *     <li>Ensure that the `ChatClientNew` and `ClientControllerNew` are properly initialized before use.</li>
 * </ul>
 * </p>
 *
 * 
 * @version 1.0
 */
public class LoginController {

    // ----------- FXML Components -----------

    /** TextField for entering the user's ID. */
    @FXML
    private TextField idField;

    /** PasswordField for entering the user's password. */
    @FXML
    private PasswordField passwordField;

    /** Reference to the ChatClient instance for server communication. */
    private ChatClientNew chatClient;

    // ----------- Initialization -----------

    /**
     * Initializes the controller.
     * <p>
     * This method links the controller with the `ClientControllerNew` and `ChatClient` for
     * interacting with the server. It ensures that the login controller is set in the `ChatClient`
     * for handling login responses.
     * </p>
     */
    public void initialize() {
        try {
            // Access the ChatClient instance from ClientControllerNew
            chatClient = ClientControllerNew.getInstance().getChatClient();
            chatClient.setLoginController(this);
        } catch (IllegalStateException e) {
            System.err.println("Error: ClientControllerNew is not initialized. " + e.getMessage());
            ShowMessageController.showError("Error", "Unable to initialize the client connection.");
        }
    }

    // ----------- Event Handlers -----------

    /**
     * Handles the Login button click event.
     * <p>
     * Validates user input for ID and password, then sends a login request to the server.
     * Displays error messages if validation fails or if the login request encounters an issue.
     * </p>
     *
     * @param event The ActionEvent triggered by the Login button.
     */
    @FXML
    private void onLoginClicked(ActionEvent event) {
        String id = idField.getText();
        String password = passwordField.getText();

        if (id.isEmpty() || password.isEmpty()) {
            ShowMessageController.showError("Error", "ID and Password cannot be empty.");
            return;
        }

        // Send login request to the server
        try {
        	int id2 = Integer.parseInt(id);
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.LOGIN_REQUEST, id2, password});
        } catch (Exception e) {
            System.err.println("Error sending login request: " + e.getMessage());
            ShowMessageController.showError("Error", "Unable to send login request. Please try again.");
        }
    }

    /**
     * Handles the Exit button click event.
     * <p>
     * Sends a logout request to the server and gracefully exits the application.
     * </p>
     *
     * @param event The ActionEvent triggered by the Exit button.
     */
    @FXML
    private void onExitClicked(ActionEvent event) {
        try {
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.LOGOUT_REQUEST});
        } catch (Exception e) {
            System.err.println("Error sending logout request: " + e.getMessage());
        }
        
    }
    
    /**
     * Handles the "Search as Guest" button click event.
     * <p>
     * Navigates the user directly to the search view without requiring login credentials.
     * </p>
     *
     * @param event The ActionEvent triggered by the Search as Guest button.
     */
    @FXML
    private void onSearchAsGuestClicked(ActionEvent event) {
        MainController.loadView("/views/search.fxml", "Library Management System - Search");
    }

    // ----------- Server Response Handling -----------

    /**
     * Processes the login response received from the server.
     * <p>
     * Based on the login success and user role, this method navigates to the appropriate
     * dashboard (Subscriber or Librarian). Displays error messages if the login fails or
     * if an unknown role is received.
     * </p>
     *
     * @param success Whether the login attempt was successful.
     * @param role    The user's role, either "Subscriber" or "Librarian".
     */
    public void processLoginResponse(boolean success, String role) {
        if (success) {
            if ("Subscriber".equalsIgnoreCase(role)) {
            	
                MainController.loadView("/views/subscriberDashboard.fxml", "Library Management System - Subscriber Dashboard");
            } else if ("Librarian".equalsIgnoreCase(role)) {
                MainController.loadView("/views/librarianDashboard.fxml", "Library Management System - Librarian Dashboard");
            } else {
                ShowMessageController.showError("Error", "Unknown role received from the server.");
            }
        } else {
            ShowMessageController.showError("Login Failed", "Invalid ID or Password.");
        }
    }

}
