package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller for handling borrow and return requests in the library system.
 *
 * <p>This controller provides functionality for:
 * <ul>
 *     <li>Submitting borrow and return requests to the server.</li>
 *     <li>Displaying details of borrow and return transactions.</li>
 *     <li>Logging request actions for user reference.</li>
 *     <li>Navigation back to the Librarian Dashboard.</li>
 * </ul>
 * </p>
 *
 * <p>Usage:
 * <ul>
 *     <li>The controller is linked to its corresponding FXML view.</li>
 *     <li>Users can input subscription numbers and book barcodes to perform borrow/return operations.</li>
 *     <li>Request details and logs are displayed dynamically based on server responses.</li>
 * </ul>
 * </p>
 *
 * 
 * @version 1.0
 */
public class BorrowReturnRequestsController {

	// ----------- FXML Components -----------
	
	/** Text field for entering the subscription number for borrowing books. */
    @FXML
    private TextField subscriptionNumberField;
    
    /** Text field for entering the subscription number for returning books. */
    @FXML
    private TextField subscriptionNumberField2;

    /** Text field for entering the book barcode for borrowing. */
    @FXML
    private TextField borrowBarcodeField;
    
    /** Text field for entering the book barcode for returning. */
    @FXML
    private TextField returnBarcodeField;
    
    /** Text area for displaying borrow details returned from the server. */
    @FXML
    private TextArea borrowDetailsArea;

    /** Text area for displaying return details returned from the server. */
    @FXML
    private TextArea returnDetailsArea;

    /** Text area for appending logs of user actions and server responses. */
    @FXML
    private TextArea detailsMessageArea;

    // ----------- Initialization -----------

    /**
     * Initializes the controller.
     *
     * <p>This method:
     * <ul>
     *     <li>Links the controller to the client for communication with the server.</li>
     *     <li>Prepares the controller to handle borrow and return requests dynamically.</li>
     * </ul>
     * </p>
     */
    public void initialize() {
        try {
        	// Link the controller to the ChatClient instance
            ClientControllerNew.getInstance().getChatClient().setBorrowReturnRequestsController(this);
        } catch (IllegalStateException e) {
            System.err.println("Error: ClientControllerNew is not initialized. " + e.getMessage());
            ShowMessageController.showError("Error", "Unable to initialize the client connection.");
        }
    }

    // ----------- Borrowing Logic -----------

    /**
     * Handles the action when the Confirm Borrow button is clicked.
     *
     * <p>Validates the input fields for subscription number and book barcode,
     * and sends a borrow request to the server if valid.</p>
     */
    @FXML
    private void onConfirmBorrowClicked() {
        String subscriptionNumber = subscriptionNumberField.getText().trim();
        String bookBarcode = borrowBarcodeField.getText().trim();

        if (subscriptionNumber.isEmpty() || bookBarcode.isEmpty()) {
        	ShowMessageController.showWarning("Input Missing", "Please enter both Subscription Number and Book Barcode.");
            return;
        }

        try {
            // Send borrow request to the server
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.BORROW_BOOK, subscriptionNumber, bookBarcode});
            appendDetailsMessage("Borrow request sent for subscription number: " + subscriptionNumber + " and book barcode: " + bookBarcode);
            //showAlert(Alert.AlertType.INFORMATION, "Borrow Request Sent", "The borrow request has been sent successfully.");
        } catch (NumberFormatException e) {
        	ShowMessageController.showError("Invalid Input", "Subscription Number must be a valid integer.");
        } catch (Exception e) {
        	ShowMessageController.showError("Borrow Request Error", "Failed to send the borrow request. Please try again.");
        }
    }

    // ----------- Returning Logic -----------

    /**
     * Handles the action when the Confirm Return button is clicked.
     *
     * <p>Validates the input fields for subscription number and book barcode,
     * and sends a return request to the server if valid.</p>
     */
    @FXML
    private void onConfirmReturnClicked() {
        String subscriptionNumber = subscriptionNumberField2.getText().trim();
        String bookBarcode = returnBarcodeField.getText().trim();

        if (subscriptionNumber.isEmpty() || bookBarcode.isEmpty() ) {
        	ShowMessageController.showWarning("Input Missing", "Please enter the Book Barcode.");
            return;
        }

        try {
        	// Send return request to the server
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.RETURN_REQUEST, subscriptionNumber, bookBarcode});
            appendDetailsMessage("Return request sent for book barcode: " + bookBarcode);
        } catch (Exception e) {
        	ShowMessageController.showError("Return Request Error", "Failed to send the return request. Please try again.");
        }
    }

    // ----------- Server Response Handling -----------

    /**
     * Displays the borrow details returned from the server.
     *
     * @param msg The message containing borrow details.
     */
    public void displayBorrowDetails(String msg) {
        Platform.runLater(() -> {
            borrowDetailsArea.setText(msg); // Set the borrow details
            appendDetailsMessage("Borrow Details: " + msg); // Log the details with a timestamp
        });
    }

    /**
     * Displays the return details returned from the server.
     *
     * @param msg The message containing return details.
     */
    public void displayReturnDetails(String msg) {
        Platform.runLater(() -> {
            returnDetailsArea.setText(msg); // Set the return details
            appendDetailsMessage("Return Details: " + msg); // Log the details with a timestamp
        });
    }

    // ----------- Logging and Navigation -----------

    /**
     * Appends a message to the detailsMessageArea with a timestamp.
     *
     * @param message The message to append.
     */
    public void appendDetailsMessage(String message) {
        Platform.runLater(() -> {
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            detailsMessageArea.appendText("[" + timestamp + "] " + message + "\n");
        });
    }

    /**
     * Handles the Back to Dashboard button click.
     *
     * <p>Removes this controller's reference from the ChatClient and navigates
     * back to the Librarian Dashboard view.</p>
     */
    @FXML
    private void onBackToDashboardClicked() {
        try {
            ClientControllerNew.getInstance().getChatClient().setBorrowReturnRequestsController(null);
            MainController.loadView("/views/librarianDashboard.fxml", "Librarian Dashboard");
        } catch (Exception e) {
        	ShowMessageController.showError("Navigation Error", "Failed to return to the dashboard.");
        }
    }


}
