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
 * Controller for managing the Subscriber Dashboard in the Library Management System.
 * 
 * <p>This controller provides functionalities for:</p>
 * <ul>
 *   <li>Displaying a personalized greeting for the subscriber.</li>
 *   <li>Viewing, searching, and reserving books from the library catalog.</li>
 *   <li>Managing and displaying messages sent to the subscriber.</li>
 *   <li>Navigating to other sections such as borrowing history, reservations, and account details.</li>
 *   <li>Logging out of the subscriber account.</li>
 * </ul>
 * 
 * <p>The controller communicates with the server via the `ClientControllerNew` to fetch and update data in real-time.</p>
 * 
 * 
 * @version 1.0
 */
public class SubscriberDashboardController {

	// FXML UI components
	
	/** Displays a personalized greeting for the subscriber */
    @FXML
    private Label greetingLabel;

    /** Input field for entering book search queries */
    @FXML
    private TextField searchField;

    /** Table to display the search results */
    @FXML
    private TableView<Books> booksTable;

    /** Column to display book titles */
    @FXML
    private TableColumn<Books, String> titleColumn;

    /** Column to display book authors */
    @FXML
    private TableColumn<Books, String> authorColumn;

    /** Column to display book subjects */
    @FXML
    private TableColumn<Books, String> subjectColumn;

    /** Column to display book descriptions */
    @FXML
    private TableColumn<Books, String> descriptionColumn;

    /** Column to display book availability status */
    @FXML
    private TableColumn<Books, String> availabilityColumn;

    /** Column to display book locations */
    @FXML
    private TableColumn<Books, String> locationColumn;

    /** List view to display messages sent to the subscriber */
    @FXML
    private ListView<String> messagesList;

    /**
     * Initializes the controller.
     * <p>
     * This method sets up data bindings for the books table, initializes the personalized greeting,
     * and loads messages for the subscriber.
     * </p>
     */
    public void initialize() {
        ClientControllerNew.getInstance().getChatClient().setSubscriberDashboardController(this);

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

        // Update the greeting label with the user's name
        updateGreetingLabel();

        // Load messages for the subscriber
        loadMessages();
    }

    /**
     * Updates the greeting label with the logged-in user's name.
     */
    private void updateGreetingLabel() {
        User loggedInUser = ClientControllerNew.getInstance().getChatClient().getLoggedInUser();
        if (loggedInUser != null) {
            Platform.runLater(() -> greetingLabel.setText("Welcome, " + loggedInUser.getUsername() + "!"));
        } else {
            Platform.runLater(() -> greetingLabel.setText("Welcome, Subscriber!"));
        }
    }

    // --------------- Messages Logic ---------------

    /**
     * Fetches messages for the subscriber from the server.
     */
    private void loadMessages() {
        try {
            int subscriberId = ClientControllerNew.getInstance().getChatClient().getLoggedInUser().getUserId();
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.FETCH_MESSAGES, subscriberId});
        } catch (Exception e) {
            ShowMessageController.showError("Error", "Failed to load messages. Please try again.");
        }
    }

    /**
     * Updates the messages list with data fetched from the server.
     *
     * @param messages List of messages sent to the subscriber.
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
     * Handles the search button click.
     * Sends a book search query to the server.
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
     * Updates the TableView with search results fetched from the server.
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

    // --------------- Request Reservation Logic ---------------

    /**
     * Handles the Reserve button click.
     * Sends a request to reserve the selected book if it is unavailable.
     */
    @FXML
    private void onReserveBookClicked() {
        Books selectedBook = booksTable.getSelectionModel().getSelectedItem();

        if (selectedBook == null) {
            ShowMessageController.showError("No Selection", "Please select a book to reserve.");
            return;
        }

        if (selectedBook.getAvailableCopies() > 0) {
            ShowMessageController.showInfo("Reservation Not Needed", "This book is currently available.");
            return;
        }

        try {
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.RESERVE_BOOK, selectedBook.getBookId(),
                    ClientControllerNew.getInstance().getChatClient().getLoggedInUser().getSubscriber().getSubscriberId()});
        } catch (Exception e) {
            ShowMessageController.showError("Error", "Failed to place reservation. Please try again.");
        }
    }

    // --------------- Logout Logic ---------------

    /**
     * Handles the logout button click.
     * Logs out the subscriber and navigates to the login view.
     */
    @FXML
    private void onLogoutClicked() {
        try {
            ClientControllerNew.getInstance().getChatClient().setSubscriberDashboardController(null);
            MainController.loadView("/views/login.fxml", "Library Management System - Login");
        } catch (Exception e) {
            ShowMessageController.showError("Error", "Failed to log out. Please try again.");
        }
    }

    // --------------- Navigation Logic ---------------

    /**
     * Navigates to the "Borrowed Books" view.
     */
    public void onViewBorrowedBooksClicked() {
        MainController.loadView("/views/SubscriberBorrowedBooks.fxml", "Subscriber Borrowed Books");
    }

    /**
     * Navigates to the "Manage Reservations" view.
     */
    public void onManageReservationsClicked() {
        MainController.loadView("/views/ReservationsSection.fxml", "Manage Reservations");
    }

    /**
     * Navigates to the "Account Management" view.
     */
    public void onViewAccountDetailsClicked() {
        MainController.loadView("/views/AccountManagement.fxml", "Account Management");
    }
}
