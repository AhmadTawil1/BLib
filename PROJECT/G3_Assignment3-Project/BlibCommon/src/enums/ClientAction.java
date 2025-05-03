/**
 * This enum represents various actions a client can perform in the library management system.
 * It defines all possible requests and operations a client can initiate within the system.
 */
package enums;
/**
 * Enumeration of client actions.
 * Each action corresponds to a specific request or operation performed by the client.
 */
public enum ClientAction {
    /** Request to log into the system. */
	LOGIN_REQUEST,
    /** Fetch the list of books from the library. */
	FETCH_BOOKS,
    /** Add a new book to the library system. */
	ADD_BOOK,
    /** Search for books in the library system. */
	SEARCH_BOOKS,
    /** Delete a book from the library system. */
	DELETE_BOOK,
    /** Edit details of a book in the library system. */
	EDIT_BOOK,
    /** Fetch the list of subscribers in the library system. */
	FETCH_SUBSCRIBERS,
    /** Freeze a subscriber's account. */
	FREEZE_ACCOUNT,
    /** Unfreeze a subscriber's account. */
	UNFREEZE_ACCOUNT,
    /** Update the details of a subscriber. */
	UPDATE_SUBSCRIBER_DETAILS,
    /** Borrow a book from the library. */
	BORROW_BOOK,   
	/** Fetch the list of books currently borrowed by the client. */
	FETCH_BORROWED_BOOKS,
    /** Check the status of the server connection. */
	CHECK_CONNECTION, 
    /** Register a new subscriber to the library system. */
	REGISTER_SUBSCRIBER, 
    /** Get a library card for a subscriber. */
	GET_CARD,
    /** Reserve a book in the library. */
	RESERVE_BOOK,
    /** Fetch the list of reservations made by the client. */
	FETCH_RESERVATIONS,
    /** Request to return a borrowed book. */
	RETURN_REQUEST,
    /** Cancel a reservation made by the client. */
	CANCEL_RESERVATION,
    /** Request an extension for a borrowed book. */
	REQUEST_EXTENSION, REQUEST_EXTENSION1,
    /** Resolve an issue regarding library usage. */
	RESOLVE_USAGE_ISSUE,
    /** Add a new usage issue for resolution. */
	ADD_USAGE_ISSUE,
    /** Fetch messages sent to the client. */
	FETCH_MESSAGES,
    /** Fetch a report from the library system. */
	FETCH_REPORT,
    /** Request to log out from the system. */
	LOGOUT_REQUEST,
}
