// This file contains material supporting section 3.7 of the textbook:
// "Object Oriented Software Engineering" and is issued under the open-source
// license found at www.lloseng.com 

package client;

import ocsf.client.*;
import controllers.AccountManagementController;
import controllers.BorrowReturnRequestsController;
import controllers.LibrarianDashboardController;
import controllers.LoginController;
import controllers.MainController;
import controllers.ManageBooksController;
import controllers.ManageSubscribersController;
import controllers.ReportsController;
import controllers.ReservationsController;
import controllers.SearchController;
import controllers.SubscriberBorrowedBooksController;
import controllers.SubscriberCardController;
import controllers.SubscriberDashboardController;
import controllers.ShowMessageController;
import enums.ServerResponse;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import models.Books;
import models.BorrowRecord;
import models.Messages;
import models.Reports;
import models.Reservation;
import models.Subscriber;
import models.User;
import java.io.*;
import java.util.List;

/**
 * The {@code ChatClientNew} class extends {@link ocsf.client.AbstractClient} to provide additional functionality
 * for handling client-server communication in the Library Management System.
 * 
 * <p>This class is responsible for:
 * <ul>
 *   <li>Connecting to the server and maintaining the connection.</li>
 *   <li>Sending messages from the client to the server.</li>
 *   <li>Receiving and processing responses from the server.</li>
 *   <li>Coordinating UI controllers with server data.</li>
 * </ul>
 *
 * <p>It acts as the bridge between the application's UI and server, handling actions
 * like login, search, managing books/subscribers, and generating reports.</p>
 *
 * 
 * @version 1.0
 */
public class ChatClientNew extends AbstractClient
{
    // Instance Variables **********************************************
    
  /**
   * The interface type variable to allow the implementation of the display method.
   */
  ChatIF clientUI; 
  /**
   * A flag to indicate whether the client is waiting for a server response.
   */
  public static boolean awaitResponse = false;
  /**
   * The main controller for the application, managing navigation and global actions.
   */
  public static MainController chat;
  /**
   * A generic message object for communication purposes.
   */
  public static Object message;
  /**
   * The currently logged-in user.
   */
  public static User user1;
  
  /**
   * Controller for handling login-related operations.
   * Used for validating user credentials and directing users to the appropriate dashboard.
   */
  private LoginController loginController; 
  /**
   * Controller for managing the search functionality.
   * Provides features to search for books based on various criteria.
   */
  private SearchController searchController; 

  private User loggedInUser; 
  /**
   * Controller for managing books in the library system.
   * Enables adding, editing, and fetching book information.
   */
  private ManageBooksController manageBooksController;
  /**
   * Controller for managing library subscribers.
   * Includes operations such as registering, freezing, unfreezing, and viewing subscriber details.
   */
  private ManageSubscribersController manageSubscribersController;
  /**
   * Controller for displaying a subscriber's card.
   * Includes account details, borrowing history, and reported issues.
   */
  private SubscriberCardController subscriberCardController;
  /**
   * Controller for managing borrow and return requests.
   * Handles book borrowing and return functionality for the library system.
   */
  private BorrowReturnRequestsController borrowReturnRequestsController;
  /**
   * Controller for displaying and managing a subscriber's borrowed books.
   * Allows viewing borrowing history and requesting loan extensions.
   */
  private SubscriberBorrowedBooksController borrowedBooksController;
  /**
   * Controller for managing book reservations.
   * Enables users to reserve books and manage their reservations.
   */
  private ReservationsController reservationsController;
  /**
   * Controller for managing a subscriber's account details.
   * Provides functionality for updating email, phone number, and viewing account status.
   */
  private AccountManagementController accountManagementController;
  /**
   * Controller for managing the subscriber's dashboard.
   * Displays messages, book search, reservations, and account-related actions for subscribers.
   */
  private SubscriberDashboardController subscriberDashboardController;
  /**
   * Controller for managing the librarian's dashboard.
   * Provides access to book management, subscriber management, reports, and messages.
   */
  private LibrarianDashboardController librarianDashboardController;
  /**
   * Controller for generating and managing library reports.
   * Includes functionalities for generating borrowing and subscription status reports.
   */
  private ReportsController reportsController; 

  //Constructors ****************************************************
  /**
   * Constructs an instance of the chat client.
   *
   * @param host The server to connect to.
   * @param port The port number to connect on.
   * @param clientUI The interface type variable.
   */
	 
  public ChatClientNew(String host, int port, ChatIF clientUI) 
    throws IOException 
  {
    super(host, port); //Call the superclass constructor
    this.clientUI = clientUI;
  }

  /**
   * Handles messages received from the server.
   *
   * <p>This method is invoked whenever the client receives a response from the server.
   * It identifies the type of response using {@link enums.ServerResponse} and delegates
   * the handling to the appropriate methods.
   *
   * @param msg The message received from the server, typically an array of objects.
   */
  @Override
  public void handleMessageFromServer(Object msg) {
      awaitResponse = false;
      if (!(msg instanceof Object[])) {
          System.err.println("Invalid server message format.");
          return;
      }

      Object[] response = (Object[]) msg;
      ServerResponse action = (ServerResponse) response[0];
      System.out.println("Action received: " + action); // Debug log

      switch (action) {
          case CHECK_CONNECTION_RESPONSE:
              handleCheckConnectionResponse((boolean) response[1]);
              break;
          case LOGIN_RESPONSE:
              handleLoginResponse((boolean) response[1], (User) response[2]);
              break;
          case SEARCH_RESULTS:
              handleSearchResults((List<Books>) response[1]);
              break;
          case FETCH_BOOKS_RESPONSE:
              handleFetchBooksResponse((List<Books>) response[1]);
              break;
          case ADD_BOOK_RESPONSE:
              handleAddBookResponse((boolean) response[1]);
              break;
          case EDIT_BOOK_RESPONSE:
              handleEditBookResponse((boolean) response[1]);
              break;
//          case DELETE_BOOK_RESPONSE:
//              handleDeleteBookResponse((boolean) response[1]);
//              break;
          case FETCH_SUBSCRIBERS_RESPONSE:
              handleFetchSubscribersResponse((List<User>) response[1]);
              break;
          case REGISTER_SUBSCRIBER_RESPONSE:
              handleRegisterSubscriberResponse((boolean) response[1]);
              break;
          case FREEZE_ACCOUNT_RESPONSE:
              handleFreezeAccountResponse((boolean) response[1]);
              break;
          case UNFREEZE_ACCOUNT_RESPONSE:
              handleUnfreezeAccountResponse((boolean) response[1]);
              break;
//          case RENEW_LOAN_RESPONSE:
//              handleRenewLoanResponse((boolean) response[1]);
//              break;
          case BORROW_BOOK_RESPONSE:
              handleBorrowResponse((boolean) response[1], (String) response[2]);
              break;
          case RETURN_BOOK_RESPONSE:
              handleReturnResponse((boolean) response[1], (String) response[2]);
              break;
          case FETCH_BORROWED_BOOKS_RESPONSE:
              handleFetchBorrowedBooksResponse((List<BorrowRecord>) response[1]);
              break;
          case REQUEST_EXTENSION_RESPONSE:
              handleExtensionResponse((boolean) response[1], (String) response[2]);
              break;
          case FETCH_RESERVATIONS_RESPONSE:
              handleFetchReservationsResponse((List<Reservation>) response[1]);
              break;
          case CANCEL_RESERVATION_RESPONSE:
              handleCancelReservationResponse((boolean) response[1], (String) response[2]);
              break;
          case UPDATE_SUBSCRIBER_DETAILS_RESPONSE:
              handleUpdateSubscriberDetailsResponse((boolean) response[1], (String) response[2]);
              break;
          case RESERVATION_RESPONSE:
        	  handleRservationResponse((boolean) response[1], (String) response[2]);
        	  break;
          case GET_CARD_RESPONSE:
        	  user1 = (User) response[1];
        	  handleCardResponse(user1);
        	  break;
          case ADD_USAGE_ISSUE_RESPONSE:
              handleAddUsageIssueResponse((boolean) response[1], (String) response[2]);
              break;

          case RESOLVE_USAGE_ISSUE_RESPONSE:
              handleResolveUsageIssueResponse((boolean) response[1], (String) response[2]);
              break; 
          case FETCH_REPORT_RESPONSE:
        	  handleReportResponse((List<Reports>) response[1]); 
        	  break;
          case FETCH_MESSAGES_RESPONSE:
        	  handleUpdateMessageResponse((List<Messages>) response[1]);
        	  break;
          case LOGOUT_REQUEST_RESPONSE:
        	  quit();
          default:
              System.err.println("Unhandled server response: " + action);
      }

  }
   
  /**
   * Handles a message from the client UI and sends it to the server for processing.
   * 
   * <p>This method ensures the client is connected to the server before sending the message.
   * If the connection is not already established, it will attempt to open the connection.
   * The method then waits for a response from the server, ensuring synchronous communication.</p>
   *
   * @param message The message object to be sent to the server. It is typically an array of objects
   *                that contain the action type and any associated data for the server to process.
   *
   * @throws IOException If an error occurs while opening the connection or sending the message.
   */
  public void handleMessageFromClientUI(Object message) {
	    try {
	        if (!isConnected()) {
	            openConnection();
	            System.out.println("Connection opened successfully."); // Debug log
	        }
	        System.out.println("Sending message to server: " + java.util.Arrays.toString((Object[]) message)); // Debug log
	        awaitResponse = true; // Set the flag
	        sendToServer(message);
	        // Wait for the response
	        while (awaitResponse) {
	            try {
	                Thread.sleep(100);
	            } catch (InterruptedException e) {
	                e.printStackTrace();
	            }
	        }
	        System.out.println("Response received. Exiting wait loop."); // Debug log
	    } catch (IOException e) {
	        System.err.println("Error opening connection or sending message: " + e.getMessage());
	        quit();
	    }
	}
  
  // Helper Methods for Handling Server Responses *********************
  
  /**
   * Handles the response from the server containing updated messages for the user.
   * 
   * <p>This method determines whether the current user is a subscriber or librarian and updates 
   * the respective dashboard's messages list accordingly.</p>
   *
   * @param messages A list of {@link Messages} objects containing message details fetched from the server.
   */
  private void handleUpdateMessageResponse(List<Messages> messages) {
	  Platform.runLater(() -> {
		  if (subscriberDashboardController !=null) {
			  subscriberDashboardController.updateMessages( (List<Messages>) messages);
		  }
		  else if (librarianDashboardController !=null) {
			  librarianDashboardController.updateMessages((List<Messages>) messages);
		  }
	  });
  }

  /**
   * Handles the response from the server containing report data.
   * 
   * <p>This method updates the appropriate report view based on the type of report received,
   * such as "Borrowing Times" or "Subscription Status". If no data is available, an informational
   * message is displayed to the user.</p>
   *
   * @param reports A list of {@link Reports} objects containing data for the requested report.
   */
  private void handleReportResponse(List<Reports> reports) {
      Platform.runLater(() -> {
  	    if(reports == null || reports.isEmpty()) {
            ShowMessageController.showInfo("No Data", "No data available for the Report.");
  	    }
  	    else if (reportsController != null) {
      	    if ("BorrowingTimes".equals(reports.get(0).getReportType())) {
      	            reportsController.updateBorrowingTimesReport(reports);
      	    } else if ("SubscriptionStatus".equals(reports.get(0).getReportType())) {
      	            reportsController.updateSubscriptionStatusReport(reports);
      	            }
      	    }
      });
  }


  /**
   * Handles the login response from the server.
   *
   * @param success Whether the login attempt was successful.
   * @param user    The user object returned by the server, or null if unsuccessful.
   */
  private void handleLoginResponse(boolean success, User user) {
      Platform.runLater(() -> {
          if (success) {
              this.loggedInUser = user; // Store the logged-in user
          }
          if (loginController != null) {
              loginController.processLoginResponse(success, user != null ? user.getRole() : null);
          }
      });
  }


  /**
   * Handles the search results response from the server.
   *
   * @param books The list of books matching the search query.
   */
  private void handleSearchResults(List<Books> books) {
	    Platform.runLater(() -> {
	        if (searchController != null) {
	            searchController.updateSearchResults(books);
	        }
	        if (subscriberDashboardController != null) {
	            subscriberDashboardController.updateSearchResults(books);
	        }
	        if (librarianDashboardController != null) {
	            librarianDashboardController.updateSearchResults(books);}
	    });
	}
  

  /**
   * Handles the response for fetching books.
   *
   * @param books The list of books returned by the server.
   */
  private void handleFetchBooksResponse(List<Books> books) {
      Platform.runLater(() -> {
              manageBooksController.updateBooksList(books);
      });
  }

  /**
   * Handles the add book response from the server.
   *
   * @param success True if the book was added successfully, false otherwise.
   */
  private void handleAddBookResponse(boolean success) {
      Platform.runLater(() -> {
          if (success) {
        	  ShowMessageController.showInfo("Add Success", "Book added successfully.");
        	  manageBooksController.loadBooks();
        	  
          } else {
        	  ShowMessageController.showError("Add Failed", "Cannot Add this Book");
          }
      });
  }

  /**
   * Handles the server response for editing a book's details.
   * 
   * <p>If the update is successful, it displays a success message and refreshes the book list.
   * If the update fails, it shows an error message.</p>
   *
   * @param success A boolean indicating whether the book edit operation was successful.
   */
  private void handleEditBookResponse(boolean success) {
      Platform.runLater(() -> {
          if (success) {
        	  ShowMessageController.showInfo("Edit Success","Book updated successfully.");
              manageBooksController.loadBooks();
          } else {
        	  ShowMessageController.showError("Edit Failed" ,"Failed to update the book.");
          }
      });
  }

//  private void handleDeleteBookResponse(boolean success) {
//      Platform.runLater(() -> {
//          if (success) {
//        	  ShowMessageController.showInfo("Delete Success", "Book Deleted successfully.");
//        	  manageBooksController.loadBooks();
//           } else {
//         	  ShowMessageController.showError("Delete Failed", "Cannot Delete this Book (it has an active borrow record)");
//           }
//      });
//  }
  
  /**
   * Handles the server response for fetching the list of subscribers.
   *
   * <p>This method updates the {@link ManageSubscribersController} with the list of subscribers 
   * received from the server. It ensures the UI is updated on the JavaFX Application Thread.</p>
   *
   * @param subscribers A {@link List} of {@link User} objects representing the subscribers fetched from the server.
   *                    Each {@link User} object contains details about a subscriber, such as their subscription information.
   *                    
   * <p>Usage Example:</p>
   * <pre>
   * List<User> subscribers = fetchSubscribersFromServer(); 
   * handleFetchSubscribersResponse(subscribers);
   * </pre>
   */
  private void handleFetchSubscribersResponse(List<User> subscribers) {
      Platform.runLater(() -> {
          if (manageSubscribersController != null) {
              manageSubscribersController.updateSubscribersList(subscribers);
          }
      });
  }

  /**
   * Handles the server response for registering a new subscriber.
   * 
   * <p>If the registration is successful, it displays a success message and refreshes the subscriber list.
   * If the registration fails, it shows a warning message.</p>
   *
   * @param success A boolean indicating whether the subscriber registration was successful.
   */
  private void handleRegisterSubscriberResponse(boolean success) {
      Platform.runLater(() -> {
          if (success) {
        	  ShowMessageController.showInfo("Success" ,"Subscriber registered successfully.");
              manageSubscribersController.loadSubscribers();
             // sendFetchSubscribersRequest(); // Refresh the subscribers list
          } else {
              ShowMessageController.showWarning("Warning" ,"Failed to register the subscriber.");
          }
      });
  }

  /**
   * Handles the server response for freezing a subscriber's account.
   * 
   * <p>If the operation is successful, it displays a success message and refreshes the subscriber list.
   * If the operation fails (e.g., account is already frozen), it shows an error message.</p>
   *
   * @param success A boolean indicating whether the account freeze operation was successful.
   */
  private void handleFreezeAccountResponse(boolean success) {
      Platform.runLater(() -> {
          if (success) {
        	  ShowMessageController.showInfo("Success" ,"Account frozen successfully.");
              manageSubscribersController.loadSubscribers();
          } else {
        	  ShowMessageController.showError("Account Frozen", "The Account is already Frozen");
          }
      });
  }

  /**
   * Handles the server response for unfreezing a subscriber's account.
   * 
   * <p>If the operation is successful, it displays a success message and refreshes the subscriber list.
   * If the operation fails (e.g., account is already active), it shows an error message.</p>
   *
   * @param success A boolean indicating whether the account unfreeze operation was successful.
   */
  private void handleUnfreezeAccountResponse(boolean success) {
      Platform.runLater(() -> {
          if (success) {
        	  ShowMessageController.showInfo("Success" ,"Account unfrozen successfully.");
              manageSubscribersController.loadSubscribers();
              //sendFetchSubscribersRequest(); // Refresh the subscribers list
          } else {
        	  ShowMessageController.showError("Account Active", "The Account is already Active");
          }
      });
  }
  
//  /**
//   * Handles the renew loan response from the server.
//   *
//   * @param success Indicates whether the renewal was successful.
//   */
//  private void handleRenewLoanResponse(boolean success) {
//      Platform.runLater(() -> {
//          if (subscriberCardController != null) {
//              if (success) {
//                  System.out.println("Borrowing period renewed successfully.");
//                  subscriberCardController.loadSubscriberDetails(); // Refresh subscriber details
//              } else {
//                  System.err.println("Failed to renew borrowing period.");
//              }
//          }
//      });
//  }
  
  /**
   * Handles the server response for a borrow request.
   *
   * <p>This method processes the response from the server regarding a borrow request and updates
   * the {@link BorrowReturnRequestsController} accordingly. Depending on the success or failure
   * of the borrow operation, it logs and displays appropriate details in the UI.</p>
   *
   * @param success A boolean indicating the success or failure of the borrow request.
   *                <ul>
   *                  <li><code>true</code>: The borrow request was successful.</li>
   *                  <li><code>false</code>: The borrow request failed.</li>
   *                </ul>
   * @param msg A {@link String} containing details or a message related to the server's response.
   *            This message can include reasons for failure or additional information about
   *            the successful borrow request.
   *
   * <p><strong>Thread-Safe:</strong> This method ensures that UI updates occur on the JavaFX Application Thread
   * by using {@link Platform#runLater(Runnable)}.</p>
   */
  private void handleBorrowResponse(boolean success, String msg) {
	    Platform.runLater(() -> {
	        if (borrowReturnRequestsController != null) {
	            if (success) {
	                // Log and update the borrow details
	                borrowReturnRequestsController.displayBorrowDetails(msg);
	                borrowReturnRequestsController.appendDetailsMessage("Borrow Request Success: " + msg);
	            } else {
	                // Log and update the details area with an error message
	                borrowReturnRequestsController.appendDetailsMessage("Borrow Request Failed: " + msg);
	            }
	        }
	    });
	}

    /**
     * Handles the server response for a return book request.
     * 
     * <p>If the operation is successful, the return details and a success message are displayed.
     * In case of failure, an error message is logged and displayed in the details area.</p>
     *
     * @param success A boolean indicating whether the return request was successful.
     * @param msg     A message containing details about the return operation, typically provided by the server.
     */
	private void handleReturnResponse(boolean success, String msg) {
	    Platform.runLater(() -> {
	        if (borrowReturnRequestsController != null) {
	            if (success) {
	                // Log and update the return details
	                borrowReturnRequestsController.displayReturnDetails(msg);
	                borrowReturnRequestsController.appendDetailsMessage("Return Request Success: " + msg);
	            } else {
	                // Log and update the details area with an error message
	                borrowReturnRequestsController.appendDetailsMessage("Return Request Failed: " + msg);
	            }
	        }
	    });
	}

	/**
	 * Handles the server response for fetching reservations.
	 * 
	 * <p>Updates the reservations table in the ReservationsController with the data received from the server.
	 * If the ReservationsController is not available, logs a debug message.</p>
	 *
	 * @param reservations A list of {@link Reservation} objects representing the fetched reservations.
	 */  
	public void handleFetchReservationsResponse(List<Reservation> reservations) {
	    Platform.runLater(() -> {
	        if (reservationsController != null) {
	            reservationsController.updateReservations(reservations);
	        } else {
	            System.out.println("ReservationsController is null!"); // Debug
	        }
	    });
	}

  /**
	* Handles the server response for canceling a reservation.
	* 
	* <p>If the cancellation is successful, a success message is displayed.
	* If the operation fails, an error message with details is shown.</p>
	*
	* @param success A boolean indicating whether the cancellation was successful.
	* @param message A message provided by the server, describing the outcome of the cancellation.
	*/
  private void handleCancelReservationResponse(boolean success, String message) {
      Platform.runLater(() -> {
		  if(success) {
			  ShowMessageController.showInfo("Cancel Resrevation Successful", message);
		  }
		  else {
			  ShowMessageController.showError("Error", message);
		  }
      });
  }
  
  /**
   * Handles the response for fetching borrowed books.
   *
   * @param records The list of borrowed books returned by the server.
   */
  private void handleFetchBorrowedBooksResponse(List<BorrowRecord> records) {
      Platform.runLater(() -> {
          if (borrowedBooksController != null) {
              borrowedBooksController.updateBorrowedBooks(records);
          }

          if (subscriberCardController != null) {
        	  subscriberCardController.setUpTable(records);
          }
      });
  }

  /**
   * Handles the response for the request extension action.
   *
   * @param success True if the request was successful, false otherwise.
   * @param message The message returned by the server.
   */
  private void handleExtensionResponse(boolean success, String message) {
      Platform.runLater(() -> {
		  if(success) {
			  ShowMessageController.showInfo("Extension Successful", message);
		  }
		  else {
			  ShowMessageController.showError("Error", message);
		  }
      });
  }
  
  /**
   * Handles the server response for updating a subscriber's details.
   * 
   * <p>If the update is successful, it logs the success message. If the update fails, 
   * it logs the failure message. This method ensures that the update process is properly 
   * tracked and provides feedback to the user.</p>
   *
   * @param success A boolean indicating whether the update operation was successful.
   * @param message A message provided by the server, describing the outcome of the update.
   */
  private void handleUpdateSubscriberDetailsResponse(boolean success, String message) {
      Platform.runLater(() -> {
		  if(success && accountManagementController != null) {
			  ShowMessageController.showInfo("Success", message);
		  }
		  else {
			  accountManagementController.updateAfterChange();
			  ShowMessageController.showError("Update Error", message);
			  
		  }
      });
  }
  
  /**
   * Handles the server response for placing a reservation.
   * 
   * <p>If the reservation is successful, a success message is displayed to the user.
   * If the reservation fails, an error message is displayed with the failure details.</p>
   *
   * @param success A boolean indicating whether the reservation operation was successful.
   * @param message A message provided by the server, describing the outcome of the reservation.
   */
  private void handleRservationResponse(boolean success, String message) {
	  Platform.runLater(() -> {
		  if(success) {
			  ShowMessageController.showInfo("Order Successful", message);
		  }
		  else {
			  ShowMessageController.showError("Error", message);
		  }
	  });
  }
  
  /**
   * Handles the server response for fetching a subscriber's card details.
   * 
   * <p>Updates the {@link SubscriberCardController} with the fetched subscriber details 
   * and refreshes the borrowing history table in the UI. If the controller is not available, 
   * it logs an error message.</p>
   *
   * @param fetchedSubscriber The {@link User} object containing the subscriber's card details.
   */
  private void handleCardResponse(User fetchedSubscriber) {
	    Platform.runLater(() -> {
	        SubscriberCardController controller = ClientControllerNew.getInstance().getChatClient().getSubscriberCardController();
	        if (controller == null) {
	            System.err.println("SubscriberCardController is null. Ensure it's set after loading the FXML.");
	            return;
	        }

	        // Update user and UI
	        user1 = fetchedSubscriber;
	        controller.loadSubscriberDetails();
	        controller.setUpTable(fetchedSubscriber.getSubscriber().getBorrowingHistory());
	    });
	}
  
  /**
   * Handles the server response for adding a usage issue related to a subscriber's account.
   * 
   * <p>If the operation is successful, a success message is displayed to the user. 
   * If the operation fails, an error message is displayed with the failure details.</p>
   *
   * @param success A boolean indicating whether the usage issue addition was successful.
   * @param message A message provided by the server, describing the outcome of the operation.
   */
  private void handleAddUsageIssueResponse(boolean success, String message) {
	    Platform.runLater(() -> {
	        if (subscriberCardController != null) {
	            if (success) {
	            	ShowMessageController.showInfo("Success", message);
	            } else {
	            	ShowMessageController.showError("Error", message);
	            }
	        }
	    });
	}

    /**
     * Handles the server response for resolving a usage issue related to a subscriber's account.
     * 
     * <p>If the operation is successful, a success message is displayed to the user. 
     * If the operation fails, an error message is displayed with the failure details.</p>
     *
     * @param success A boolean indicating whether the usage issue resolution was successful.
     * @param message A message provided by the server, describing the outcome of the operation.
     */
	private void handleResolveUsageIssueResponse(boolean success, String message) {
	    Platform.runLater(() -> {
	        if (subscriberCardController != null) {
	            if (success) {
	            	ShowMessageController.showInfo("Success", message);
	            } else {
	            	ShowMessageController.showError("Error", message);
	            }
	        }
	    });
	}



  
/**
   * Logs out the currently logged-in user.
   */
  public void logoutUser() {
      try {

          // Clear the logged-in user
          loggedInUser = null;

          // Disconnect from the server
          closeConnection();

          System.out.println("User logged out successfully.");
      } catch (IOException e) {
          System.err.println("Failed to log out: " + e.getMessage());
      }
  }


  /**
   * Retrieves the logged-in user.
   *
   * @return The currently logged-in {@link User}.
   */
  public User getLoggedInUser() {
      return loggedInUser;
  }
  
  private SubscriberCardController getSubscriberCardController() {
	
	return subscriberCardController;
}
  
  
  /**
   * Sets the {@link LoginController} instance.
   *
   * @param loginController The LoginController to set.
   */
  public void setLoginController(LoginController loginController) {
      this.loginController = loginController;
  }

  /**
   * Sets the SearchController instance.
   */
  public void setSearchController(SearchController searchController) {
      this.searchController = searchController;
  }
  
  public void setManageBooksController(ManageBooksController manageBooksController) {
      this.manageBooksController = manageBooksController;
  }
  
  public void setManageSubscribersController(ManageSubscribersController manageSubscribersController) {
      this.manageSubscribersController = manageSubscribersController;
  }
  
  /**
   * Sets the SubscriberCardController instance.
   */
  public void setSubscriberCardController(SubscriberCardController subscriberCardController) {
      this.subscriberCardController = subscriberCardController;
  }
  
  /**
   * Sets the SubscriberBorrowedBooksController instance.
   */
  public void setBorrowedBooksController(SubscriberBorrowedBooksController borrowedBooksController) {
      this.borrowedBooksController = borrowedBooksController;
  }
  
  /**
   * Sets the SubscriberBorrowedBooksController instance.
   */
  public void setReservationsController(ReservationsController reservationsController) {
      this.reservationsController = reservationsController;
  }
  
  public void setAccountManagementController(AccountManagementController accountManagementController) {
	  this.accountManagementController = accountManagementController;
  }
  
  /**
   * Sets the SubscriberDashboardController instance.
   */
  public void setSubscriberDashboardController(SubscriberDashboardController subscriberDashboardController) {
      this.subscriberDashboardController = subscriberDashboardController;
  }
  
  /**
   * Sets the SubscriberDashboardController instance.
   */
  public void setLibrarianDashboardController(LibrarianDashboardController librarianDashboardController) {
      this.librarianDashboardController = librarianDashboardController;
  }
  
  public void setBorrowReturnRequestsController(BorrowReturnRequestsController borrowReturnRequestsController) {
	  this.borrowReturnRequestsController = borrowReturnRequestsController;
  }
  
  public void setReportsController(ReportsController reportsController) {
	  this.reportsController = reportsController;
  }
  
  
  /**
   * Handles the server's response to a connection check request.
   *
   * <p>This method processes the result of a connection check, indicating whether
   * the client was able to successfully connect to the server. Depending on the result,
   * it updates the client UI with an appropriate message.</p>
   *
   * @param isConnected A boolean indicating the connection status:
   *                    <ul>
   *                        <li><code>true</code>: Connection to the server was successful.</li>
   *                        <li><code>false</code>: Connection to the server failed.</li>
   *                    </ul>
   *
   * <p><strong>UI Interaction:</strong> Displays a message in the client UI to inform the user
   * of the connection status.</p>
   */
  private void handleCheckConnectionResponse(boolean isConnected) {
      if (isConnected) {
          clientUI.display("Connected to the server successfully!");
      } else {
          System.err.println("Failed to connect to the server.");
          clientUI.display("Failed to connect to the server. Please try again.");
      }
  }
  
  
  
  /**
   * Handles the logout request and terminates the client session.
   */
  public void quit()
  {
    try
    {
      closeConnection();
    }
    catch(IOException e) {}
    System.exit(0);
  }
}
//End of ChatClient class











    
   



