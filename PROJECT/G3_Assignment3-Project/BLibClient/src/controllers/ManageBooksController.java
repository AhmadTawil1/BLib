package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.GridPane;
import models.Books;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing books in the library system.
 * <p>
 * This class provides functionalities for:
 * <ul>
 *     <li>Viewing a list of all books in the library.</li>
 *     <li>Searching for books based on various fields.</li>
 *     <li>Adding new books to the library's inventory.</li>
 *     <li>Editing the number of available copies for a book.</li>
 *     <li>Deleting books from the inventory.</li>
 * </ul>
 * </p>
 *
 * <p>All interactions with the server are handled through the {@link ClientControllerNew} instance.</p>
 *
 * 
 * @version 1.0
 */
public class ManageBooksController {

	/** TableView for displaying the list of books. */
    @FXML
    private TableView<Books> booksTable;

    /** Table column for book IDs. */
    @FXML
    private TableColumn<Books, Integer> bookIdColumn;

    /** Table column for book titles. */
    @FXML
    private TableColumn<Books, String> titleColumn;

    /** Table column for book authors. */
    @FXML
    private TableColumn<Books, String> authorColumn;

    /** Table column for book subjects. */
    @FXML
    private TableColumn<Books, String> subjectColumn;

    /** Table column for book descriptions. */
    @FXML
    private TableColumn<Books, String> descriptionColumn;

    /** Table column for the number of available copies. */
    @FXML
    private TableColumn<Books, Integer> availableCopiesColumn;


    /** Table column for book location in the library. */
    @FXML
    private TableColumn<Books, String> locationColumn;

    /** Table column for book barcodes. */
    @FXML
    private TableColumn<Books, String> barcodeColumn;

    /** Table column for the nearest return date of a borrowed book. */
    @FXML
    private TableColumn<Books, LocalDate> nearestReturnDateColumn;

    /** TextField for entering search queries. */
    @FXML
    private TextField searchField;

    /** ObservableList for managing the books displayed in the TableView. */
    private ObservableList<Books> booksList = FXCollections.observableArrayList();

    /**
     * Initializes the controller.
     * <p>
     * Configures table columns to bind with the {@link Books} model properties
     * and loads the initial list of books from the server.
     * </p>
     */
    public void initialize() {
    	ClientControllerNew.getInstance().getChatClient().setManageBooksController(this);
        // Bind columns to Books properties
        bookIdColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getBookId()).asObject());
        titleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getTitle()));
        authorColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getAuthor()));
        subjectColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSubject()));
        descriptionColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getDescription()));
        availableCopiesColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleIntegerProperty(data.getValue().getAvailableCopies()).asObject());
        locationColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getLocation()));
        barcodeColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getBarcode()));
        nearestReturnDateColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleObjectProperty<>(data.getValue().getNearestReturnDate()));

        // Load all books initially
        loadBooks();
    }

    /**
     * Handles the Search button click.
     * <p>
     * Filters the list of books displayed in the TableView based on the user's search query.
     * </p>
     */
    @FXML
    private void onSearchClicked() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
            booksTable.setItems(booksList); // Reset to all books
        } else {
            ObservableList<Books> filteredList = FXCollections.observableArrayList();
            for (Books book : booksList) {
                if (book.getTitle().toLowerCase().contains(query) ||
                    book.getAuthor().toLowerCase().contains(query) ||
                    book.getSubject().toLowerCase().contains(query) ||
                    book.getDescription().toLowerCase().contains(query)) {
                    filteredList.add(book);
                }
            }
            booksTable.setItems(filteredList);
        }
    }

    /**
     * Fetches and loads books into the table by requesting data from the server.
     */
    public void loadBooks() {
        try {
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.FETCH_BOOKS});
        } catch (Exception e) {
        	ShowMessageController.showError("Error", "Failed to load books. Please try again.");
        }
    }

    /**
     * Updates the books list with data received from the server.
     *
     * @param fetchedBooks List of books fetched from the server.
     */
    public void updateBooksList(List<Books> fetchedBooks) {
        booksList.setAll(fetchedBooks);
        booksTable.setItems(booksList);
    }

    /**
     * Handles the Add Book button click.
     * <p>
     * Opens a dialog to enter details for a new book and sends the data to the server.
     * </p>
     */
    @FXML
    private void onAddBookClicked() {
        Dialog<Books> dialog = new Dialog<>();
        dialog.setTitle("Add New Book");
        dialog.setHeaderText("Enter the details of the new book:");

        // Set dialog fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField titleField = new TextField();
        titleField.setPromptText("Title");
        TextField authorField = new TextField();
        authorField.setPromptText("Author");
        TextField subjectField = new TextField();
        subjectField.setPromptText("Subject");
        TextField descriptionField = new TextField();
        descriptionField.setPromptText("Description");
        TextField copiesField = new TextField();
        copiesField.setPromptText("Number of Copies");
        TextField locationField = new TextField();
        locationField.setPromptText("Location");
        TextField barcodeField = new TextField();
        barcodeField.setPromptText("Barcode");

        grid.add(new Label("Title:"), 0, 0);
        grid.add(titleField, 1, 0);
        grid.add(new Label("Author:"), 0, 1);
        grid.add(authorField, 1, 1);
        grid.add(new Label("Subject:"), 0, 2);
        grid.add(subjectField, 1, 2);
        grid.add(new Label("Description:"), 0, 3);
        grid.add(descriptionField, 1, 3);
        grid.add(new Label("Copies:"), 0, 4);
        grid.add(copiesField, 1, 4);
        grid.add(new Label("Location:"), 0, 5);
        grid.add(locationField, 1, 5);
        grid.add(new Label("Barcode:"), 0, 6);
        grid.add(barcodeField, 1, 6);

        dialog.getDialogPane().setContent(grid);

        // Add buttons to the dialog
        ButtonType addButtonType = new ButtonType("Add", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(addButtonType, ButtonType.CANCEL);

        // Handle result conversion from dialog
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == addButtonType) {
                try {
                    String title = titleField.getText().trim();
                    String author = authorField.getText().trim();
                    String subject = subjectField.getText().trim();
                    String description = descriptionField.getText().trim();
                    int copies = Integer.parseInt(copiesField.getText().trim());
                    String location = locationField.getText().trim();
                    String barcode = barcodeField.getText().trim();

                    return new Books(0, title, author, subject, description, location, copies, barcode);
                } catch (NumberFormatException e) {
                	ShowMessageController.showError("Invalid Input", "Copies must be a valid integer.");
                    return null;
                }
            }
            return null;
        });

        // Show the dialog and process the result
        dialog.showAndWait().ifPresent(newBook -> {
            if (newBook != null) {
                newBook.setNearestReturnDate(null); // Nearest return date is not set during addition
                ClientControllerNew.getInstance().accept(new Object[]{ClientAction.ADD_BOOK, newBook});
            }
        });
    }


    /**
     * Handles the Edit Book button click.
     * <p>
     * Opens a dialog to update the number of available copies for the selected book.
     * Sends the updated data to the server.
     * </p>
     */
    @FXML
    private void onEditBookClicked() {
        Books selectedBook = booksTable.getSelectionModel().getSelectedItem();
        if (selectedBook != null) {
            TextInputDialog dialog = new TextInputDialog(String.valueOf(selectedBook.getAvailableCopies()));
            dialog.setTitle("Edit Book");
            dialog.setHeaderText("Edit Available Copies");
            dialog.setContentText("Enter new number of copies:");

            // Process the input after the dialog is closed
            dialog.showAndWait().ifPresent(input -> {
                try {
                    int newCopies = Integer.parseInt(input.trim());
                    if (newCopies < 0) {
                        throw new NumberFormatException();
                    }
                    int id = selectedBook.getBookId();
                    // Send the update request to the server
                    ClientControllerNew.getInstance().accept(new Object[]{ClientAction.EDIT_BOOK, id, newCopies});
                } catch (NumberFormatException e) {
                	ShowMessageController.showError("Invalid Input", "Please enter a valid number for copies.");
                }
            });
        } else {
        	ShowMessageController.showWarning("No Selection", "Please select a book to edit.");
        }
    }

//    /**
//     * Handles the Delete Book button click.
//     * <p>
//     * Prompts the user for confirmation and sends a request to delete the selected book.
//     * </p>
//     */
//    @FXML
//    private void onDeleteBookClicked() {
//        Books selectedBook = booksTable.getSelectionModel().getSelectedItem();
//        if (selectedBook != null) {
//            Alert confirm = new Alert(AlertType.CONFIRMATION, "Are you sure you want to delete this book?");
//            confirm.showAndWait().ifPresent(response -> {
//                if (response == ButtonType.OK) {
//                	// Send delete request to the server
//                    ClientControllerNew.getInstance().accept(new Object[]{ClientAction.DELETE_BOOK, selectedBook.getBookId()});
//                }
//            });
//        } else {
//        	ShowMessageController.showWarning("No Selection", "Please select a book to delete.");
//        }
//    }
//    
    /**
     * Handles the Back button click.
     * <p>
     * Navigates back to the Librarian Dashboard view.
     * </p>
     */

    @FXML
    private void onBackClicked() {
        try {
        	// Unset the current controller reference to avoid memory leaks
        	ClientControllerNew.getInstance().getChatClient().setManageBooksController(null);
        	// Navigate back to the dashboard
            MainController.loadView("/views/librarianDashboard.fxml", "Librarian Dashboard");
        } catch (Exception e) {
        	ShowMessageController.showError("Navigation Error", "Failed to navigate back to the dashboard. Please try again.");
        }
    }

}
