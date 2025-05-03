package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import models.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Controller class for managing subscribers in the library system.
 * <p>
 * This controller provides functionalities to:
 * <ul>
 *     <li>Search for subscribers based on their details.</li>
 *     <li>Register new subscribers.</li>
 *     <li>Freeze or unfreeze subscriber accounts.</li>
 *     <li>View subscriber cards.</li>
 * </ul>
 * </p>
 * <p>
 * All interactions with the server are handled through the {@link ClientControllerNew} instance.
 * </p>
 * 
 * 
 * @version 1.0
 */
public class ManageSubscribersController {
	
	/** Holds the currently selected subscriber for further actions. */
	public static User selectedSubscriber; // Holds the currently selected subscriber

	/** TableView to display the list of subscribers. */
    @FXML
    private TableView<User> subscribersTable;

    /** Column displaying subscription numbers. */
    @FXML
    private TableColumn<User, String> subscriptionNumberColumn;

    /** Column displaying subscriber names. */
    @FXML
    private TableColumn<User, String> nameColumn;

    /** Column displaying subscriber emails. */
    @FXML
    private TableColumn<User, String> emailColumn;

    /** Column displaying subscriber mobile numbers. */
    @FXML
    private TableColumn<User, String> mobileNumberColumn;

    /** Column displaying subscriber account statuses. */
    @FXML
    private TableColumn<User, String> accountStatusColumn;

    /** TextField for entering search queries. */
    @FXML
    private TextField searchField;

    /** ObservableList to manage the list of subscribers displayed in the TableView. */
    private ObservableList<User> subscribersList = FXCollections.observableArrayList();

    /**
     * Initializes the controller by setting up the table columns, linking to the server,
     * and loading the initial list of subscribers.
     */
    public void initialize() {
    	ClientControllerNew.getInstance().getChatClient().setManageSubscribersController(this);
    	// Bind columns to Subscriber properties
        subscriptionNumberColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSubscriber().getSubscriptionNumber()));
        nameColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getUsername()));
        emailColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSubscriber().getEmail()));
        mobileNumberColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSubscriber().getMobilePhoneNumber()));
        accountStatusColumn.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue().getSubscriber().getAccountStatus()));

        // Load subscribers into the table
        loadSubscribers();
    }

    /**
     * Handles the search functionality by filtering the subscriber list based on the user's query.
     */
    @FXML
    private void onSearchClicked() {
        String query = searchField.getText().trim().toLowerCase();
        if (query.isEmpty()) {
        	// Reset table to show all subscribers
            subscribersTable.setItems(subscribersList);
        } else {
            ObservableList<User> filteredList = FXCollections.observableArrayList();
            for (User subscriber : subscribersList) {
                // Check if the query matches the username, subscription number, or email
                if (subscriber.getUsername().toLowerCase().contains(query) ||
                        subscriber.getSubscriber().getSubscriptionNumber().toLowerCase().contains(query) ||
                        subscriber.getSubscriber().getEmail().toLowerCase().contains(query)) {
                    filteredList.add(subscriber);
                }
            }
            subscribersTable.setItems(filteredList);
        }
    }

    /**
     * Fetches the list of subscribers from the server and updates the table.
     */
    public void loadSubscribers() {
        try {
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.FETCH_SUBSCRIBERS});
        } catch (Exception e) {
        	ShowMessageController.showError("Error", "Failed to load subscribers.");
        }
    }

    /**
     * Updates the subscribers table with the list fetched from the server.
     *
     * @param fetchedSubscribers List of subscribers fetched from the server.
     */
    public void updateSubscribersList(List<User> fetchedSubscribers) {
        subscribersList.setAll(fetchedSubscribers);
        subscribersTable.setItems(subscribersList);
    }

    /**
     * Handles the registration of a new subscriber.
     * Opens a dialog to collect subscriber details and sends them to the server.
     */
    @FXML
    private void onRegisterClicked() {
        Dialog<List<Object>> dialog = new Dialog<>();
        dialog.setTitle("Register New Subscriber");
        dialog.setHeaderText("Enter new subscriber details:");

        // Set dialog fields
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new javafx.geometry.Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Name");
        TextField passwordField = new TextField();
        passwordField.setPromptText("Password");
        TextField userId = new TextField();
        userId.setPromptText("Subscriber ID");
        TextField emailField = new TextField();
        emailField.setPromptText("Email");
        TextField mobileNumberField = new TextField();
        mobileNumberField.setPromptText("Mobile Number");
        ComboBox<String> accountStatusField = new ComboBox<>();
        accountStatusField.getItems().addAll("Active", "Frozen");
        accountStatusField.setValue("Active");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Password:"), 0, 1);
        grid.add(passwordField, 1, 1);
        grid.add(new Label("Subscriber ID:"), 0, 2);
        grid.add(userId, 1, 2);
        grid.add(new Label("Email:"), 0, 3);
        grid.add(emailField, 1, 3);
        grid.add(new Label("Mobile Number:"), 0, 4);
        grid.add(mobileNumberField, 1, 4);
        grid.add(new Label("Account Status:"), 0, 5);
        grid.add(accountStatusField, 1, 5);

        dialog.getDialogPane().setContent(grid);

        ButtonType registerButtonType = new ButtonType("Register", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(registerButtonType, ButtonType.CANCEL);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == registerButtonType) {
                List<Object> subscriberDetails = new ArrayList<>();
                subscriberDetails.add(Integer.parseInt(userId.getText().trim()));
                subscriberDetails.add(nameField.getText().trim());
                subscriberDetails.add(passwordField.getText().trim());
                subscriberDetails.add(emailField.getText().trim());
                subscriberDetails.add(mobileNumberField.getText().trim());
                subscriberDetails.add(accountStatusField.getValue());
                return subscriberDetails;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(subscriberDetails -> {
            if (validateSubscriber(subscriberDetails)) {
                ClientControllerNew.getInstance().accept(new Object[]{ClientAction.REGISTER_SUBSCRIBER, subscriberDetails});
            }
        });
    }

    /**
     * Validates the subscriber details entered by the user.
     *
     * @param subscriberDetails List of subscriber details.
     * @return True if valid, false otherwise.
     */
    private boolean validateSubscriber(List<Object> subscriberDetails) {
        if (subscriberDetails.get(1).toString().isEmpty() || subscriberDetails.get(3).toString().isEmpty() || subscriberDetails.get(4).toString().isEmpty()) {
        	ShowMessageController.showError("Invalid Input", "All fields are required.");
            return false;
        }
        if (!subscriberDetails.get(3).toString().contains("@")) {
        	ShowMessageController.showError("Invalid Input", "Email must be valid.");
            return false;
        }
        return true;
    }

    /**
     * Handles freezing the selected subscriber's account.
     */
    @FXML
    private void onFreezeClicked() {
        User selectedSubscriber = (User) subscribersTable.getSelectionModel().getSelectedItem();
        if (selectedSubscriber != null) {
        		System.out.println(selectedSubscriber.getSubscriber().getAccountStatus());
        		ClientControllerNew.getInstance().accept(new Object[]{ClientAction.FREEZE_ACCOUNT, selectedSubscriber.getSubscriber().getSubscriberId()});
        	
        } else {
        	ShowMessageController.showWarning("No Selection", "Please select a subscriber to freeze.");
        }
    }

    /**
     * Handles unfreezing the selected subscriber's account.
     */
    @FXML
    private void onUnfreezeClicked() {
        User selectedSubscriber = (User) subscribersTable.getSelectionModel().getSelectedItem();
        if (selectedSubscriber != null) {
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.UNFREEZE_ACCOUNT, selectedSubscriber.getSubscriber().getSubscriberId()});

        } else {
        	ShowMessageController.showWarning("No Selection", "Please select a subscriber to unfreeze.");
        }
    }

    /**
     * Handles viewing the selected subscriber's card.
     */
    @FXML
    private void onViewCardClicked() {
        selectedSubscriber = subscribersTable.getSelectionModel().getSelectedItem();
        if (selectedSubscriber != null) {
            try {
                // Send only the subscriberId to the server
                ClientControllerNew.getInstance().accept(new Object[]{
                    ClientAction.GET_CARD,
                    selectedSubscriber.getSubscriber().getSubscriberId(), selectedSubscriber.getPassword()
                });

                // Load the SubscriberCard.fxml after sending the request
                
                MainController.loadView("/views/subscriberCard.fxml", "Subscriber Card");

            } catch (Exception e) {
            	ShowMessageController.showError("Error", "Failed to fetch subscriber details.");
            }
        } else {
        	ShowMessageController.showWarning("No Selection", "Please select a subscriber to view their card.");
        }
    }

    
    /**
     * Handles the back button click and navigates back to the Librarian Dashboard.
     */
    @FXML
    private void onBackClicked() {
        try {
        	ClientControllerNew.getInstance().getChatClient().setManageSubscribersController(null);
            MainController.loadView("/views/librarianDashboard.fxml", "Librarian Dashboard");
        } catch (Exception e) {
        	ShowMessageController.showError("Navigation Error", "Failed to navigate back to the dashboard. Please try again.");
        }
    }

}
