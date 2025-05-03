package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import models.Reservation;
import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing reservations in the library system.
 * <p>
 * This class provides functionalities to:
 * <ul>
 *     <li>Fetch and display reservations in a table.</li>
 *     <li>Cancel active reservations.</li>
 *     <li>Navigate back to the subscriber dashboard.</li>
 * </ul>
 * <p>
 * All interactions with the server are handled through the {@link ClientControllerNew} instance.
 * </p>
 * 
 * 
 * @version 1.0
 */
public class ReservationsController {

	/** TableView for displaying reservations. */
    @FXML
    private TableView<Reservation> reservationsTable;

    /** Column for displaying the title of reserved books. */
    @FXML
    private TableColumn<Reservation, String> bookTitleColumn;

    /** Column for displaying the reservation date. */
    @FXML
    private TableColumn<Reservation, String> reservationDateColumn;

    /** Column for displaying the expiration date of reservations. */
    @FXML
    private TableColumn<Reservation, String> expirationDateColumn;

    /** Column for displaying the reservation status. */
    @FXML
    private TableColumn<Reservation, String> statusColumn;


    /**
     * Initializes the controller by binding table columns to reservation model properties
     * and fetching the initial list of reservations from the server.
     */
    public void initialize() {
    	// Link this controller to the ChatClient instance
    	ClientControllerNew.getInstance().getChatClient().setReservationsController(this);
        // Bind table columns to Reservation model properties
        bookTitleColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getBookTitle()));
        reservationDateColumn.setCellValueFactory(data -> 
        	new javafx.beans.property.SimpleStringProperty(data.getValue().getReservationDate().toString()));

        expirationDateColumn.setCellValueFactory(data -> {
            LocalDate expirationDate = data.getValue().getExpirationDate();
            // Handle null expiration date
            return new javafx.beans.property.SimpleStringProperty(
                (expirationDate != null) ? expirationDate.toString() : "N/A"
            );
        });



        statusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getStatus()));

        // Fetch reservations from the server
        loadReservations();
    }

    /**
     * Handles the action when the Cancel Reservation button is clicked.
     * Cancels the selected reservation if it is active.
     */
    @FXML
    private void onCancelReservationClicked() {
        Reservation selectedReservation = reservationsTable.getSelectionModel().getSelectedItem();

        if (selectedReservation == null) {
            ShowMessageController.showError("No Selection", "Please select a reservation to cancel.");
            return;
        }

        if (!"Active".equals(selectedReservation.getStatus())) {
            ShowMessageController.showError("Invalid Action", "Only active reservations can be canceled.");
            return;
        }

        try {
            // Send cancel reservation request via ClientControllerNew
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.CANCEL_RESERVATION, selectedReservation.getReservationId()});
            ShowMessageController.showInfo("Success", "The reservation has been canceled.");
            loadReservations(); // Refresh the table after cancellation
        } catch (Exception e) {
            ShowMessageController.showError("Error", "Failed to cancel the reservation. Please try again.");
        }
    }


    /**
     * Updates the reservations displayed in the TableView.
     * This method is called when new reservation data is received from the server.
     *
     * @param reservations The list of reservations to display.
     */
    public void updateReservations(List<Reservation> reservations) {
        Platform.runLater(() -> {
            reservationsTable.getItems().clear(); // Clear existing items
            if (reservations != null && !reservations.isEmpty()) {
                reservationsTable.getItems().addAll(reservations); // Add new items
            }
        });
    }


    /**
     * Fetches the list of reservations from the server.
     * The request is sent for the currently logged-in subscriber.
     */
    private void loadReservations() {
        try {
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.FETCH_RESERVATIONS,
            		ClientControllerNew.getInstance().getChatClient().getLoggedInUser().getSubscriber().getSubscriberId()});
        } catch (Exception e) {
            ShowMessageController.showError("Error", "Failed to load reservations. Please try again.");
        }
    }

    /**
     * Handles the Back button click action.
     * Navigates back to the subscriber dashboard.
     */
    @FXML
    private void onBackClicked() {
        try {
        	ClientControllerNew.getInstance().getChatClient().setReservationsController(null);
            MainController.loadView("/views/subscriberDashboard.fxml", "Subscriber Dashboard");
        } catch (Exception e) {
            ShowMessageController.showError("Navigation Error", "Failed to return to the dashboard.");
        }
    }

}
