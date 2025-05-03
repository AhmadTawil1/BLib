package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import models.BorrowRecord;
import models.Issues;
import models.User;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller for managing the subscriber's card in the Library Management System.
 * 
 * <p>This controller provides functionalities for:</p>
 * <ul>
 *   <li>Displaying subscriber details, such as name, subscription number, email, phone number, and account status.</li>
 *   <li>Viewing the subscriber's borrowing history, including book details, borrow dates, due dates, return dates, and statuses.</li>
 *   <li>Displaying and managing usage issues reported by the subscriber.</li>
 *   <li>Allowing the librarian or subscriber to renew the borrowing period for active records.</li>
 *   <li>Enabling reporting of new issues and resolving existing issues.</li>
 * </ul>
 * 
 * <p>The controller integrates with the server through the `ClientControllerNew` to fetch and update subscriber data in real-time.</p>
 * 
 * 
 * @version 1.0
 */
public class SubscriberCardController {
	
	// FXML UI components
	/** Displays the subscriber's name */
    @FXML
    private Label subscriberNameLabel;

    /** Displays the subscriber's subscription number */
    @FXML
    private Label subscriptionNumberLabel;
    
    /** Displays the subscriber's email address */
    @FXML
    private Label emailLabel;

    /** Displays the subscriber's mobile phone number */
    @FXML
    private Label mobileNumberLabel;

    /** Displays the account status of the subscriber */
    @FXML
    private Label statusLabel;

    /** Table to display the borrowing history of the subscriber */
    @FXML
    private TableView<BorrowRecord> borrowingHistoryTable;

    /** Column to display the title of the borrowed books */
    @FXML
    private TableColumn<BorrowRecord, String> bookTitleColumn;

    /** Column to display the borrow dates */
    @FXML
    private TableColumn<BorrowRecord, String> borrowDateColumn;

    /** Column to display the due dates */
    @FXML
    private TableColumn<BorrowRecord, String> DueDateColumn;
    
    /** Column to display the return dates (if available) */
    @FXML
    private TableColumn<BorrowRecord, String> ReturnDateColumn;

    /** Column to display the status of the borrow records (e.g., Active, Returned) */
    @FXML
    private TableColumn<BorrowRecord, String> statusColumn;

    /** Column for adding action buttons like "Renew" */
    @FXML
    private TableColumn<BorrowRecord, Void> actionsColumn;

    /** List view to display the usage issues reported by the subscriber */
    @FXML
    private ListView<String> usageIssuesList;

    /**
     * Initializes the controller.
     * <p>
     * This method sets up data bindings for the borrowing history table, links the controller with the 
     * `ChatClient`, and loads the subscriber's details and usage issues. It also adds action buttons 
     * for renewing borrowing periods and configures placeholders for empty data.
     * </p>
     */
    public void initialize() {
        // Link the controller to the ChatClient
        ClientControllerNew.getInstance().getChatClient().setSubscriberCardController(this);

        // Set up Borrowing History Table columns
        bookTitleColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBook().getTitle()));
        borrowDateColumn.setCellValueFactory(data -> 
            new javafx.beans.property.SimpleStringProperty(data.getValue().getBorrowDate().toString()));
        DueDateColumn.setCellValueFactory(data -> 
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
        
        // Load subscriber details and usage issues  
        loadSubscriberDetails();

        // Add "Renew" buttons to the table
        addActionsToTable();

        // Set placeholders for empty tables/lists
        borrowingHistoryTable.setPlaceholder(new Label("No borrowing history available."));
        usageIssuesList.setPlaceholder(new Label("No usage issues found."));
    }

    /**
     * Adds "Renew" buttons to each row of the borrowing history table for active borrow records.
     */
    private void addActionsToTable() {
        actionsColumn.setCellFactory(param -> new TableCell<BorrowRecord, Void>() {
            private final Button renewButton = new Button("Renew");

            {
                renewButton.setOnAction(event -> {
                    BorrowRecord selectedRecord = getTableView().getItems().get(getIndex());
                    if (selectedRecord != null) {
                        renewBorrowingPeriod(selectedRecord);
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || (!"Active".equals(getTableView().getItems().get(getIndex()).getStatus()))) {
                    setGraphic(null);
                } else {
                    setGraphic(renewButton);
                }
            }
        });

    }

    /**
     * Loads the subscriber's details and updates the UI fields.
     * This includes fetching borrowing history and usage issues.
     */
    public void loadSubscriberDetails() {
        try {
            User currentSubscriber = ClientControllerNew.getInstance().getChatClient().user1;

            if (currentSubscriber != null && currentSubscriber.getSubscriber() != null) {
                Platform.runLater(() -> {
                    // Populate subscriber details
                    subscriberNameLabel.setText(currentSubscriber.getUsername());
                    subscriptionNumberLabel.setText(currentSubscriber.getSubscriber().getSubscriptionNumber());
                    emailLabel.setText(currentSubscriber.getSubscriber().getEmail());
                    mobileNumberLabel.setText(currentSubscriber.getSubscriber().getMobilePhoneNumber());
                    statusLabel.setText(currentSubscriber.getSubscriber().getAccountStatus());

                    // Populate borrowing history table using setUpTable
                    setUpTable(currentSubscriber.getSubscriber().getBorrowingHistory());

                    // Load usage issues
                    loadUsageIssuesFromSubscriber(currentSubscriber.getSubscriber().getUsageIssues());
                });
            } else {
            	ShowMessageController.showError("No Data", "Subscriber details could not be loaded.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            ShowMessageController.showError("Error", "Failed to load subscriber details. Please try again.");
        }
    }

    /**
     * Updates the borrowing history table with the provided records.
     * 
     * @param records List of borrow records to display.
     */
    public void setUpTable(List<BorrowRecord> records) {
        Platform.runLater(() -> {
            borrowingHistoryTable.getItems().clear(); // Clear existing items
            if (records != null && !records.isEmpty()) {
                borrowingHistoryTable.getItems().addAll(records); // Add new items
            }
        });
    }


    /**
     * Populates the usage issues list with the provided data.
     * 
     * @param issues List of usage issues reported by the subscriber.
     */
    private void loadUsageIssuesFromSubscriber(List<Issues> issues) {
        usageIssuesList.getItems().clear();
        for (Issues issue : issues) {
            String issueDetails = String.format(
                "ID: %d, %s (Date: %s, Status: %s)",
                issue.getIssueId(),
                issue.getDescription(),
                issue.getDateReported(),
                issue.getStatus()
            );
            usageIssuesList.getItems().add(issueDetails);
        }
    }

    /**
     * Handles the action to report a new issue for a selected borrow record.
     */
    @FXML
    private void onAddIssueClicked() {
        BorrowRecord selectedRecord = borrowingHistoryTable.getSelectionModel().getSelectedItem();

        if (selectedRecord == null) {
            ShowMessageController.showError("No Selection", "Please select a record to report an issue.");
            return;
        }

        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Report Issue for Borrow Record");
        dialog.setHeaderText("Report an issue for the selected borrow record:");
        dialog.setContentText("Issue Description:");

        dialog.showAndWait().ifPresent(description -> {
            if (description.isEmpty()) {
                ShowMessageController.showError("Invalid Input", "The issue description cannot be empty.");
                return;
            }

            int recordId = selectedRecord.getRecordId();
            LocalDate dateReported = LocalDate.now();
            String status = "Open";

            try {
                ClientControllerNew.getInstance().accept(new Object[] {
                    ClientAction.ADD_USAGE_ISSUE, recordId, description, dateReported, status
                });
                ShowMessageController.showInfo("Success", "The issue has been reported for the selected record.");
            } catch (Exception e) {
                ShowMessageController.showError("Error", "Failed to report the issue. Please try again.");
            }
        });
    }


    /**
     * Handles resolving a selected usage issue.
     */
    @FXML
    private void onResolveIssueClicked() {
        String selectedIssue = usageIssuesList.getSelectionModel().getSelectedItem();
        if (selectedIssue == null) {
            ShowMessageController.showError("No Selection", "Please select an issue to resolve.");
            return;
        }

        int issueId = extractIssueIdFromString(selectedIssue);
        ClientControllerNew.getInstance().accept(new Object[] {ClientAction.RESOLVE_USAGE_ISSUE, issueId});
    }

    /**
     * Extracts the issue ID from the issue string.
     */
    private int extractIssueIdFromString(String issueString) {
        int start = issueString.indexOf("ID: ") + 4;
        int end = issueString.indexOf(",", start);
        return Integer.parseInt(issueString.substring(start, end).trim());
    }

    /**
     * Handles renewing the borrowing period for a selected record.
     * 
     * @param record The borrow record to renew.
     */
    private void renewBorrowingPeriod(BorrowRecord record) {
        TextInputDialog dialog = new TextInputDialog(record.getDueDate().toString());
        dialog.setTitle("Renew Borrowing Period");
        dialog.setHeaderText("Enter new return date:");
        dialog.setContentText("Return Date (YYYY-MM-DD):");

        dialog.showAndWait().ifPresent(date -> {
            try {
                LocalDate newReturnDate = LocalDate.parse(date);
                if (newReturnDate.isBefore(LocalDate.now())) {
                    throw new IllegalArgumentException("The new return date cannot be in the past.");
                }
                ClientControllerNew.getInstance().accept(new Object[] {
                    ClientAction.REQUEST_EXTENSION1,
                    record.getRecordId(),
                    ClientControllerNew.getInstance().getChatClient().getLoggedInUser().getUsername(),
                    newReturnDate.toString()
                });
                
            } catch (Exception e) {
            	ShowMessageController.showError("Error", "Failed to renew the borrowing period.");
            }
        });
    }

    /**
     * Navigates back to the Manage Subscribers view.
     */
    @FXML
    private void onCloseClicked() {
        try {
            ClientControllerNew.getInstance().getChatClient().setSubscriberCardController(null);
            MainController.loadView("/views/manageSubscribers.fxml", "Manage Subscribers");
        } catch (Exception e) {
        	ShowMessageController.showError("Navigation Error", "Failed to navigate back. Please try again.");
        }
    }
    
    /**
     * Refreshes and updates the subscriber details view.
     */
    @FXML
    private void onUpdateClicked() {
        try {
            ClientControllerNew.getInstance().getChatClient().setSubscriberCardController(null);
            ClientControllerNew.getInstance().accept(new Object[]{
                    ClientAction.GET_CARD,
                    ManageSubscribersController.selectedSubscriber.getSubscriber().getSubscriberId(),  ManageSubscribersController.selectedSubscriber.getPassword()
                });
            MainController.loadView("/views/subscriberCard.fxml", "Manage Subscribers");
        } catch (Exception e) {
        	ShowMessageController.showError("Navigation Error", "Failed to navigate back. Please try again.");
        }
    }
}
