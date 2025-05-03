package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Issues;
import models.User;

import java.util.List;

/**
 * Controller for managing a subscriber's account in the library system.
 *
 * <p>This controller is responsible for:
 * <ul>
 *     <li>Displaying subscriber details (e.g., name, email, phone number, and account status).</li>
 *     <li>Providing functionality to update subscriber details such as email and phone number.</li>
 *     <li>Displaying any usage issues related to the subscriber's account.</li>
 *     <li>Allowing navigation back to the Subscriber Dashboard.</li>
 * </ul>
 * </p>
 *
 * <p>Usage:
 * <ul>
 *     <li>This controller is linked to the UI via FXML and is initialized when the associated view is loaded.</li>
 *     <li>The subscriber details are fetched dynamically from the server and displayed in the UI.</li>
 *     <li>Changes to email or phone are sent to the server for validation and persistence.</li>
 * </ul>
 * </p>
 *
 * 
 * @version 1.0
 */
public class AccountManagementController {

	 // ----------- FXML Components -----------
	/** Label for displaying the subscriber's name. */
    @FXML
    private Label nameField;
    
    /** Label for displaying the subscriber's account status (e.g., Active, Frozen). */
    @FXML
    private Label accountStatusField;

    /** Text field for editing the subscriber's email. */
    @FXML
    private TextField emailField;
    
    /** Text field for editing the subscriber's phone number. */
    @FXML
    private TextField phoneField;

    /** ListView for displaying the subscriber's usage issues. */
    @FXML
    private ListView<String> usageIssuesList;

    // ----------- Initialization -----------

    /**
     * Initializes the controller.
     *
     * <p>During initialization, this method:
     * <ul>
     *     <li>Links this controller to the ChatClient for server communication.</li>
     *     <li>Loads the subscriber's details and populates the UI fields.</li>
     * </ul>
     * </p>
     */
    public void initialize() {
    	// Set this controller instance in the ChatClient
        ClientControllerNew.getInstance().getChatClient().setAccountManagementController(this);
        
        // Load subscriber details from the server or cache
        loadSubscriberDetails();
    }

    // ----------- Methods for Subscriber Details -----------

    /**
     * Fetches and displays the subscriber's details in the UI.
     *
     * <p>This method retrieves the logged-in user's details from the server
     * and populates the UI components such as name, email, phone number,
     * account status, and usage issues.</p>
     */
    public void loadSubscriberDetails() {
        try {
        	// Get the logged-in user
            User subscriber = ClientControllerNew.getInstance().getChatClient().getLoggedInUser();
            if (subscriber != null && subscriber.getSubscriber() != null) {
            	// Update the UI with subscriber details
                Platform.runLater(() -> {
                    nameField.setText(subscriber.getUsername());
                    emailField.setText(subscriber.getSubscriber().getEmail());
                    phoneField.setText(subscriber.getSubscriber().getMobilePhoneNumber());
                    accountStatusField.setText(subscriber.getSubscriber().getAccountStatus());

                    // Populate the usage issues list
                    updateUsageIssues(subscriber.getSubscriber().getUsageIssues());
                });
            } else {
            	// Show warning if subscriber details are unavailable
            	ShowMessageController.showWarning("No Data", "Subscriber details could not be loaded.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert if loading fails
            ShowMessageController.showError("Error", "Failed to load subscriber details. Please try again.");
        }
    }

    /**
     * Updates the ListView with the subscriber's usage issues.
     *
     * @param issues A list of {@link Issues} associated with the subscriber.
     */
    private void updateUsageIssues(List<Issues> issues) {
        Platform.runLater(() -> {
            usageIssuesList.getItems().clear(); // Clear any existing items
            for (Issues issue : issues) {
            	// Format each issue for display in the list
                String issueDetails = String.format("ID: %d, %s (Date: %s, Status: %s)", 
                        issue.getIssueId(), 
                        issue.getDescription(), 
                        issue.getDateReported(), 
                        issue.getStatus());
                usageIssuesList.getItems().add(issueDetails); // Add formatted issue to the list
            }
        });
    }

    // ----------- Methods for Editing Subscriber Details -----------

    /**
     * Handles the "Save Changes" button click.
     *
     * <p>This method validates the input fields for email and phone number,
     * and sends the updated details to the server for processing.</p>
     */
    @FXML
    private void onSaveChangesClicked() {
        try {
        	// Retrieve updated email and phone number from input fields
            String updatedEmail = emailField.getText().trim();
            String updatedPhone = phoneField.getText().trim();

            // Validate input fields
            if (updatedEmail.isEmpty() || updatedPhone.isEmpty()) {
            	ShowMessageController.showWarning("Validation Error", "Email and Phone cannot be empty.");
                return;
            }
            
            // Get the current subscriber details
            User subscriber = ClientControllerNew.getInstance().getChatClient().getLoggedInUser();
            if (subscriber != null && subscriber.getSubscriber() != null) {
                // Send updated details to the server
                ClientControllerNew.getInstance().accept(new Object[]{ClientAction.UPDATE_SUBSCRIBER_DETAILS, subscriber.getUserId(),
                		updatedEmail, updatedPhone});
                
                // Show success alert
                //ShowMessageController.showInfo("Success", "Details updated successfully!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Show error alert if the update fails
            //ShowMessageController.showError("Update Error", "Failed to save changes. Please try again.");
        }
    }
    
    /**
     * Updates the email and phone fields in the UI after changes have been made to the subscriber's details.
     * This method retrieves the latest subscriber information from the logged-in user's data and updates the
     * corresponding fields in the account management view.
     */
    public void updateAfterChange() {
    	User subscriber = ClientControllerNew.getInstance().getChatClient().getLoggedInUser();
    	emailField.setText(subscriber.getSubscriber().getEmail());
        phoneField.setText(subscriber.getSubscriber().getMobilePhoneNumber());
    }
    
    

    // ----------- Navigation -----------

    /**
     * Handles the "Back" button click.
     *
     * <p>This method navigates the user back to the Subscriber Dashboard.</p>
     */
    @FXML
    private void onBackClicked() {
        try {
        	ClientControllerNew.getInstance().getChatClient().setAccountManagementController(null);
            MainController.loadView("/views/subscriberDashboard.fxml", "Subscriber Dashboard");
        } catch (Exception e) {
        	ShowMessageController.showError("Navigation Error", "Failed to navigate back.");
        }
    }
}
