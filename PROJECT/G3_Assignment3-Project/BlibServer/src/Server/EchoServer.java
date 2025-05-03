package Server;
import java.io.*;
import java.net.InetAddress;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;

import enums.ClientAction;
import enums.ServerResponse;
import gui.ClientInfo;
import gui.ServerPortFrameController;
import javafx.application.Platform;
import models.Books;
import models.BorrowRecord;
import models.Messages;
import models.Reports;
import models.Reservation;
import models.Subscriber;
import models.User;
import ocsf.server.*;

/**
 * The {@code EchoServer} class extends the {@link AbstractServer} class to implement 
 * a library system server. It acts as the central point for handling client requests, 
 * managing connections, and interacting with the database.
 * 
 * <p>This server supports a wide range of operations, including:</p>
 * <ul>
 *   <li>User authentication</li>
 *   <li>Book management (search, add, edit, delete)</li>
 *   <li>Subscriber management (register, update, freeze, unfreeze)</li>
 *   <li>Borrowing and returning books</li>
 *   <li>Managing reservations</li>
 *   <li>Generating reports</li>
 *   <li>Communicating with clients</li>
 * </ul>
 * 
 * <p>The server integrates with the {@code dbControl} class for database interactions 
 * and uses the {@link gui.ClientInfo} and {@link gui.ServerPortFrameController} classes 
 * to manage client data and update the user interface.</p>
 * 
 * <h2>Usage</h2>
 * <p>The server is initialized with a specific port and starts listening for connections 
 * once {@code serverStarted()} is called.</p>
 * 
 * <h2>Dependencies</h2>
 * <p>Ensure the database is properly configured and accessible before starting the server.</p>
 * 
 * <p>This file is based on section 3.7 of the textbook 
 * "Object Oriented Software Engineering" and is issued under the 
 * open-source license found at www.lloseng.com.</p>
 *
 * @version July 2000
 */

public class EchoServer extends AbstractServer 
{
	
	// Class variables *************************************************
	  
    /**
     * The default port to listen on.
     */
    public static final int DEFAULT_PORT = 5555;

    /**
     * A list of currently connected clients. Each client is represented by a {@link ClientInfo} object,
     * which includes details like client ID, IP address, and connection status.
     */
    private static final ArrayList<ClientInfo> connectedClients = new ArrayList<>();

    
    // Constructors ****************************************************
    /**
     * Constructs an instance of the EchoServer.
     *
     * @param port The port number to connect on.
     */
    public EchoServer(int port) {
        super(port);
    }
   
    // Class Methods ***************************************************

    /**
     * Gets the list of connected clients.
     * 
     * @return A list of ClientInfo objects representing connected clients.
     */    
    public static ArrayList<ClientInfo> getConnectedClients() {
        return connectedClients;
    }
    
    
    /**
     * Called when a client connects to the server.
     *
     * @param client The connection to the client.
     */
    @Override
    protected void clientConnected(ConnectionToClient client) {
        synchronized (connectedClients) {
            connectedClients.add(new ClientInfo(client.getId(), client.getInetAddress().getHostAddress()));
        }
        updateClientsInUI();
    }
    
    /**
     * Called when a client disconnects from the server.
     *
     * @param client The connection to the client.
     */
    @Override
    protected void clientDisconnected(ConnectionToClient client) {
        synchronized (connectedClients) {
            for (ClientInfo info : connectedClients) {
                if (info.getClientId() == client.getId()) {
                    info.setStatus("Disconnected"); // Update the status property
                    break;
                }
            }
        }
        updateClientsInUI();
    }
    
    /**
     * Updates the list of clients in the user interface.
     */
    private void updateClientsInUI() {
        Platform.runLater(() -> {
            if (ServerPortFrameController.getInstance() != null) {
                ServerPortFrameController.getInstance().updateClientsList(connectedClients);
            }
        });
    }
    
    
    
    
    /**
     * Handles messages received from clients. This method processes requests 
     * from the client, performs corresponding database operations, and sends 
     * appropriate responses based on the client's actions.
     *
     * <p>Supported client actions include:</p>
     * <ul>
     *   <li><b>CHECK_CONNECTION:</b> Verifies the connection between the client and the server.</li>
     *   <li><b>LOGIN_REQUEST:</b> Authenticates a user using their ID and password.</li>
     *   <li><b>FETCH_BOOKS:</b> Retrieves a list of all books available in the library.</li>
     *   <li><b>ADD_BOOK:</b> Adds a new book to the library database.</li>
     *   <li><b>EDIT_BOOK:</b> Updates the number of copies available for a specific book.</li>
     *   <li><b>DELETE_BOOK:</b> Removes a book from the library database.</li>
     *   <li><b>REGISTER_SUBSCRIBER:</b> Registers a new subscriber in the library system.</li>
     *   <li><b>SEARCH_BOOKS:</b> Retrieves search results for books based on specified criteria.</li>
     *   <li><b>FETCH_SUBSCRIBERS:</b> Retrieves a list of all subscribers.</li>
     *   <li><b>FREEZE_ACCOUNT:</b> Freezes a subscriber's account.</li>
     *   <li><b>UNFREEZE_ACCOUNT:</b> Reactivates a subscriber's frozen account.</li>
     *   <li><b>UPDATE_SUBSCRIBER_DETAILS:</b> Updates a subscriber's contact details.</li>
     *   <li><b>BORROW_BOOK:</b> Processes a borrow request for a book by a subscriber.</li>
     *   <li><b>FETCH_BORROWED_BOOKS:</b> Retrieves a list of books currently borrowed by a subscriber.</li>
     *   <li><b>GET_CARD:</b> Retrieves card details for a subscriber.</li>
     *   <li><b>RESERVE_BOOK:</b> Reserves a book for a subscriber.</li>
     *   <li><b>FETCH_RESERVATIONS:</b> Retrieves a list of reservations for a subscriber.</li>
     *   <li><b>RETURN_REQUEST:</b> Processes a request to return a borrowed book.</li>
     *   <li><b>CANCEL_RESERVATION:</b> Cancels a reservation for a book.</li>
     *   <li><b>REQUEST_EXTENSION1:</b> Requests an extension for a borrowed book's due date.</li>
     *   <li><b>ADD_USAGE_ISSUE:</b> Reports a lost or damaged book issue.</li>
     *   <li><b>RESOLVE_USAGE_ISSUE:</b> Resolves a reported book issue.</li>
     *   <li><b>FETCH_MESSAGES:</b> Retrieves messages associated with a user.</li>
     *   <li><b>FETCH_REPORT:</b> Generates and retrieves reports based on specified criteria.</li>
     *   <li><b>LOGOUT_REQUEST:</b> Logs out the user and disconnects their session.</li>
     * </ul>
     *
     * <p>If the request format is invalid or an unrecognized action is received, the method 
     * logs an error and does not process the request further.</p>
     *
     * @param msg The message received from the client. This is expected to be an array of objects 
     *            containing the action type (as {@link ClientAction}) followed by any necessary parameters.
     * @param client The {@link ConnectionToClient} object representing the client that sent the message.
     * @throws IOException If an error occurs while sending a response to the client.
     */
    public void handleMessageFromClient(Object msg, ConnectionToClient client) {
//	  	System.out.println("got message from : "+client);
//	  	System.out.println("Client has sent: " + java.util.Arrays.toString((Object[]) msg));
	    String toBeSent;
		String currentIp;
		boolean response;
		User user;
		Subscriber sub;
		ArrayList<Books> books;
		ArrayList<User> Subscribers;
		ArrayList<Object> result;
		ArrayList<BorrowRecord> records;
		ArrayList<Reservation> reservations;
		ArrayList<Messages> messages;
		ArrayList<Reports> reports;
	    try {
	        // Check if the message is an ArrayList
	    		if (!(msg instanceof Object[])) { 
	    			System.err.println("Invalid message format received from client."); 
	    			return;  
	    		}
	    		Object[] request = (Object[]) msg; 
	    		ClientAction action = (ClientAction) request[0];
	            // Switch based on the enum
	            switch (action) {
	            	case CHECK_CONNECTION:
	            		System.out.println("Client " + client + " Connected Successfully");
	            		client.sendToClient(new Object[] {ServerResponse.CHECK_CONNECTION_RESPONSE,true});
	            		break;
	                case LOGIN_REQUEST: 
	                	user = dbControl.fetchUserDetails((int)request[1],(String)request[2]);
	                	if (user == null) {
	                		System.out.println(client+ " has failed to log in");
	                		response = false;
	                	}
	                	else {
	                		System.out.println(client +" has logged in as user with id:" + (int)request[1]);
	                		response = true;
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.LOGIN_RESPONSE,response,user});
	                	break;
	                case FETCH_BOOKS:
	           		 	books = dbControl.fetchAllBooks();
    	                client.sendToClient(new Object[] {ServerResponse.FETCH_BOOKS_RESPONSE,books});
	                	break;
	                case ADD_BOOK:
	                	response = dbControl.addBook((Books)request[1]) ;
	                	if (response) {
	                		System.out.println(client + " has added a new book to the library");
	                	}
	                	else {
	                		System.out.println(client + " has failed to add a new book to the library");
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.ADD_BOOK_RESPONSE, response});
    	                break;
	                case EDIT_BOOK:
	                	response = dbControl.updateBookCopies((int)request[1],(int)request[2]);
	                	if (response) {
	                		System.out.println(client + "has edited the number of copies for book with id " + (int)request[2]);
	                	}
	                	else {
	                		System.out.println(client + "has failed to edit the number of copies for book with id " + (int)request[2]);
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.EDIT_BOOK_RESPONSE, response});
	                	break;
//	                case DELETE_BOOK:
//	                	response = dbControl.deleteBook((int)request[1]);
//	                	if (response) {
//	                		System.out.println(client + "has deleted book with id " + (int)request[2]);
//	                	}
//	                	else {
//	                		System.out.println(client + "has failed to delet book with id " + (int)request[2]);
//	                	}
//    	                client.sendToClient(new Object[] {ServerResponse.DELETE_BOOK_RESPONSE, response});
//	                	break;
	                case REGISTER_SUBSCRIBER:
	                    ArrayList<Object> list = (ArrayList<Object>) request[1];
	                    sub = new Subscriber((int)list.get(0), null, (String)list.get(3), (String)list.get(4), (String)list.get(5));
	                	response = dbControl.RegisterNewSub(sub,(String)list.get(2),(String)list.get(1));
	                	if (response) {
	                		System.out.println(client + "has Registered a new sub with id " + (int)list.get(0));
	                	}
	                	else {
	                		System.out.println(client + "has faild to register a new sub with id " + (int)list.get(0));
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.REGISTER_SUBSCRIBER_RESPONSE, response});
	                    break;
	                case SEARCH_BOOKS: 
	           		 	books = dbControl.searchBooks((String)request[1]);
    	                client.sendToClient(new Object[] {ServerResponse.SEARCH_RESULTS,books});
	                    break;
	                case FETCH_SUBSCRIBERS:
	                	Subscribers = dbControl.getAllSubscribers();
    	                client.sendToClient(new Object[] {ServerResponse.FETCH_SUBSCRIBERS_RESPONSE,Subscribers});
	                	break;
	                case FREEZE_ACCOUNT:
	                	response = dbControl.freezeSubscriber((int)request[1]);
	                	if (response) {
	                		System.out.println(client + "request to freeze sub with id " + (int)request[1] + " was succesfull" );
	                	}
	                	else {
	                		System.out.println(client + "request to freeze sub with id " + (int)request[1] + " has failed" );
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.FREEZE_ACCOUNT_RESPONSE, response});
	                	break;
	                case UNFREEZE_ACCOUNT:
	                	response = dbControl.activateSubscriber((int)request[1]);
	                	if (response) {
	                		System.out.println(client + "request to Unfreeze sub with id " + (int)request[1] + " was succesfull" );
	                	}
	                	else {
	                		System.out.println(client + "request to Unfreeze sub with id " + (int)request[1] + " has failed" );
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.UNFREEZE_ACCOUNT_RESPONSE, response});
	                	break;
	                case UPDATE_SUBSCRIBER_DETAILS:
	           		    result = dbControl.updateSubscriberContactDetails((int)request[1],(String)request[2],(String)request[3]);
	           		    response = (boolean) result.get(0);
	           		    toBeSent = (String) result.get(1) ;
	           		    if (response) {
	                		System.out.println(client + "request to update the details of sub with id " + (int)request[1] + " was succesfull" );
	                	}
	                	else {
	                		System.out.println(client + "request to update the details of sub with id " + (int)request[1] + " has failed" );
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.UPDATE_SUBSCRIBER_DETAILS_RESPONSE, response,toBeSent});
	                	break;
	                case BORROW_BOOK:
	                	result = dbControl.startBorrowingProcess((String)request[1],(String)request[2]);
	           		    response = (boolean) result.get(0);
	           		    toBeSent = (String) result.get(1) ;
	           		    if (response) {
	                		System.out.println(client + "request to borrow book with barcode "+(String)request[2] +" as sub with subscrption number "+ (String)request[1] + " was succesfull" );
	                	}
	                	else {
	                		System.out.println(client + "request to borrow book with barcode "+(String)request[2] +" as sub with subscrption number "+ (String)request[1] + " has failed" );
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.BORROW_BOOK_RESPONSE, response,toBeSent});
	                	break;
	                case  FETCH_BORROWED_BOOKS:
	                	records = dbControl.fetchBorrowRecordsBySubscriberId((int)request[1]);
    	                client.sendToClient(new Object[] {ServerResponse.FETCH_BORROWED_BOOKS_RESPONSE,records});
    	                break;
	                case GET_CARD: 
	                	user = dbControl.fetchUserDetails((int)request[1],(String)request[2]);
    	                client.sendToClient(new Object[] {ServerResponse.GET_CARD_RESPONSE, user});
	                	break;
	                case RESERVE_BOOK:
	                	result = dbControl.addReservation((int)request[1], (int)request[2]);
	                	response = (boolean) result.get(0);
	           		    toBeSent = (String) result.get(1) ;
	           		    if (response) {
	                		System.out.println(client + "request to reserve book with barcode "+(int)request[1] +" as sub with id "+ (int)request[2] + " was succesfull" );
	                	}
	                	else {
	                		System.out.println(client + "request to reserve book with barcode "+(int)request[1] +" as sub with id "+ (int)request[2] + " has failed" );
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.RESERVATION_RESPONSE,response,toBeSent});
    	                break;
	                case FETCH_RESERVATIONS:
	                	reservations = dbControl.fetchAllReservationsForSubscriber((int)request[1]);
    	                client.sendToClient(new Object[] {ServerResponse.FETCH_RESERVATIONS_RESPONSE,reservations});
	                	break;
	                case RETURN_REQUEST:
	                	result = dbControl.returnBook((String)request[1],(String)request[2]);
	                	response = (boolean) result.get(0);
	           		    toBeSent = (String) result.get(1) ;
	           		    if (response) {
	                		System.out.println(client + "request to return book with barcode "+(String)request[2] +" as sub with subscrption number "+ (String)request[1] + " was succesfull" );
	                	}
	                	else {
	                		System.out.println(client + "request to return book with barcode "+(String)request[2] +" as sub with subscrption number "+ (String)request[1] + " has failed" );
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.RETURN_BOOK_RESPONSE,response,toBeSent});
	                	break;
	                case CANCEL_RESERVATION:
	                	result = dbControl.cancelReservation((int)request[1]);
	                	response = (boolean) result.get(0);
	           		    toBeSent = (String) result.get(1) ;
	           		    if (response) {
	                		System.out.println(client + "request to cancel reservation with id " + (int)request[1] + " was succesfull" );
	                	}
	                	else {
	                		System.out.println(client + "request to cancel reservation with id " + (int)request[1] + " has failed" );
	                	}
    	                client.sendToClient(new Object[] {ServerResponse.CANCEL_RESERVATION_RESPONSE,response,toBeSent});
    	                break;
	                case REQUEST_EXTENSION1:
	                    try {
	                        // Parse request[3] (which is a string) to LocalDate
	                        String dueDateString = (String) request[3];
	                        LocalDate newDueDate = LocalDate.parse(dueDateString); // Use default ISO-8601 format
	                        // Call the method with the parsed LocalDate
	                        result = dbControl.extendBorrowingTime((int) request[1], (String) request[2], newDueDate);
	                        response = (boolean) result.get(0);
	                        toBeSent = (String) result.get(1);
	                        
	                        if (response) {
		                		System.out.println(client + "request to extend borrow with id " + (int)request[1] +"by "+ (String)request[2] +" was succesfull" );
		                	}
		                	else {
		                		System.out.println(client + "request to extend borrow with id " + (int)request[1] +"by "+ (String)request[2] + " has failed" );
		                	}	                        client.sendToClient(new Object[]{ServerResponse.REQUEST_EXTENSION_RESPONSE, response, toBeSent});
	                    } catch (DateTimeParseException e) {
	                        System.err.println("Error parsing dueDate: " + request[3]);
	                        client.sendToClient(new Object[]{ServerResponse.REQUEST_EXTENSION_RESPONSE, false, "Invalid date format."});
	                    }
	                    break;
	                case ADD_USAGE_ISSUE:
	                	response = dbControl.reportLostBook((int)request[1]);
	                	if (response) {
	                		System.out.println(client + "request to add a new issue for borrow with id " + (int)request[1] +" was succesfull" );
	                	}
	                	else {
	                		System.out.println(client + "request to add a new issue for borrow with id " + (int)request[1] +" has failed" );
	                	}	   
                        client.sendToClient(new Object[]{ServerResponse.ADD_USAGE_ISSUE_RESPONSE, response});
	                	break;
	                case RESOLVE_USAGE_ISSUE:
	                	response = dbControl.resolveIssue((int)request[1]);
	                	if (response) {
	                		System.out.println(client + "request to resolve issue with id " + (int)request[1] +" was succesfull" );
	                	}
	                	else {
	                		System.out.println(client + "request to resolve issue with id " + (int)request[1] +" has failed" );
	                	}	
                        client.sendToClient(new Object[]{ServerResponse.RESOLVE_USAGE_ISSUE_RESPONSE, response});
	                	break;
	                case FETCH_MESSAGES:
	                	messages = dbControl.fetchMessagesByUserId((int)request[1]);
	                	client.sendToClient(new Object[]{ServerResponse.FETCH_MESSAGES_RESPONSE, messages});
	                	break;
	                case FETCH_REPORT:
//	                	System.out.println("Server enter the case");
//	                	System.out.println(request[2].getClass().getName());
	                	String ReportDate = ((LocalDate) request[2]).toString();
//	                	System.out.println(ReportDate.getClass().getName());
                        LocalDate get = LocalDate.parse(ReportDate); // Use default ISO-8601 format
//                        System.out.println(get);
	                	reports = dbControl.fetchReports((String)request[1], get);
//	                	System.out.println("system passed the fetch");
//	                	System.out.println("Report Type: " + request[1]);
//	                	System.out.println("Report Month: " + request[2]);
//	                	System.out.println(reports);
	                	if (reports == null || reports.isEmpty()) {
	                	    System.out.println("No reports found for the specified type and month.");
	                	    client.sendToClient(new Object[]{ServerResponse.FETCH_REPORT_RESPONSE, null});
	                	} else {
	                		System.out.println("reprots for this month have been generated");
	                	    client.sendToClient(new Object[]{ServerResponse.FETCH_REPORT_RESPONSE, reports});
	                	}
	                	break;
	                case LOGOUT_REQUEST:
	                	System.out.println("Client "+ client + "has Disconnected");
	                	clientDisconnected(client);
                	    client.sendToClient(new Object[]{ServerResponse.LOGOUT_REQUEST_RESPONSE});
	                break;
	                default: 
	                	System.out.println("In the default");
	            }
	        
	    } catch (IOException e) {
	        e.printStackTrace();
	        System.out.println("Error while sending a response to the server.");
	    }

	}

    
  /**
   * Called when the server starts listening for connections.
   */  
   
  protected void serverStarted()
  {
    System.out.println ("Server listening for connections on port " + getPort());
  }
  
  
  /**
   * Called when the server stops listening for connections.
   */
  protected void serverStopped()  {
    System.out.println ("Server has stopped listening for connections.");
  }  
}

//End of EchoServer class
