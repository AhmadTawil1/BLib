package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import models.Books;

import java.util.List;


/**
 * Controller for the search functionality in the Library Management System.
 * <p>
 * This class enables users to search for books in the library database.
 * It allows users to:
 * <ul>
 *     <li>Search for books based on titles, authors, subjects, and descriptions.</li>
 *     <li>Display search results in a table with detailed information about each book.</li>
 *     <li>Navigate back to the login view.</li>
 * </ul>
 * <p>
 * The controller interacts with the server to retrieve search results.
 * </p>
 * 
 * 
 * @version 1.0
 */
public class SearchController {

	/** TextField for entering the search query. */
    @FXML
    private TextField searchBar;

    /** TableView for displaying search results. */
    @FXML
    private TableView<Books> resultsTable;

    /** TableColumn for displaying the title of books. */
    @FXML
    private TableColumn<Books, String> titleColumn;

    /** TableColumn for displaying the author of books. */
    @FXML
    private TableColumn<Books, String> authorColumn;

    /** TableColumn for displaying the subject of books. */
    @FXML
    private TableColumn<Books, String> subjectColumn;

    /** TableColumn for displaying the description of books. */
    @FXML
    private TableColumn<Books, String> descriptionColumn;

    /** TableColumn for displaying the availability status of books. */
    @FXML
    private TableColumn<Books, String> availabilityColumn;

    /** TableColumn for displaying the location of books in the library. */
    @FXML
    private TableColumn<Books, String> locationColumn;

    /**
     * Initializes the controller.
     * <p>
     * This method sets up bindings between the table columns and the properties
     * of the {@link Books} model. It also links this controller to the ChatClient for server communication.
     * </p>
     */
    public void initialize() {
    	ClientControllerNew.getInstance().getChatClient().setSearchController(this);
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
    }

    /**
     * Handles the search button click event.
     * <p>
     * This method validates the search query entered by the user. If the query is valid,
     * it sends a search request to the server.
     * </p>
     */
    @FXML
    private void onSearchClicked() {
        String query = searchBar.getText().trim();

        if (query.isEmpty()) {
            ShowMessageController.showError("Error", "Please enter a search query.");
            return;
        }

        try {
            // Send the query to the server
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.SEARCH_BOOKS, query});
        } catch (Exception e) {
            ShowMessageController.showError("Search Error", "Failed to send the search request. Please try again.");
        }
    }

    /**
     * Updates the search results in the table.
     * <p>
     * This method is called when the server returns search results. It updates
     * the TableView to display the results.
     * </p>
     * 
     * @param results List of books returned by the server.
     */
    public void updateSearchResults(List<Books> results) {
        Platform.runLater(() -> {
            resultsTable.getItems().clear();
            if (results != null && !results.isEmpty()) {
                resultsTable.getItems().addAll(results);
            } else {
                ShowMessageController.showInfo("No Results", "No books match your search query.");
            }
        });
    }

    /**
     * Handles the back arrow click event.
     * <p>
     * This method navigates the user back to the login view.
     * </p>
     */
    @FXML
    private void onBackArrowClicked() {
        try {
        	ClientControllerNew.getInstance().getChatClient().setSearchController(null);
            MainController.loadView("/views/login.fxml", "Library Management System - Login");
        } catch (Exception e) {
            ShowMessageController.showError("Navigation Error", "Failed to navigate back. Please try again.");
        }
    }
}
