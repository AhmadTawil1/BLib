package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.BorrowRecord;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing the subscriber's borrowed books in the Library Management System.
 * 
 * <p>This controller provides the following functionalities:</p>
 * <ul>
 *   <li>Viewing the list of books currently borrowed by the subscriber.</li>
 *   <li>Requesting an extension for the due date of borrowed books.</li>
 *   <li>Navigating back to the subscriber dashboard.</li>
 * </ul>
 * 
 * <p>Data is fetched and updated in real-time by communicating with the server through `ClientControllerNew`.</p>
 * 
 * 
 * @version 1.0
 */
public class SubscriberBorrowedBooksController {
    // FXML UI components
	
	/** Table to display the list of borrowed books */
    @FXML
    private TableView<BorrowRecord> borrowedBooksTable;

    /** Column to display book titles */
    @FXML
    private TableColumn<BorrowRecord, String> bookTitleColumn;

    /** Column to display the borrow dates */
    @FXML
    private TableColumn<BorrowRecord, String> borrowDateColumn;

    /** Column to display the due dates */
    @FXML
    private TableColumn<BorrowRecord, String> dueDateColumn;
    
    /** Column to display the return dates */
    @FXML
    private TableColumn<BorrowRecord, String> ReturnDateColumn;

    /** Column to display the status of borrowed books */
    @FXML
    private TableColumn<BorrowRecord, String> statusColumn;

    
    /**
     * Initializes the controller.
     * <p>
     * This method sets up data bindings for the borrowed books table, fetches the list of borrowed books,
     * and prepares the interface for user interactions.
     * </p>
     */
    public void initialize() {
        ClientControllerNew.getInstance().getChatClient().setBorrowedBooksController(this);

        // Bind table columns to BorrowRecord properties
        // Set up Borrowing History Table columns
        bookTitleColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBook().getTitle()));
        borrowDateColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBorrowDate().toString()));
        dueDateColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getDueDate().toString()));

        ReturnDateColumn.setCellValueFactory(data -> {
            LocalDate ReturnDate = data.getValue().getReturnDate();
            // Handle null expiration date
            return new javafx.beans.property.SimpleStringProperty(
                (ReturnDate != null) ? ReturnDate.toString() : "N/A"
            );
        });
        
        
        statusColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));
        
      
        // Fetch borrowed books from the server
        loadBorrowedBooks();
    }

    /**
     * Handles the action of requesting an extension for the selected borrowed book.
     * 
     * <p>Validates that the selected book is eligible for an extension and sends the request to the server.</p>
     */
    @FXML
    private void onRequestExtensionClicked() {
        BorrowRecord selectedBook = borrowedBooksTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            ShowMessageController.showError("No Selection", "Please select a book to request an extension.");
            return;
        }

        if (!"Active".equals(selectedBook.getStatus())) {
            ShowMessageController.showError("Extension Not Allowed", "Extensions can only be requested for books that are on time.");
            return;
        }

        try {
            // Send the extension request to the server
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.REQUEST_EXTENSION1, selectedBook.getRecordId(), "system", selectedBook.getDueDate().plusDays(7).toString()});
            //ShowMessageController.showInfo("Extension Requested", "Your extension request for \"" + selectedBook.getBook().getTitle() + "\" has been submitted.");
        } catch (Exception e) {
            ShowMessageController.showError("Error", "Failed to send the extension request. Please try again.");
        }
    }


    /**
     * Updates the borrowed books table with the latest data.
     * 
     * @param records List of borrowed books fetched from the server.
     */
    public void updateBorrowedBooks(List<BorrowRecord> records) {
        Platform.runLater(() -> {
            borrowedBooksTable.getItems().clear(); // Clear existing items
            if (records != null && !records.isEmpty()) {
                borrowedBooksTable.getItems().addAll(records); // Add new items
            }
        });
    }// when the librarian do a borrow the table will be updated

    
    /**
     * Fetches the list of borrowed books for the subscriber from the server.
     */
    private void loadBorrowedBooks() {
        try {
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.FETCH_BORROWED_BOOKS,
                    ClientControllerNew.getInstance().getChatClient().getLoggedInUser().getSubscriber().getSubscriberId()});
        } catch (Exception e) {
            ShowMessageController.showError("Error", "Failed to fetch borrowed books. Please try again.");
        }
    }

    /**
     * Handles the Back button action.
     * Navigates back to the Subscriber Dashboard view.
     */
    @FXML
    private void onBackClicked() {
        try {
        	ClientControllerNew.getInstance().getChatClient().setBorrowedBooksController(null);
            MainController.loadView("/views/subscriberDashboard.fxml", "Subscriber Dashboard");
        } catch (Exception e) {
            ShowMessageController.showError("Navigation Error", "Failed to navigate back. Please try again.");
        }
    }
}
