package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Books;
import models.Messages;
import models.User;

import java.util.List;

/**
 * Controller for managing the Librarian Dashboard.
 *
 * <p>This controller provides functionalities for:
 * <ul>
 *     <li>Searching books and displaying search results in a table.</li>
 *     <li>Managing subscribers and books by navigating to respective views.</li>
 *     <li>Viewing borrow/return requests and reports.</li>
 *     <li>Displaying and managing messages for the librarian.</li>
 *     <li>Logging out and returning to the login screen.</li>
 * </ul>
 * </p>
 *
 * <p>Usage:
 * <ul>
 *     <li>Bind this controller to the corresponding FXML file for the Librarian Dashboard.</li>
 *     <li>Ensure the table and list components are properly configured in the FXML file.</li>
 * </ul>
 * </p>
 *
 * 
 * @version 1.0
 */
public class LibrarianDashboardController {
    // ----------- FXML Components -----------

    /** Label for displaying a greeting message with the librarian's name. */
    @FXML
    private Label greetingLabel;

    /** Text field for entering a search query for books. */
    @FXML
    private TextField searchField;

    /** TableView for displaying book search results. */
    @FXML
    private TableView<Books> booksTable;

    /** TableColumn for displaying the title of a book. */
    @FXML
    private TableColumn<Books, String> titleColumn;

    /** TableColumn for displaying the author of a book. */
    @FXML
    private TableColumn<Books, String> authorColumn;

    /** TableColumn for displaying the subject of a book. */
    @FXML
    private TableColumn<Books, String> subjectColumn;

    /** TableColumn for displaying the description of a book. */
    @FXML
    private TableColumn<Books, String> descriptionColumn;

    /** TableColumn for displaying the availability of a book. */
    @FXML
    private TableColumn<Books, String> availabilityColumn;

    /** TableColumn for displaying the location of a book. */
    @FXML
    private TableColumn<Books, String> locationColumn;

    /** ListView for displaying messages for the librarian. */
    @FXML
    private ListView<String> messagesList;

    /**
     * Initializes the controller.
     *
     * <p>This method:
     * <ul>
     *     <li>Links the controller to the ChatClient instance for server communication.</li>
     *     <li>Configures the bindings for table columns to the Books model properties.</li>
     *     <li>Updates the greeting label with the librarian's name.</li>
     *     <li>Loads messages for the librarian from the server.</li>
     * </ul>
     * </p>
     */
    public void initialize() {
    	// Link this controller to the ChatClient instance
        ClientControllerNew.getInstance().getChatClient().setLibrarianDashboardController(this);

        // Bind TableView columns to Books model properties
        titleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));
        authorColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAuthor()));
        subjectColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSubject()));
        descriptionColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDescription()));

        // Availability with nearest return date
        availabilityColumn.setCellValueFactory(data -> {
            if (data.getValue().getAvailableCopies() > 0) {
                return new SimpleStringProperty(data.getValue().getAvailableCopies() + " Available");
            } else {
                String nearestReturnDate = data.getValue().getNearestReturnDate() != null
                        ? "Next return: " + data.getValue().getNearestReturnDate().toString()
                        : "Unavailable";
                return new SimpleStringProperty(nearestReturnDate);
            }
        });

        locationColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getLocation()));

        // Update the greeting label with the librarian's name
        updateGreetingLabel();

        // Load messages for the librarian
        loadMessages();
    }

    /**
     * Updates the greeting label with the logged-in librarian's name.
     */
    private void updateGreetingLabel() {
        User loggedInUser = ClientControllerNew.getInstance().getChatClient().getLoggedInUser();
        if (loggedInUser != null) {
            Platform.runLater(() -> greetingLabel.setText("Welcome, " + loggedInUser.getUsername() + "!"));
        } else {
            Platform.runLater(() -> greetingLabel.setText("Welcome, Librarian!"));
        }
    }

    // --------------- Messages Logic ---------------
	  /**
	  * Fetches messages for the librarian from the server.
	  */
	 private void loadMessages() {
	     try {
	     	int userId = ClientControllerNew.getInstance().getChatClient().getLoggedInUser().getUserId();
	         ClientControllerNew.getInstance().accept(new Object[]{ClientAction.FETCH_MESSAGES, userId});
	     } catch (Exception e) {
	         ShowMessageController.showError("Error", "Failed to load messages. Please try again.");
	     }
	 }
	 
	  /**
	   * Updates the messages list in the UI.
       *
	   * @param messages List of messages fetched from the server.
	   */
	 public void updateMessages(List<Messages> messages) {
        Platform.runLater(() -> {
            messagesList.getItems().clear();
            for (Messages message : messages) {
                String messageDetails = String.format("[%s] %s: %s", 
                    message.getTimestamp(), 
                    message.getType(), 
                    message.getContent());
                messagesList.getItems().add(messageDetails);
            }
        });
    }

    // --------------- Search Logic ---------------
	/**
     * Handles the action for the Search button click.
     * Sends a search request to the server with the query entered in the search field.
	 */
    @FXML
    private void onSearchBooksClicked() {
        String query = searchField.getText();
        if (query == null || query.isEmpty()) {
            ShowMessageController.showError("Search Error", "Please enter a valid search query.");
            return;
        }

        try {
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.SEARCH_BOOKS, query});
        } catch (Exception e) {
            ShowMessageController.showError("Error", "Unable to send search request. Please try again.");
        }
    }

    /**
     * Updates the TableView with the search results.
     *
     * @param books List of books that match the search query.
     */
    public void updateSearchResults(List<Books> books) {
        Platform.runLater(() -> {
            booksTable.getItems().clear();
            if (books != null && !books.isEmpty()) {
                booksTable.getItems().addAll(books);
            } else {
                ShowMessageController.showInfo("No Results", "No books match your search query.");
            }
        });
    }
    
    /**
     * Handles the Logout button click.
     * Logs out the librarian and navigates to the Login view.
     */
    @FXML
    private void onLogoutClicked() {
        try {
            ClientControllerNew.getInstance().getChatClient().setLibrarianDashboardController(null);
            MainController.loadView("/views/login.fxml", "Library Management System - Login");
        } catch (Exception e) {
            ShowMessageController.showError("Error", "Failed to log out. Please try again.");
        }
    }
    
    /**
     * Navigates to the Manage Subscribers view.
     */
    @FXML
    private void onManageSubscribersClicked() {
        MainController.loadView("/views/manageSubscribers.fxml", "Manage Subscribers");
    }

    /**
     * Navigates to the Manage Books view.
     */
    @FXML
    private void onManageBooksClicked() {
        MainController.loadView("/views/ManageBooks.fxml", "Manage Books");
    }
    
    /**
     * Navigates to the Borrow/Return Requests view.
     */
    @FXML
    private void onBorrowReturnClicked() {
        MainController.loadView("/views/BorrowReturnRequests.fxml", "Borrow/Return Requests");
    }

    /**
     * Navigates to the Reports view.
     */
    @FXML
    private void onViewReportsClicked() {
        MainController.loadView("/views/reports.fxml", "reports");
    }
}

