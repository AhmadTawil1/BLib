/**
 * This enum represents various responses that the server can send back to the client.
 * Each response corresponds to a specific action requested by the client.
 */
package enums;

/**
 * Enumeration of server responses.
 * These responses indicate the result or data related to specific client actions.
 */
public enum ServerResponse {

    /** Response to a client's login request. */
    LOGIN_RESPONSE,

    /** Response containing the results of a book search. */
    SEARCH_RESULTS,

    /** Response with the list of books fetched from the library. */
    FETCH_BOOKS_RESPONSE,

    /** Response indicating the result of adding a book. */
    ADD_BOOK_RESPONSE,

    /** Response indicating the result of editing a book. */
    EDIT_BOOK_RESPONSE,

    /** Response indicating the result of deleting a book. */
    DELETE_BOOK_RESPONSE,

    /** Response with the list of subscribers fetched from the library. */
    FETCH_SUBSCRIBERS_RESPONSE,

    /** Response indicating the result of registering a subscriber. */
    REGISTER_SUBSCRIBER_RESPONSE,

    /** Response indicating the result of freezing a subscriber's account. */
    FREEZE_ACCOUNT_RESPONSE,

    /** Response indicating the result of unfreezing a subscriber's account. */
    UNFREEZE_ACCOUNT_RESPONSE,

    /** Response indicating the result of updating subscriber details. */
    UPDATE_SUBSCRIBER_DETAILS_RESPONSE,

    /** Response indicating the result of borrowing a book. */
    BORROW_BOOK_RESPONSE,

    /** Response with the list of books currently borrowed by the client. */
    FETCH_BORROWED_BOOKS_RESPONSE,

    /** Response indicating the server's connection status. */
    CHECK_CONNECTION_RESPONSE,

    /** Response indicating the result of getting a library card for a subscriber. */
    GET_CARD_RESPONSE,

    /** Response indicating the result of a book reservation. */
    RESERVATION_RESPONSE,

    /** Response with the list of reservations fetched for the client. */
    FETCH_RESERVATIONS_RESPONSE,

    /** Response indicating the result of returning a borrowed book. */
    RETURN_BOOK_RESPONSE,

    /** Response indicating the result of canceling a reservation. */
    CANCEL_RESERVATION_RESPONSE,

    /** Response indicating the result of a request to extend the borrow period of a book. */
    REQUEST_EXTENSION_RESPONSE,

    /** Response indicating the result of resolving a library usage issue. */
    RESOLVE_USAGE_ISSUE_RESPONSE,

    /** Response indicating the result of adding a new usage issue. */
    ADD_USAGE_ISSUE_RESPONSE,

    /** Response containing messages sent to the client. */
    FETCH_MESSAGES_RESPONSE,

    /** Response containing a report fetched from the library system. */
    FETCH_REPORT_RESPONSE,

    /** Response indicating the result of a logout request. */
    LOGOUT_REQUEST_RESPONSE
}
