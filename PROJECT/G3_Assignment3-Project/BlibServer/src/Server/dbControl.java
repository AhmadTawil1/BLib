package Server;

import java.sql.Connection;
import java.sql.Date;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;

import javax.net.ssl.SSLException;

import models.Books;
import models.BorrowRecord;
import models.Issues;
import models.Messages;
import models.Reports;
import models.Reservation;
import models.Subscriber;
import models.User;
 
/**
 * The {@code dbControl} class provides methods for managing the interaction 
 * between the application and a MySQL database.
 * 
 * <p>This class offers functionality to perform various operations such as:</p>
 * <ul>
 *   <li>Managing database connections (connect, disconnect)</li>
 *   <li>CRUD operations on users, subscribers, books, and reservations</li>
 *   <li>Handling borrowing and returning processes</li>
 *   <li>Managing issues related to lost books and account freezing</li>
 *   <li>Generating and fetching reports</li>
 *   <li>Sending and retrieving messages for users</li>
 * </ul>
 * 
 * <h2>Usage</h2>
 * <p>To use this class, first establish a connection using the {@link #connect(String, String, String, String, String)} 
 * method. After completing database operations, disconnect using the {@link #disconnect()} method to free resources.</p>
 * 
 * <h2>Thread Safety</h2>
 * <p>This class is not thread-safe. If multiple threads are expected to interact with the database simultaneously, 
 * ensure external synchronization or refactor the code to handle thread safety.</p>
 * 
 * <h2>Example</h2>
 * <pre>{@code
 * // Establish a connection
 * dbControl.connect("localhost", "3306", "library", "root", "password");
 * 
 * // Fetch all books
 * ArrayList<Books> books = dbControl.fetchAllBooks();
 * 
 * // Close the connection
 * dbControl.disconnect();
 * }</pre>
 * 
 * @version 1.0
 * @since 2025-01-26
 */
public class dbControl {
	private static Connection connection;

	/**
	 * Establishes a connection to the MySQL database.
	 * 
	 * @param serverIp The IP address of the database server.
	 * @param port The port number of the database server.
	 * @param dbName The name of the database to connect to.
	 * @param user The username for the database.
	 * @param password The password for the database.
	 * @return A {@link Connection} object representing the database connection.
	 */
	public static Connection connect(String serverIp, String port, String dbName, String user, String password) {
        String url = "jdbc:mysql://" + serverIp + ":" + port + "/" + dbName + "?serverTimezone=Asia/Jerusalem";
        try 
		{
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
         //   System.out.println("Driver definition succeed");
        } catch (Exception ex) {
        	/* handle the error*/
        	 System.out.println("Driver definition failed");
        	 }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            connection = DriverManager.getConnection(url, user, password);
            System.out.println("connected to data base");
           // createTableSubscriber(connection);
        } catch (Exception ex) {
            System.out.println("Connection failed: " + ex.getMessage());
            ex.printStackTrace();
        }
        return connection;
    }

    
	/**
	 * Closes the database connection gracefully.
	 *
	 * @throws SSLException If an error occurs during the disconnection process.
	 */
	public static void disconnect() throws SSLException {
	    if (connection != null) {
	        try {
	            // Gracefully close the connection
	            if (!connection.isClosed()) {
	                connection.close();
	                System.out.println("Disconnected from the database successfully.");
	            }
	        } catch (SQLException sqlEx) {
	            // Handle SQL exceptions
	            System.err.println("Error during disconnect: " + sqlEx.getMessage());
	            sqlEx.printStackTrace();
	        } finally {
	            // Ensure the connection object is set to null
	            connection = null;
	        }
	    }
	}


	/**
	 * Fetches user details from the database based on the provided ID and password.
	 * 
	 * <p>If the user's role is "Subscriber," additional subscriber details are fetched.</p>
	 *
	 * @param id The ID of the user.
	 * @param inputPassword The password of the user.
	 * @return A {@link User} object containing the user's details, or {@code null} if no matching user is found.
	 */
    
    public static User fetchUserDetails(int id, String inputPassword) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String sql = "SELECT u.userId, u.username, u.userPassword, u.role, " +
                         "s.subscriptionNumber, s.email, s.mobilePhoneNumber, s.accountStatus " +
                         "FROM Users u " +
                         "LEFT JOIN Subscribers s ON u.userId = s.subscriberId " +
                         "WHERE u.userId = ? AND u.userPassword = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, id);
            stmt.setString(2, inputPassword);

            rs = stmt.executeQuery();

            if (rs.next()) {
                String role = rs.getString("role");
                Subscriber subscriber = null;

	            if ("Subscriber".equalsIgnoreCase(role)) {
	             // Fetch subscriber details if the role is Subscriber
	             subscriber = fetchSubscriberDetails(id);
	            }

                return new User(
                    rs.getInt("userId"),
                    rs.getString("username"), // Populate username field
                    rs.getString("userPassword"),
                    role,
                    subscriber
                );
            } else {
                System.out.println("No user found with ID: " + id + " and provided password.");
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Fetches detailed subscriber information for a given subscriber ID.
     *
     * @param subscriberId The ID of the subscriber.
     * @return A {@link Subscriber} object containing the subscriber's details, or {@code null} if no subscriber is found.
     */

    public static Subscriber fetchSubscriberDetails(int subscriberId) {
        PreparedStatement subscriberStmt = null;
        ResultSet subscriberRs = null;

        try {
            // Query to fetch subscriber details
            String subscriberSql = "SELECT u.username, s.subscriberId, s.subscriptionNumber, s.email, s.mobilePhoneNumber, s.accountStatus " +
                                   "FROM Subscribers s " +
                                   "JOIN Users u ON s.subscriberId = u.userId " +
                                   "WHERE s.subscriberId = ?";
            subscriberStmt = connection.prepareStatement(subscriberSql);
            subscriberStmt.setInt(1, subscriberId);

            subscriberRs = subscriberStmt.executeQuery();

            if (subscriberRs.next()) {
                // Create the Subscriber object
                Subscriber subscriber = new Subscriber(
                    subscriberRs.getInt("subscriberId"),
                    subscriberRs.getString("subscriptionNumber"),
                    subscriberRs.getString("email"),
                    subscriberRs.getString("mobilePhoneNumber"),
                    subscriberRs.getString("accountStatus")
                );

                // Fetch the borrow records and populate the borrowing history
                ArrayList<BorrowRecord> borrowRecords = fetchBorrowRecordsBySubscriberId(subscriberId);
                for (BorrowRecord record : borrowRecords) {
                    subscriber.addBorrowingRecord(record);
                }

                // Fetch the usage issues using fetchIssuesBySubscriber method
                ArrayList<Issues> usageIssues = fetchIssuesBySubscriber(subscriberId);
                for (Issues issue : usageIssues) {
                    subscriber.addUsageIssue(issue);
                }

                return subscriber;
            } else {
                System.out.println("No subscriber found with ID: " + subscriberId);
                return null;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        } finally {
            try {
                if (subscriberRs != null) subscriberRs.close();
                if (subscriberStmt != null) subscriberStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    
    /**
     * Searches for books in the database that match the given search string.
     * 
     * <p>The search is performed on the book's title, subject, or description.</p>
     *
     * @param searchString The search string used to find matching books.
     * @return An {@link ArrayList} of {@link Books} objects representing the matching books.
     */
    public static ArrayList<Books> searchBooks(String searchString) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<Books> matchingBooks = new ArrayList<>();

        try {
            String sql = "SELECT bookId FROM Books WHERE title LIKE ? OR subject LIKE ? OR description LIKE ?";
            stmt = connection.prepareStatement(sql);
            String searchPattern = "%" + searchString + "%";
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            stmt.setString(3, searchPattern);

            rs = stmt.executeQuery();

            while (rs.next()) {
                int bookId = rs.getInt("bookId");
                Books book = fetchBookDetails(bookId); // Use fetchBookDetails method

                if (book != null) {
                    matchingBooks.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return matchingBooks;
    }

    
    /**
     * Retrieves all books from the database.
     * 
     * @return An {@link ArrayList} of {@link Books} objects representing all books in the database.
     */
    public static ArrayList<Books> fetchAllBooks() {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<Books> allBooks = new ArrayList<>();

        try {
            String sql = "SELECT bookId FROM Books"; // Fetch all book IDs
            stmt = connection.prepareStatement(sql);

            rs = stmt.executeQuery();

            while (rs.next()) {
                int bookId = rs.getInt("bookId");
                Books book = fetchBookDetails(bookId); // Use fetchBookDetails to get full details

                if (book != null) {
                    allBooks.add(book);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return allBooks;
    }

    /**
     * Fetches detailed information for a specific book based on its ID.
     * 
     * @param bookId The ID of the book to fetch.
     * @return A {@link Books} object containing the book's details, or {@code null} if no matching book is found.
     */
    public static Books fetchBookDetails(int bookId) {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Query to fetch book details
            String sql = "SELECT * FROM Books WHERE bookId = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, bookId);

            rs = stmt.executeQuery();

            if (rs.next()) {
                // Create and return the Books object using the constructor
                Books book = new Books(
                    rs.getInt("bookId"),
                    rs.getString("title"),
                    rs.getString("author"),
                    rs.getString("subject"),
                    rs.getString("description"),
                    rs.getString("location"),
                    rs.getInt("availableCopies"),
                    rs.getString("barcode")
                );

                // Set nearestReturnDate if available
                Date nearestReturnDate = rs.getDate("nearestReturnDate");
                if (nearestReturnDate != null) {
                    book.setNearestReturnDate(nearestReturnDate.toLocalDate());
                }

                return book; // Return the populated Books object
            } else {
                System.out.println("No book found with ID: " + bookId);
                return null; // No match found
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return null; // Return null on error
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Registers a new subscriber in the database.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *   <li>Checks if the subscriber already exists based on the provided ID, email, or phone number.</li>
     *   <li>Inserts the subscriber's details into the {@code Users} and {@code Subscribers} tables if no conflicts are found.</li>
     * </ul>
     *
     * @param subscriber A {@link Subscriber} object containing the subscriber's details.
     * @param password The password for the new subscriber.
     * @param username The username for the new subscriber.
     * @return {@code true} if the subscriber was successfully registered; {@code false} otherwise.
     */
    public static boolean RegisterNewSub(Subscriber subscriber, String password, String username) {
        PreparedStatement checkStmt = null;
        PreparedStatement insertUserStmt = null;
        PreparedStatement insertSubscriberStmt = null;
        ResultSet rs = null;

        try {
            // Step 1: Check for existing user or conflicts in Users and Subscribers
            String checkSql = "SELECT u.userId, u.username, s.email, s.mobilePhoneNumber " +
                              "FROM Users u " +
                              "LEFT JOIN Subscribers s ON u.userId = s.subscriberId " +
                              "WHERE u.userId = ? OR u.username = ? OR s.email = ? OR s.mobilePhoneNumber = ?";
            checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, subscriber.getSubscriberId());
            checkStmt.setString(2, username);
            checkStmt.setString(3, subscriber.getEmail());
            checkStmt.setString(4, subscriber.getMobilePhoneNumber());

            rs = checkStmt.executeQuery();

            while (rs.next()) {
                int userId = rs.getInt("userId");
                String existingEmail = rs.getString("email");
                String existingPhone = rs.getString("mobilePhoneNumber");

                if (userId == subscriber.getSubscriberId()) {
                    System.out.println("User with ID " + subscriber.getSubscriberId() + " already exists.");
                    return false;
                }   
                if (subscriber.getEmail().equalsIgnoreCase(existingEmail)) {
                    System.out.println("Email " + subscriber.getEmail() + " is already in use.");
                    return false;
                }
                if (subscriber.getMobilePhoneNumber().equals(existingPhone)) {
                    System.out.println("Phone number " + subscriber.getMobilePhoneNumber() + " is already in use.");
                    return false;
                }
            }

            // Step 2: Add the user to the Users table
            String insertUserSql = "INSERT INTO Users (userId, username, userPassword, role) VALUES (?, ?, ?, ?)";
            insertUserStmt = connection.prepareStatement(insertUserSql);
            insertUserStmt.setInt(1, subscriber.getSubscriberId());
            insertUserStmt.setString(2, username);
            insertUserStmt.setString(3, password);
            insertUserStmt.setString(4, "Subscriber");

            int userRowsInserted = insertUserStmt.executeUpdate();

            if (userRowsInserted > 0) {
                System.out.println("User with ID " + subscriber.getSubscriberId() + " added to Users table.");
            } else {
                System.out.println("Failed to add user to Users table.");
                return false;
            }

            // Step 3: Add the subscriber details to the Subscribers table
            String insertSubscriberSql = "INSERT INTO Subscribers (subscriberId, subscriptionNumber, email, mobilePhoneNumber, accountStatus) VALUES (?, ?, ?, ?, ?)";
            insertSubscriberStmt = connection.prepareStatement(insertSubscriberSql);
            insertSubscriberStmt.setInt(1, subscriber.getSubscriberId());
            insertSubscriberStmt.setString(2, subscriber.getSubscriptionNumber());
            insertSubscriberStmt.setString(3, subscriber.getEmail());
            insertSubscriberStmt.setString(4, subscriber.getMobilePhoneNumber());
            insertSubscriberStmt.setString(5, subscriber.getAccountStatus());

            int subscriberRowsInserted = insertSubscriberStmt.executeUpdate();

            if (subscriberRowsInserted > 0) {
                System.out.println("Subscriber with ID " + subscriber.getSubscriberId() + " added to Subscribers table.");
                return true;
            } else {
                System.out.println("Failed to add subscriber to Subscribers table.");
                return false;
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkStmt != null) checkStmt.close();
                if (insertUserStmt != null) insertUserStmt.close();
                if (insertSubscriberStmt != null) insertSubscriberStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    
    /**
     * Adds a new book to the database.
     *
     * @param book A {@link Books} object containing the details of the book to add.
     * @return {@code true} if the book was successfully added; {@code false} otherwise.
     */
    
    public static boolean addBook(Books book) {
        PreparedStatement checkStmt = null;
        PreparedStatement insertStmt = null;
        ResultSet rs = null;

        try {
            // Step 1: Check if the book already exists in the Books table
            String checkSql = "SELECT bookId FROM books WHERE bookId = ? OR barcode = ?";
            checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, book.getBookId());
            checkStmt.setString(2, book.getBarcode());

            rs = checkStmt.executeQuery();

            if (rs.next()) {
                // Book already exists
                System.out.println("Book with ID " + book.getBookId() + " already exists.");
                return false;
            }

            // Step 2: Insert the new book into the Books table
            String insertSql = "INSERT INTO Books (bookId, title, author, subject, description, location, availableCopies, barcode, nearestReturnDate) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
            insertStmt = connection.prepareStatement(insertSql);
            insertStmt.setInt(1, book.getBookId());
            insertStmt.setString(2, book.getTitle());
            insertStmt.setString(3, book.getAuthor());
            insertStmt.setString(4, book.getSubject());
            insertStmt.setString(5, book.getDescription()); // Corrected position
            insertStmt.setString(6, book.getLocation());
            insertStmt.setInt(7, book.getAvailableCopies());
            insertStmt.setString(8, book.getBarcode());
            insertStmt.setDate(9, book.getNearestReturnDate() != null ? java.sql.Date.valueOf(book.getNearestReturnDate()) : null);

            int rowsInserted = insertStmt.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println(book.getTitle() + " added successfully.");
                return true;
            } else {
                System.out.println("Failed to add book with ID " + book.getBookId() + ".");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkStmt != null) checkStmt.close();
                if (insertStmt != null) insertStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Updates the number of available copies for a specific book.
     *
     * @param bookId The ID of the book to update.
     * @param newCopiesCount The new number of available copies.
     * @return {@code true} if the update was successful; {@code false} otherwise.
     */
    public static boolean updateBookCopies(int bookId, int newCopiesCount) {
        PreparedStatement checkStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            // Step 1: Check if the book exists in the Books table
            String checkSql = "SELECT bookId FROM books WHERE bookId = ?";
            checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, bookId);

            rs = checkStmt.executeQuery();

            if (!rs.next()) {
                // Book does not exist
                System.out.println("Book with ID " + bookId + " does not exist.");
                return false;
            }

            // Step 2: Update the available copies in the Books table
            String updateSql = "UPDATE Books SET availableCopies = ? WHERE bookId = ?";
            updateStmt = connection.prepareStatement(updateSql);
            updateStmt.setInt(1, newCopiesCount);
            updateStmt.setInt(2, bookId);

            int rowsUpdated = updateStmt.executeUpdate();

            if (rowsUpdated > 0) {
                System.out.println("Available copies for book with ID " + bookId + " updated successfully.");
                return true;
            } else {
                System.out.println("Failed to update available copies for book with ID " + bookId + ".");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkStmt != null) checkStmt.close();
                if (updateStmt != null) updateStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
//    /**
//     * Deletes a book from the database.
//     *
//     * @param bookId The ID of the book to delete.
//     * @return {@code true} if the book was successfully deleted; {@code false} otherwise.
//     */
//    public static boolean deleteBook(int bookId) {
//        PreparedStatement checkBorrowStmt = null;
//        PreparedStatement checkReservationStmt = null;
//        PreparedStatement checkStmt = null;
//        PreparedStatement deleteStmt = null;
//        ResultSet rs = null;
//
//        try {
//            // Step 1: Check if there are active borrow records for the book
//            String checkBorrowSql = "SELECT recordId FROM BorrowRecords WHERE bookId = ? AND (status = 'Active' OR status = 'Extended')";
//            checkBorrowStmt = connection.prepareStatement(checkBorrowSql);
//            checkBorrowStmt.setInt(1, bookId);
//            rs = checkBorrowStmt.executeQuery();
//
//            if (rs.next()) {
//                System.out.println("Cannot delete book with ID " + bookId + " because it has active borrow records.");
//                return false;
//            }
//            // Step 2: Check if there are active reservations for the book
//            String checkReservationSql = "SELECT reservationId FROM Reservations WHERE bookId = ? AND status = 'Active'";
//            checkReservationStmt = connection.prepareStatement(checkReservationSql);
//            checkReservationStmt.setInt(1, bookId);
//            rs = checkReservationStmt.executeQuery();
//
//            if (rs.next()) {
//                System.out.println("Cannot delete book with ID " + bookId + " because it has active reservations.");
//                return false;
//            }
//
//            // Step 3: Check if the book exists in the Books table
//            String checkSql = "SELECT bookId FROM Books WHERE bookId = ?";
//            checkStmt = connection.prepareStatement(checkSql);
//            checkStmt.setInt(1, bookId);
//
//            rs = checkStmt.executeQuery();
//
//            if (!rs.next()) {
//                // Book does not exist
//                System.out.println("Book with ID " + bookId + " does not exist.");
//                return false;
//            }
//
//            // Step 4: Delete the book from the Books table
//            String deleteSql = "DELETE FROM Books WHERE bookId = ?";
//            deleteStmt = connection.prepareStatement(deleteSql);
//            deleteStmt.setInt(1, bookId);
//
//            int rowsDeleted = deleteStmt.executeUpdate();
//
//            if (rowsDeleted > 0) {
//                System.out.println("Book with ID " + bookId + " deleted successfully.");
//                return true;
//            } else {
//                System.out.println("Failed to delete book with ID " + bookId + ".");
//                return false;
//            }
//        } catch (SQLException e) {
//            e.printStackTrace();
//            return false;
//        } finally {
//            try {
//                if (rs != null) rs.close();
//                if (checkBorrowStmt != null) checkBorrowStmt.close();
//                if (checkStmt != null) checkStmt.close();
//                if (deleteStmt != null) deleteStmt.close();
//            } catch (SQLException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//    
    /**
     * Retrieves a list of all subscribers in the system.
     *
     * @return An {@link ArrayList} of {@link User} objects representing all subscribers.
     */
    public static ArrayList<User> getAllSubscribers() {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<User> subscribersList = new ArrayList<>();

        try {
            // Use a JOIN to fetch username along with subscriber details
            String sql = "SELECT u.username, u.userPassword, u.role, s.subscriberId, s.subscriptionNumber, s.email, s.mobilePhoneNumber, s.accountStatus " +
                         "FROM Subscribers s " +
                         "JOIN Users u ON s.subscriberId = u.userId";
            stmt = connection.prepareStatement(sql);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // Create a Subscriber object
                Subscriber subscriber = new Subscriber(
                    rs.getInt("subscriberId"),
                    rs.getString("subscriptionNumber"),
                    rs.getString("email"),
                    rs.getString("mobilePhoneNumber"),
                    rs.getString("accountStatus")
                );

                // Optionally, log the username
                String username = rs.getString("username");
                String password = rs.getString("userPassword");
                String role = rs.getString("role");
                User user = new User(
                		rs.getInt("subscriberId"),
                		username,
                		password,
                		role,
                		subscriber);
                subscribersList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return subscribersList;
    }

    /**
     * Freezes the account of a specific subscriber.
     *
     * @param subscriberId The ID of the subscriber to freeze.
     * @return {@code true} if the account was successfully frozen; {@code false} otherwise.
     */
    
    public static boolean freezeSubscriber(int subscriberId) {
        PreparedStatement checkStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            // Step 1: Check if the subscriber exists and is currently active
            String checkSql = "SELECT accountStatus FROM Subscribers WHERE subscriberId = ?";
            checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, subscriberId);
            rs = checkStmt.executeQuery();

            if (rs.next()) {
                String currentStatus = rs.getString("accountStatus");
                if ("Active".equalsIgnoreCase(currentStatus)) {
                    // Step 2: Update the account status to Frozen
                    String updateSql = "UPDATE Subscribers SET accountStatus = 'Frozen' WHERE subscriberId = ?";
                    updateStmt = connection.prepareStatement(updateSql);
                    updateStmt.setInt(1, subscriberId);
                    int rowsUpdated = updateStmt.executeUpdate();

                    if (rowsUpdated > 0) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                System.out.println("Subscriber with ID " + subscriberId + " does not exist.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkStmt != null) checkStmt.close();
                if (updateStmt != null) updateStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Activates the account of a specific subscriber.
     *
     * @param subscriberId The ID of the subscriber to activate.
     * @return {@code true} if the account was successfully activated; {@code false} otherwise.
     */
    
    public static boolean activateSubscriber(int subscriberId) {
        PreparedStatement checkStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        try {
            // Step 1: Check if the subscriber exists and is currently frozen
            String checkSql = "SELECT accountStatus FROM Subscribers WHERE subscriberId = ?";
            checkStmt = connection.prepareStatement(checkSql);
            checkStmt.setInt(1, subscriberId);

            rs = checkStmt.executeQuery();

            if (rs.next()) {
                String currentStatus = rs.getString("accountStatus");
                if ("Frozen".equalsIgnoreCase(currentStatus)) {
                    // Step 2: Update the account status to Active
                    String updateSql = "UPDATE Subscribers SET accountStatus = 'Active' WHERE subscriberId = ?";
                    updateStmt = connection.prepareStatement(updateSql);
                    updateStmt.setInt(1, subscriberId);

                    int rowsUpdated = updateStmt.executeUpdate();
                    if (rowsUpdated > 0) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    System.out.println("Subscriber with ID " + subscriberId + " is not currently frozen.");
                    return false;
                }
            } else {
                System.out.println("Subscriber with ID " + subscriberId + " does not exist.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkStmt != null) checkStmt.close();
                if (updateStmt != null) updateStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Updates the contact details (email and phone number) of a specific subscriber.
     *
     * @param subscriberId The ID of the subscriber.
     * @param newEmail The new email address for the subscriber.
     * @param newPhoneNumber The new phone number for the subscriber.
     * @return An {@link ArrayList} of objects containing the operation status and messages.
     */
    public static ArrayList<Object> updateSubscriberContactDetails(int subscriberId, String newEmail, String newPhoneNumber) {
        PreparedStatement checkSubscriberStmt = null;
        PreparedStatement checkEmailStmt = null;
        PreparedStatement checkPhoneStmt = null;
        PreparedStatement updateStmt = null;
        ResultSet rs = null;

        ArrayList<Object> result = new ArrayList<>();

        try {
            // Step 1: Check if the subscriber exists in the Subscribers table
            String checkSubscriberSql = "SELECT subscriberId FROM Subscribers WHERE subscriberId = ?";
            checkSubscriberStmt = connection.prepareStatement(checkSubscriberSql);
            checkSubscriberStmt.setInt(1, subscriberId);

            rs = checkSubscriberStmt.executeQuery();

            if (!rs.next()) {
                // Subscriber does not exist
                result.add(false);
                result.add("Subscriber with ID " + subscriberId + " does not exist.");
                return result;
            }

            // Step 2: Check if the new email already exists
            String checkEmailSql = "SELECT subscriberId FROM Subscribers WHERE email = ? AND subscriberId != ?";
            checkEmailStmt = connection.prepareStatement(checkEmailSql);
            checkEmailStmt.setString(1, newEmail);
            checkEmailStmt.setInt(2, subscriberId);

            rs = checkEmailStmt.executeQuery();

            if (rs.next()) {
                // Email already exists for another subscriber
                result.add(false);
                result.add("The email " + newEmail + " is already in use by another subscriber.");
                return result;
            }

            // Step 3: Check if the new phone number already exists
            String checkPhoneSql = "SELECT subscriberId FROM Subscribers WHERE mobilePhoneNumber = ? AND subscriberId != ?";
            checkPhoneStmt = connection.prepareStatement(checkPhoneSql);
            checkPhoneStmt.setString(1, newPhoneNumber);
            checkPhoneStmt.setInt(2, subscriberId);

            rs = checkPhoneStmt.executeQuery();

            if (rs.next()) {
                // Phone number already exists for another subscriber
                result.add(false);
                result.add("The phone number " + newPhoneNumber + " is already in use by another subscriber.");
                return result;
            }

            // Step 4: Update the email and phone number for the subscriber
            String updateSql = "UPDATE Subscribers SET email = ?, mobilePhoneNumber = ? WHERE subscriberId = ?";
            updateStmt = connection.prepareStatement(updateSql);
            updateStmt.setString(1, newEmail);
            updateStmt.setString(2, newPhoneNumber);
            updateStmt.setInt(3, subscriberId);

            int rowsUpdated = updateStmt.executeUpdate();

            if (rowsUpdated > 0) {
                result.add(true);
                result.add("Subscriber with ID " + subscriberId + " contact details updated successfully.");
            } else {
                result.add(false);
                result.add("Failed to update contact details for subscriber with ID " + subscriberId + ".");
            }

            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            result.add(false);
            result.add("An error occurred: " + e.getMessage());
            return result;
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkSubscriberStmt != null) checkSubscriberStmt.close();
                if (checkEmailStmt != null) checkEmailStmt.close();
                if (checkPhoneStmt != null) checkPhoneStmt.close();
                if (updateStmt != null) updateStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Starts the borrowing process for a subscriber and a specific book.
     * Simulates the scanning of a subscriber's card (via subscription number) and a book's barcode.
     *
     * @param subscriptionNumber The subscription number of the subscriber (simulates the card scan).
     * @param bookBarcode The barcode of the book to borrow (simulates the book scan).
     * @return An {@link ArrayList} of objects containing the operation status (Boolean) and messages (String).
     */
  
    
    public static ArrayList<Object> startBorrowingProcess(String subscriptionNumber, String bookBarcode) {
        PreparedStatement checkStmt = null;
        PreparedStatement updateBookStmt = null;
        PreparedStatement insertBorrowStmt = null;
        PreparedStatement updateReservationStmt = null;
        PreparedStatement updateNearestReturnStmt = null;
        ResultSet rs = null;

        ArrayList<Object> result = new ArrayList<>();

        try {
            // Query to fetch subscriber and book details using subscription number
            String sql = "SELECT s.subscriberId, u.username, u.userPassword, s.accountStatus, " +
                         "b.bookId, b.title, b.author, b.availableCopies, b.barcode, b.nearestReturnDate " +
                         "FROM Subscribers s " +
                         "JOIN Users u ON s.subscriberId = u.userId " +
                         "JOIN Books b ON b.barcode = ? " +
                         "WHERE s.subscriptionNumber = ?";
            checkStmt = connection.prepareStatement(sql);
            checkStmt.setString(1, bookBarcode);
            checkStmt.setString(2, subscriptionNumber);

            rs = checkStmt.executeQuery();

            if (!rs.next()) {
                result.add(false);
                result.add("Subscriber or book not found.");
                return result;
            }

            // Extract subscriber and book details
            int subscriberId = rs.getInt("subscriberId");
            String accountStatus = rs.getString("accountStatus");
            int bookId = rs.getInt("bookId");
            int availableCopies = rs.getInt("availableCopies");
            String bookTitle = rs.getString("title");
            LocalDate nearestReturnDate = rs.getDate("nearestReturnDate") != null
                    ? rs.getDate("nearestReturnDate").toLocalDate()
                    : null;

            // Check if the subscriber's account is frozen
            if ("Frozen".equalsIgnoreCase(accountStatus)) {
                result.add(false);
                result.add("Subscriber's account is frozen. Borrowing not allowed.");
                return result;
            }

            // If no copies are available, check for active reservations
            if (availableCopies <= 0) {
                String reservationCheckSql = "SELECT reservationId FROM Reservations " +
                                             "WHERE bookId = ? AND subscriberId = ? AND status = 'Active' AND expirationDate IS NOT NULL";
                PreparedStatement reservationCheckStmt = connection.prepareStatement(reservationCheckSql);
                reservationCheckStmt.setInt(1, bookId);
                reservationCheckStmt.setInt(2, subscriberId);
                ResultSet reservationRs = reservationCheckStmt.executeQuery();

                if (reservationRs.next()) {
                    int reservationId = reservationRs.getInt("reservationId");

                    // Update reservation status to 'Unactive'
                    String updateReservationSql = "UPDATE Reservations SET status = 'Unactive' WHERE reservationId = ?";
                    updateReservationStmt = connection.prepareStatement(updateReservationSql);
                    updateReservationStmt.setInt(1, reservationId);
                    updateReservationStmt.executeUpdate();

                    // Create a borrow record without changing availableCopies
                    createBorrowRecord(subscriberId, bookId, bookTitle, nearestReturnDate, result);
                } else {
                    result.add(false);
                    result.add("No copies available and no valid reservation found.");
                }
                return result;
            }

            // Proceed with normal borrowing (copies are available)
            String updateSql = "UPDATE Books SET availableCopies = availableCopies - 1 WHERE bookId = ?";
            updateBookStmt = connection.prepareStatement(updateSql);
            updateBookStmt.setInt(1, bookId);
            updateBookStmt.executeUpdate();

            // Create borrow record and update nearest return date
            createBorrowRecord(subscriberId, bookId, bookTitle, nearestReturnDate, result);

            // Update nearest return date if necessary
            LocalDate newDueDate = LocalDate.now().plusWeeks(2);
            if (nearestReturnDate == null || newDueDate.isBefore(nearestReturnDate)) {
                String updateNearestReturnSql = "UPDATE Books SET nearestReturnDate = ? WHERE bookId = ?";
                updateNearestReturnStmt = connection.prepareStatement(updateNearestReturnSql);
                updateNearestReturnStmt.setDate(1, java.sql.Date.valueOf(newDueDate));
                updateNearestReturnStmt.setInt(2, bookId);
                updateNearestReturnStmt.executeUpdate();
            }

            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            result.add(false);
            result.add("An error occurred: " + e.getMessage());
            return result;
        } finally {
            try {
                if (rs != null) rs.close();
                if (checkStmt != null) checkStmt.close();
                if (updateBookStmt != null) updateBookStmt.close();
                if (insertBorrowStmt != null) insertBorrowStmt.close();
                if (updateReservationStmt != null) updateReservationStmt.close();
                if (updateNearestReturnStmt != null) updateNearestReturnStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    



    /**
     * Helper method to create a borrow record.
     *
     * @param subscriberId The ID of the subscriber borrowing the book.
     * @param bookId The ID of the book being borrowed.
     * @param bookTitle The title of the book being borrowed.
     * @param nearestReturnDate The nearest return date for the book.
     * @param result An {@link ArrayList} to store the operation status and messages.
     * @throws SQLException If a database error occurs.
     */
    private static void createBorrowRecord(int subscriberId, int bookId, String bookTitle, LocalDate nearestReturnDate, ArrayList<Object> result) throws SQLException {
        String insertSql = "INSERT INTO BorrowRecords (subscriberId, bookId, borrowDate, dueDate, status) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement insertBorrowStmt = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS);
        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusWeeks(2);
        insertBorrowStmt.setInt(1, subscriberId);
        insertBorrowStmt.setInt(2, bookId);
        insertBorrowStmt.setDate(3, java.sql.Date.valueOf(borrowDate));
        insertBorrowStmt.setDate(4, java.sql.Date.valueOf(dueDate));
        insertBorrowStmt.setString(5, "Active");

        int rowsInserted = insertBorrowStmt.executeUpdate();

        if (rowsInserted > 0) {
            // Send a success message to the subscriber
            createMessage(
                subscriberId,
                "You have successfully borrowed the book '" + bookTitle + "'. It is due on " + dueDate + ".",
                "Borrow"
            );
            result.add(true);
            result.add("Borrowing process completed successfully.");
        } else {
            result.add(false);
            result.add("Failed to create borrow record.");
        }
    }

    /**
     * Fetches all borrow records for a specific subscriber.
     *
     * @param subscriberId The ID of the subscriber.
     * @return An {@link ArrayList} of {@link BorrowRecord} objects representing the subscriber's borrow history.
     */
    public static ArrayList<BorrowRecord> fetchBorrowRecordsBySubscriberId(int subscriberId) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<BorrowRecord> borrowRecords = new ArrayList<>();

        try {
            // Query to fetch borrow records for a subscriber
            String sql = "SELECT recordId, subscriberId, bookId, borrowDate, dueDate, returnDate, status " +
                         "FROM BorrowRecords " +
                         "WHERE subscriberId = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, subscriberId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                // Fetch the book details using fetchBookDetails method
                Books book = fetchBookDetails(rs.getInt("bookId"));

                // Create the BorrowRecord object
                BorrowRecord borrowRecord = new BorrowRecord(
                    rs.getInt("recordId"),
                    rs.getInt("subscriberId"),
                    rs.getInt("bookId"),
                    rs.getDate("borrowDate").toLocalDate(),
                    rs.getDate("dueDate").toLocalDate(),
                    rs.getDate("returnDate") != null ? rs.getDate("returnDate").toLocalDate() : null,
                    rs.getString("status")
                );

                // Attach the book details to the borrow record
                borrowRecord.setBook(book);

                // Add the borrow record to the list
                borrowRecords.add(borrowRecord);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return borrowRecords;
    }
    /**
     * Adds a reservation for a book by a specific subscriber.
     *
     * @param bookId The ID of the book to reserve.
     * @param subscriberId The ID of the subscriber making the reservation.
     * @return An {@link ArrayList} of objects containing the operation status and messages.
     */
    public static ArrayList<Object> addReservation(int bookId, int subscriberId) {
        PreparedStatement insertReservationStmt = null;
        PreparedStatement checkSubscriberStmt = null;
        ResultSet rs = null;

        ArrayList<Object> result = new ArrayList<>();
        
        try {
        	// Step 1: Check the status of the subscriber
            String checkSubscriberSql = "SELECT accountStatus FROM Subscribers WHERE subscriberId = ?";
            checkSubscriberStmt = connection.prepareStatement(checkSubscriberSql);
            checkSubscriberStmt.setInt(1, subscriberId);

            rs = checkSubscriberStmt.executeQuery();

            if (rs.next()) { // Check if a record exists for the subscriber
                String accountStatus = rs.getString("accountStatus");
                if ("Frozen".equalsIgnoreCase(accountStatus)) {
                    result.add(false);
                    result.add("Cannot add reservation: Subscriber with ID " + subscriberId + " has a frozen account.");
                    return result;
                }
            } else {
                result.add(false);
                result.add("Subscriber with ID " + subscriberId + " does not exist.");
                return result;
            }
            // Step 2: Check if it's possible to create a reservation
            if (!canCreateReservation(bookId)) {
                result.add(false);
                result.add("Cannot add reservation: Active reservations exceed available book copies.");
                return result;
            }

            // Step 3: Insert the reservation
            String insertReservationSql = "INSERT INTO Reservations (bookId, bookTitle, subscriberId, reservationDate, status) " +
                                           "VALUES (?, (SELECT title FROM Books WHERE bookId = ?), ?, ?, 'Active')";
            insertReservationStmt = connection.prepareStatement(insertReservationSql);
            insertReservationStmt.setInt(1, bookId);
            insertReservationStmt.setInt(2, bookId); // For fetching book title
            insertReservationStmt.setInt(3, subscriberId);
            insertReservationStmt.setDate(4, java.sql.Date.valueOf(LocalDate.now()));

            int rowsInserted = insertReservationStmt.executeUpdate();

            if (rowsInserted > 0) {
            	Books book = fetchBookDetails(bookId);
            	createMessage(
                		subscriberId,
                        "You have successfully placed a reservation for the book " + book.getTitle() + ".",
                        "Reservation"
                    );
                result.add(true);
                result.add("Reservation added successfully for book ID " + bookId + " by subscriber ID " + subscriberId + ".");
            } else {
                result.add(false);
                result.add("Failed to add reservation. Please try again.");
            }
            
            
            return result;
        } catch (SQLException e) {
            e.printStackTrace();
            result.add(false);
            result.add("An error occurred: " + e.getMessage());
            return result;
        } finally {
            try {
                if (rs != null) rs.close();
                if (insertReservationStmt != null) insertReservationStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Fetches all reservations for a specific subscriber.
     *
     * @param subscriberId The ID of the subscriber.
     * @return An {@link ArrayList} of {@link Reservation} objects representing the subscriber's reservations.
     */
    
    public static ArrayList<Reservation> fetchAllReservationsForSubscriber(int subscriberId) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<Reservation> reservationsList = new ArrayList<>();

        try {
            // SQL query to fetch reservations for a specific subscriber
            String sql = "SELECT r.reservationId, r.bookId, r.bookTitle, r.subscriberId, " +
                         "r.reservationDate, r.expirationDate, r.priority, r.status " +
                         "FROM Reservations r " +
                         "WHERE r.subscriberId = ? " +
                         "ORDER BY r.priority ASC";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, subscriberId);

            rs = stmt.executeQuery();

            // Loop through the result set and populate Reservation objects
            while (rs.next()) {
                Reservation reservation = new Reservation(
                    rs.getInt("reservationId"),
                    rs.getInt("bookId"),
                    rs.getString("bookTitle"),
                    rs.getInt("subscriberId"),
                    rs.getDate("reservationDate").toLocalDate(),
                    rs.getDate("expirationDate") != null ? rs.getDate("expirationDate").toLocalDate() : null,
                    rs.getString("status")
                );
              //  reservation.setPriority(rs.getInt("priority"));
                reservationsList.add(reservation);
            }

            return reservationsList;
        } catch (SQLException e) {
            e.printStackTrace();
            return reservationsList;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Processes the return of a borrowed book.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *   <li>Fetches the book ID using the provided barcode.</li>
     *   <li>Verifies if the subscriber has an active borrow record for the book.</li>
     *   <li>Marks the borrow record as returned and updates the return date.</li>
     *   <li>Sends a confirmation message to the subscriber upon successful return.</li>
     * </ul>
     *
     * @param subscriberId The ID of the subscriber returning the book.
     * @param bookBarcode The barcode of the book being returned.
     * @return An {@link ArrayList} of objects containing the operation status and messages.
     */
    public static ArrayList<Object> returnBook(String subscriptionNumber, String bookBarcode) {
        PreparedStatement getSubscriberIdStmt = null;
        PreparedStatement getBookIdStmt = null;
        PreparedStatement checkBorrowStmt = null;
        PreparedStatement updateBorrowStmt = null;
        ResultSet rs = null;

        ArrayList<Object> result = new ArrayList<>();

        try {
            // Step 1: Fetch the subscriber ID using the subscription number
            String getSubscriberIdSql = "SELECT subscriberId FROM Subscribers WHERE subscriptionNumber = ?";
            getSubscriberIdStmt = connection.prepareStatement(getSubscriberIdSql);
            getSubscriberIdStmt.setString(1, subscriptionNumber);
            rs = getSubscriberIdStmt.executeQuery();

            if (!rs.next()) {
                result.add(false);
                result.add("No subscriber found with the given subscription number: " + subscriptionNumber);
                return result;
            }

            int subscriberId = rs.getInt("subscriberId");

            // Step 2: Fetch the book ID using the barcode
            String getBookIdSql = "SELECT bookId FROM Books WHERE barcode = ?";
            getBookIdStmt = connection.prepareStatement(getBookIdSql);
            getBookIdStmt.setString(1, bookBarcode);
            rs = getBookIdStmt.executeQuery();

            if (!rs.next()) {
                result.add(false);
                result.add("No book found with the given barcode: " + bookBarcode);
                return result;
            }

            int bookId = rs.getInt("bookId");

            // Step 3: Check if the subscriber has borrowed this book
            String checkBorrowSql = "SELECT recordId FROM BorrowRecords WHERE subscriberId = ? AND bookId = ? AND (status = 'Active' OR status = 'Extended')";
            checkBorrowStmt = connection.prepareStatement(checkBorrowSql);
            checkBorrowStmt.setInt(1, subscriberId);
            checkBorrowStmt.setInt(2, bookId);
            rs = checkBorrowStmt.executeQuery();

            if (!rs.next()) {
                result.add(false);
                result.add("No active borrow record found for subscription number: " + subscriptionNumber + " and book barcode: " + bookBarcode);
                return result;
            }

            int recordId = rs.getInt("recordId");

            // Step 4: Update the borrow record
            String updateBorrowSql = "UPDATE BorrowRecords SET returnDate = ?, status = 'Returned' WHERE recordId = ?";
            updateBorrowStmt = connection.prepareStatement(updateBorrowSql);
            updateBorrowStmt.setDate(1, java.sql.Date.valueOf(LocalDate.now()));
            updateBorrowStmt.setInt(2, recordId);
            updateBorrowStmt.executeUpdate();

            Books book = fetchBookDetails(bookId);
            createMessage(
                subscriberId,
                "You have successfully returned the book '" + book.getTitle() + "'.",
                "Return"
            );

            result.add(true);
            result.add("Book with barcode '" + bookBarcode + "' successfully returned by subscription number " + subscriptionNumber + ".");

            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            result.add(false);
            result.add("An error occurred: " + e.getMessage());
            return result;
        } finally {
            try {
                if (rs != null) rs.close();
                if (getSubscriberIdStmt != null) getSubscriberIdStmt.close();
                if (getBookIdStmt != null) getBookIdStmt.close();
                if (checkBorrowStmt != null) checkBorrowStmt.close();
                if (updateBorrowStmt != null) updateBorrowStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Determines if a reservation can be created for a specific book.
     *
     * @param bookId The ID of the book.
     * @return {@code true} if a reservation can be created; {@code false} otherwise.
     */
    public static boolean canCreateReservation(int bookId) {
        PreparedStatement borrowCountStmt = null;
        PreparedStatement reservationStmt = null;
        ResultSet borrowCountRs = null;
        ResultSet reservationRs = null;

        try {
            // Step 1: Count the total number of active borrow records for the book
            String borrowCountSql = "SELECT COUNT(*) AS activeBorrows FROM BorrowRecords WHERE bookId = ? AND (status = 'Active' OR status = 'Extended') ";
            borrowCountStmt = connection.prepareStatement(borrowCountSql);
            borrowCountStmt.setInt(1, bookId);
            borrowCountRs = borrowCountStmt.executeQuery();

            int totalCopiesInUse = 0;
            if (borrowCountRs.next()) {
                totalCopiesInUse = borrowCountRs.getInt("activeBorrows");
            }

            // Step 2: Count the number of active reservations for the book
            String reservationSql = "SELECT COUNT(*) AS activeReservations FROM Reservations WHERE bookId = ? AND status = 'Active'";
            reservationStmt = connection.prepareStatement(reservationSql);
            reservationStmt.setInt(1, bookId);
            reservationRs = reservationStmt.executeQuery();

            int activeReservations = 0;
            if (reservationRs.next()) {
                activeReservations = reservationRs.getInt("activeReservations");
            }

            // Step 3: Compare active reservations with active borrow records (total copies in use)
            if (activeReservations < totalCopiesInUse) {
                return true; // Can create more reservations
            } else {
                System.out.println("Cannot create more reservations. Active reservations: " + activeReservations + ", Total copies in use: " + totalCopiesInUse);
                return false; // Limit reached
            }

        } catch (SQLException e) {
            e.printStackTrace();
            return false; // Handle SQL error
        } finally {
            try {
                if (borrowCountRs != null) borrowCountRs.close();
                if (reservationRs != null) reservationRs.close();
                if (borrowCountStmt != null) borrowCountStmt.close();
                if (reservationStmt != null) reservationStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Cancels a specific reservation and updates the system accordingly.
     *
     * <p>This method performs the following steps:</p>
     * <ul>
     *   <li>Marks the reservation as "Canceled" in the database.</li>
     *   <li>Adjusts the priority of subsequent reservations for the same book.</li>
     *   <li>Handles the impact on available copies of the book, if applicable.</li>
     *   <li>Checks if the next reservation in line can be fulfilled and assigns the book if possible.</li>
     *   <li>Sends a notification message to the subscriber confirming the cancellation.</li>
     * </ul>
     *
     * @param reservationId The ID of the reservation to cancel.
     * @return An {@link ArrayList} of objects containing the operation status and messages.
     */
    public static ArrayList<Object> cancelReservation(int reservationId) {
        PreparedStatement updateReservationStmt = null;
        PreparedStatement fetchReservationDetailsStmt = null;
        PreparedStatement updatePriorityStmt = null;
        PreparedStatement assignNextReservationStmt = null;
        PreparedStatement checkAvailableCopiesStmt = null;
        PreparedStatement updateAvailableCopiesStmt = null;

        ArrayList<Object> result = new ArrayList<>();

        try {
            // Step 1: Mark the reservation as "Canceled"
            String updateReservationSql = "UPDATE Reservations SET status = 'Canceled' WHERE reservationId = ? AND status = 'Active'";
            updateReservationStmt = connection.prepareStatement(updateReservationSql);
            updateReservationStmt.setInt(1, reservationId);
            int rowsUpdated = updateReservationStmt.executeUpdate();

            if (rowsUpdated <= 0) {
                result.add(false);
                result.add("Failed to cancel the reservation with ID " + reservationId + ".");
                return result;
            }

            // Step 2: Fetch the bookId, priority, and expirationDate of the canceled reservation
            String fetchReservationDetailsSql = "SELECT bookId, priority, expirationDate, subscriberId FROM Reservations WHERE reservationId = ?";
            fetchReservationDetailsStmt = connection.prepareStatement(fetchReservationDetailsSql);
            fetchReservationDetailsStmt.setInt(1, reservationId);
            ResultSet reservationDetailsRs = fetchReservationDetailsStmt.executeQuery();

            if (!reservationDetailsRs.next()) {
                result.add(false);
                result.add("Reservation not found with ID " + reservationId + ".");
                return result;
            }
            int subscriberId = reservationDetailsRs.getInt("subscriberId");
            int bookId = reservationDetailsRs.getInt("bookId");
            int priority = reservationDetailsRs.getInt("priority");
            Date expirationDate = reservationDetailsRs.getDate("expirationDate");

            // Step 3: Update priorities of subsequent reservations for the same book
            String updatePrioritySql = "UPDATE Reservations SET priority = priority - 1 WHERE bookId = ? AND priority > ?";
            updatePriorityStmt = connection.prepareStatement(updatePrioritySql);
            updatePriorityStmt.setInt(1, bookId);
            updatePriorityStmt.setInt(2, priority);
            updatePriorityStmt.executeUpdate();

            // Step 4: Handle the impact on availableCopies only if the reservation had a book assigned (expirationDate is NOT NULL)
            if (expirationDate != null) {
                // Increment the availableCopies since the reservation had a book assigned
                String updateAvailableCopiesSql = "UPDATE Books SET availableCopies = availableCopies + 1 WHERE bookId = ?";
                updateAvailableCopiesStmt = connection.prepareStatement(updateAvailableCopiesSql);
                updateAvailableCopiesStmt.setInt(1, bookId);
                updateAvailableCopiesStmt.executeUpdate();
            }

            // Step 5: Check if the next reservation can be fulfilled
            String checkAvailableCopiesSql = "SELECT availableCopies FROM Books WHERE bookId = ?";
            checkAvailableCopiesStmt = connection.prepareStatement(checkAvailableCopiesSql);
            checkAvailableCopiesStmt.setInt(1, bookId);
            ResultSet availableCopiesRs = checkAvailableCopiesStmt.executeQuery();

            if (availableCopiesRs.next() && availableCopiesRs.getInt("availableCopies") > 0) {
                // Fulfill the next reservation if a copy is available
                String assignNextReservationSql = "UPDATE Reservations SET expirationDate = DATE_ADD(CURDATE(), INTERVAL 2 DAY) " +
                                                  "WHERE bookId = ? AND status = 'Active' AND expirationDate IS NULL " +
                                                  "ORDER BY priority ASC LIMIT 1";
                assignNextReservationStmt = connection.prepareStatement(assignNextReservationSql);
                assignNextReservationStmt.setInt(1, bookId);
                int nextReservationAssigned = assignNextReservationStmt.executeUpdate();

                if (nextReservationAssigned > 0) {
                    // Decrement the availableCopies after assigning the reservation
                    String decrementCopiesSql = "UPDATE Books SET availableCopies = availableCopies - 1 WHERE bookId = ?";
                    PreparedStatement decrementCopiesStmt = connection.prepareStatement(decrementCopiesSql);
                    decrementCopiesStmt.setInt(1, bookId);
                    decrementCopiesStmt.executeUpdate();
                    decrementCopiesStmt.close();
                }
            }
            
            // Step 6: Return success message
            Books book = fetchBookDetails(bookId);
        	createMessage(
            		subscriberId,
                    "You have successfully canceled a reservation for the book " + book.getTitle() + ".",
                    "Reservation"
                );
            result.add(true);
            result.add("Reservation with ID " + reservationId + " has been successfully canceled.");
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            result.add(false);
            result.add("An error occurred: " + e.getMessage());
            return result;
        } finally {
            try {
                if (updateReservationStmt != null) updateReservationStmt.close();
                if (fetchReservationDetailsStmt != null) fetchReservationDetailsStmt.close();
                if (updatePriorityStmt != null) updatePriorityStmt.close();
                if (assignNextReservationStmt != null) assignNextReservationStmt.close();
                if (checkAvailableCopiesStmt != null) checkAvailableCopiesStmt.close();
                if (updateAvailableCopiesStmt != null) updateAvailableCopiesStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Extends the borrowing time for a specific borrow record.
     *
     * @param recordId The ID of the borrow record.
     * @param extendedBy Indicates whether the extension was requested by the system or manually by a librarian.
     * @param newDueDate The new due date for the borrowed book.
     * @return An {@link ArrayList} of objects containing the operation status and messages.
     */
    public static ArrayList<Object> extendBorrowingTime(int recordId, String extendedBy, LocalDate newDueDate) {
        PreparedStatement fetchBorrowRecordStmt = null;
        PreparedStatement fetchActiveReservationsStmt = null;
        PreparedStatement updateBorrowRecordStmt = null;
        PreparedStatement fetchNearestReturnDateStmt = null;
        PreparedStatement updateNearestReturnDateStmt = null;

        ArrayList<Object> result = new ArrayList<>();

        try {
            // Step 1: Fetch the borrow record details
            String fetchBorrowRecordSql = "SELECT subscriberId, bookId, dueDate FROM BorrowRecords WHERE recordId = ? AND status = 'Active'";
            fetchBorrowRecordStmt = connection.prepareStatement(fetchBorrowRecordSql);
            fetchBorrowRecordStmt.setInt(1, recordId);
            ResultSet borrowRecordRs = fetchBorrowRecordStmt.executeQuery();

            if (!borrowRecordRs.next()) {
                result.add(false);
                result.add("No active borrow record found with ID " + recordId + ".");
                return result;
            }
            int subscriberId = borrowRecordRs.getInt("subscriberId");
            int bookId = borrowRecordRs.getInt("bookId");
            Books book = fetchBookDetails(bookId);
            Subscriber sub = fetchSubscriberDetails(subscriberId);
            LocalDate currentDueDate = borrowRecordRs.getDate("dueDate").toLocalDate();

            // Ensure the new due date is after the current due date
            if (newDueDate.isBefore(currentDueDate)) {
                result.add(false);
                result.add("The new due date must be after the current due date.");
                return result;
            }

            // Step 2: Check for active reservations for the book
            String fetchActiveReservationsSql = "SELECT COUNT(*) AS activeReservations FROM Reservations WHERE bookId = ? AND status = 'Active'";
            fetchActiveReservationsStmt = connection.prepareStatement(fetchActiveReservationsSql);
            fetchActiveReservationsStmt.setInt(1, bookId);
            ResultSet activeReservationsRs = fetchActiveReservationsStmt.executeQuery();

            if (activeReservationsRs.next() && activeReservationsRs.getInt("activeReservations") > 0) {
                result.add(false);
                result.add("Cannot extend the borrowing time as there are active reservations for this book.");
                if(extendedBy.equals("system")) {
               	 String subscriberMessage = "Subscribers "+ sub.getSubscriptionNumber() +"Extension request for (ID: " + recordId + ") for book " + book.getTitle() +
                            " has been denayed";
                    createMessage(subscriberId, subscriberMessage, "ExtensionNotification");
               }
               else {
               	 String subscriberMessage = "Subscribers "+ sub.getSubscriptionNumber() +"Manual Extension by librarian " + extendedBy + ", request for (ID: " + recordId + ") for book " + book.getTitle() +
                            " has been denayed ";
                    createMessage(subscriberId, subscriberMessage, "ExtensionNotification");
               }
                return result;
            }

            // Step 3: Allow the extension and update the due date
            String updateBorrowRecordSql = "UPDATE BorrowRecords SET dueDate = ?, status = 'Extended' WHERE recordId = ?";
            updateBorrowRecordStmt = connection.prepareStatement(updateBorrowRecordSql);
            updateBorrowRecordStmt.setDate(1, java.sql.Date.valueOf(newDueDate));
            updateBorrowRecordStmt.setInt(2, recordId);
            updateBorrowRecordStmt.executeUpdate();
            
          
            
            // Step 4: Update the nearest return date for the book
            String fetchNearestReturnDateSql = "SELECT MIN(dueDate) AS nearestReturnDate FROM BorrowRecords WHERE bookId = ? AND status = 'Active'";
            fetchNearestReturnDateStmt = connection.prepareStatement(fetchNearestReturnDateSql);
            fetchNearestReturnDateStmt.setInt(1, bookId);
            ResultSet nearestReturnDateRs = fetchNearestReturnDateStmt.executeQuery();

            if (nearestReturnDateRs.next() && nearestReturnDateRs.getDate("nearestReturnDate") != null) {
                LocalDate nearestReturnDate = nearestReturnDateRs.getDate("nearestReturnDate").toLocalDate();
                String updateNearestReturnDateSql = "UPDATE Books SET nearestReturnDate = ? WHERE bookId = ?";
                updateNearestReturnDateStmt = connection.prepareStatement(updateNearestReturnDateSql);
                updateNearestReturnDateStmt.setDate(1, java.sql.Date.valueOf(nearestReturnDate));
                updateNearestReturnDateStmt.setInt(2, bookId);
                updateNearestReturnDateStmt.executeUpdate();
            }
            
            if(extendedBy.equals("system")) {
            	 String subscriberMessage = "Subscribers "+ sub.getSubscriptionNumber() +"Extension request for (ID: " + recordId + ") for book " + book.getTitle() +
                         " has been approved";
                 createMessage(subscriberId, subscriberMessage, "ExtensionNotification");
            }
            else {
            	 String subscriberMessage = "Subscribers "+ sub.getSubscriptionNumber() +"Manual Extension by librarian " + extendedBy + ", request for (ID: " + recordId + ") for book " + book.getTitle() +
                         " has been approved ";
                 createMessage(subscriberId, subscriberMessage, "ExtensionNotification");
            }
           
            result.add(true);
            result.add("The borrowing time for record ID " + recordId + " has been successfully extended to " + newDueDate + ".");
            return result;

        } catch (SQLException e) {
            e.printStackTrace();
            result.add(false);
            result.add("An error occurred: " + e.getMessage());
            return result;
        } finally {
            try {
                if (fetchBorrowRecordStmt != null) fetchBorrowRecordStmt.close();
                if (fetchActiveReservationsStmt != null) fetchActiveReservationsStmt.close();
                if (updateBorrowRecordStmt != null) updateBorrowRecordStmt.close();
                if (fetchNearestReturnDateStmt != null) fetchNearestReturnDateStmt.close();
                if (updateNearestReturnDateStmt != null) updateNearestReturnDateStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Fetches all issues reported by a specific subscriber.
     *
     * @param subscriberId The ID of the subscriber.
     * @return An {@link ArrayList} of {@link Issues} objects representing the subscriber's reported issues.
     */

    public static ArrayList<Issues> fetchIssuesBySubscriber(int subscriberId) {
        PreparedStatement stmt = null;
        ResultSet rs = null;
        ArrayList<Issues> issuesList = new ArrayList<>();

        try {
            String sql = "SELECT * FROM Issues WHERE subscriberId = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, subscriberId);

            rs = stmt.executeQuery();

            while (rs.next()) {
                Issues issue = new Issues(
                    rs.getInt("issueId"),
                    rs.getInt("subscriberId"),
                    rs.getString("description"),
                    rs.getDate("dateReported").toLocalDate(),
                    rs.getString("status")
                );
                issuesList.add(issue);
            }

            return issuesList;
        } catch (SQLException e) {
            e.printStackTrace();
            return issuesList;
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
	* Reports a book as lost for a specific borrow record.
	*
	* @param recordId The ID of the borrow record associated with the lost book.
	* @return {@code true} if the issue was successfully reported; {@code false} otherwise.
	*/
    
    public static boolean reportLostBook(int recordId) {
        PreparedStatement fetchRecordStmt = null;
        PreparedStatement insertLostIssueStmt = null;
        ResultSet recordResultSet = null;

        try {
            // Step 1: Fetch the subscriberId and bookId using the recordId
            String fetchRecordSql = "SELECT subscriberId, bookId FROM BorrowRecords WHERE recordId = ? AND status IN ('Active', 'Extended')";
            fetchRecordStmt = connection.prepareStatement(fetchRecordSql);
            fetchRecordStmt.setInt(1, recordId);

            recordResultSet = fetchRecordStmt.executeQuery();

            if (!recordResultSet.next()) {
                System.out.println("No active or extended borrow record found with ID: " + recordId);
                return false;
            }

            int subscriberId = recordResultSet.getInt("subscriberId");
            int bookId = recordResultSet.getInt("bookId");

            // Step 2: Insert into the Issues table if not already present
            String insertIssueSql = "INSERT INTO Issues (subscriberId, description, dateReported, status, issueType) " +
                                    "SELECT ?, CONCAT('Book ID ', ?, ' reported lost'), CURDATE(), 'Open', 'Lost' " +
                                    "WHERE NOT EXISTS ( " +
                                    "    SELECT 1 FROM Issues i " +
                                    "    WHERE i.subscriberId = ? " +
                                    "    AND i.description LIKE CONCAT('%Book ID ', ?, '%') " +
                                    "    AND i.issueType = 'Lost' " +
                                    "    AND i.status = 'Open' " +
                                    ")";
            insertLostIssueStmt = connection.prepareStatement(insertIssueSql);

            // Set parameters for the issue insertion
            insertLostIssueStmt.setInt(1, subscriberId); // Subscriber ID
            insertLostIssueStmt.setInt(2, bookId);       // Book ID
            insertLostIssueStmt.setInt(3, subscriberId); // For NOT EXISTS check
            insertLostIssueStmt.setInt(4, bookId);       // For NOT EXISTS check

            // Execute the query
            int rowsAffected = insertLostIssueStmt.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Lost issue reported for record ID: " + recordId + " (Subscriber ID: " + subscriberId + ", Book ID: " + bookId + ")");
                return true;
            } else {
                System.out.println("Lost issue already exists for record ID: " + recordId);
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (recordResultSet != null) recordResultSet.close();
                if (fetchRecordStmt != null) fetchRecordStmt.close();
                if (insertLostIssueStmt != null) insertLostIssueStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Resolves a specific issue based on its ID.
     *
     * @param issueId The ID of the issue to resolve.
     * @return {@code true} if the issue was successfully resolved; {@code false} otherwise.
     */
    public static boolean resolveIssue(int issueId) {
        PreparedStatement stmt = null;

        try {
            // SQL query to update the issue status to "Resolved"
            String sql = "UPDATE Issues SET status = 'Resolved' WHERE issueId = ?";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, issueId);

            // Execute the update
            int rowsUpdated = stmt.executeUpdate();
            if (rowsUpdated > 0) {
                System.out.println("Issue with ID " + issueId + " marked as Resolved.");
                return true;
            } else {
                System.out.println("No issue found with ID: " + issueId + ". Update failed.");
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Creates a message for a specific user.
     *
     * @param userId The ID of the user to send the message to.
     * @param content The content of the message.
     * @param type The type of the message (e.g., Borrow, Return, Reservation).
     * @return {@code true} if the message was successfully created; {@code false} otherwise.
     */
    public static boolean createMessage(int userId, String content, String type) {
        PreparedStatement stmt = null;
        try {
            String sql = "INSERT INTO Messages (userId, content, type) VALUES (?, ?, ?)";
            stmt = connection.prepareStatement(sql);
            stmt.setInt(1, userId);
            stmt.setString(2, content);
            stmt.setString(3, type);

            int rowsInserted = stmt.executeUpdate();
            return rowsInserted > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
    
    /**
     * Fetches all messages associated with a specific user ID.
     *
     * <p>The messages are fetched based on the user's role (Subscriber or Librarian).
     * Librarians only receive messages of type "ExtensionNotification."</p>
     *
     * @param userId The ID of the user.
     * @return An {@link ArrayList} of {@link Messages} objects representing the user's messages.
     */
    public static ArrayList<Messages> fetchMessagesByUserId(int userId) {
        PreparedStatement checkUserRoleStmt = null;
        PreparedStatement fetchMessagesStmt = null;
        ResultSet roleRs = null;
        ResultSet messagesRs = null;
        ArrayList<Messages> messagesList = new ArrayList<>();

        try {
            // Step 1: Check if the user is a subscriber
            String checkUserRoleSql = "SELECT role FROM Users WHERE userId = ?";
            checkUserRoleStmt = connection.prepareStatement(checkUserRoleSql);
            checkUserRoleStmt.setInt(1, userId);
            roleRs = checkUserRoleStmt.executeQuery();

            if (roleRs.next()) {
                String userRole = roleRs.getString("role");

                if ("Subscriber".equalsIgnoreCase(userRole)) {
                    // Step 2: Fetch messages for the subscriber
                    String fetchSubscriberMessagesSql = "SELECT * FROM Messages WHERE userId = ? ORDER BY timestamp DESC";
                    fetchMessagesStmt = connection.prepareStatement(fetchSubscriberMessagesSql);
                    fetchMessagesStmt.setInt(1, userId);
                } else if ("Librarian".equalsIgnoreCase(userRole)) {
                    // Step 3: Fetch all extension notifications for librarians
                    String fetchLibrarianMessagesSql = "SELECT * FROM Messages WHERE type = 'ExtensionNotification' ORDER BY timestamp DESC";
                    fetchMessagesStmt = connection.prepareStatement(fetchLibrarianMessagesSql);
                } else {
                    // If the role is neither Subscriber nor Librarian, return an empty list
                    System.out.println("User with ID " + userId + " has an unsupported role: " + userRole);
                    return messagesList;
                }

                // Step 4: Execute the query and build the messages list
                messagesRs = fetchMessagesStmt.executeQuery();

                while (messagesRs.next()) {
                    Messages message = new Messages(
                        messagesRs.getInt("messageId"),
                        messagesRs.getInt("userId"),
                        messagesRs.getString("content"),
                        messagesRs.getTimestamp("timestamp").toLocalDateTime(),
                        messagesRs.getString("type")
                    );
                    messagesList.add(message);
                }
            } else {
                System.out.println("No user found with ID: " + userId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (roleRs != null) roleRs.close();
                if (messagesRs != null) messagesRs.close();
                if (checkUserRoleStmt != null) checkUserRoleStmt.close();
                if (fetchMessagesStmt != null) fetchMessagesStmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return messagesList;
    }
    
    /**
     * Fetches all reports of a specific type for a given month.
     *
     * @param reportType The type of report (e.g., Borrowing, Reservations).
     * @param reportMonth The month for which to fetch the reports.
     * @return An {@link ArrayList} of {@link Reports} objects representing the fetched reports.
     */

    public static ArrayList<Reports> fetchReports(String reportType, LocalDate reportMonth) {
        ArrayList<Reports> reportsList = new ArrayList<>();
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            // Ensure reportMonth is the first day of the month
            reportMonth = reportMonth.withDayOfMonth(1);

            String sql = "SELECT * FROM Reports WHERE reportType = ? AND reportMonth = ? ORDER BY category";
            stmt = connection.prepareStatement(sql);
            stmt.setString(1, reportType);
            stmt.setDate(2, java.sql.Date.valueOf(reportMonth));

            rs = stmt.executeQuery();

            while (rs.next()) {
                Reports report = new Reports(
                    rs.getInt("reportId"),
                    rs.getString("reportType"),
                    rs.getDate("reportMonth").toLocalDate(),
                    rs.getString("category"),
                    rs.getInt("value"),
                    rs.getTimestamp("createdAt").toLocalDateTime()
                );
                reportsList.add(report);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (rs != null) rs.close();
                if (stmt != null) stmt.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return reportsList;
    }

}

