/**
 * Represents a reservation made by a subscriber for a book in the library system.
 * This class provides details about the reservation, including the reserved book,
 * the subscriber, the reservation dates, and its current status.
 * It is part of the library management system's data model.
 */
package models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * The Reservation class represents a book reservation in the library system.
 * It contains attributes related to the reservation's identity, associated book,
 * subscriber, and its status.
 * Implements {@link Serializable} for transferring reservation data over a network
 * or saving it to persistent storage.
 */
public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for the reservation. */
    private int reservationId;

    /** ID of the reserved book. */
    private int bookId;

    /** Title of the reserved book. */
    private String bookTitle;

    /** ID of the subscriber who made the reservation. */
    private int subscriberId;

    /** Date when the reservation was made. */
    private LocalDate reservationDate;

    /** Date when the reservation will expire. */
    private LocalDate expirationDate;

    /** Status of the reservation (e.g., "Active", "Expired"). */
    private String status;

    /**
     * Constructs a new Reservation object with the specified details.
     *
     * @param reservationId   Unique ID of the reservation.
     * @param bookId          ID of the reserved book.
     * @param bookTitle       Title of the reserved book.
     * @param subscriberId    ID of the subscriber.
     * @param reservationDate Date of the reservation.
     * @param expirationDate  Expiration date of the reservation.
     * @param status          Status of the reservation (e.g., "Active", "Expired").
     */
    public Reservation(int reservationId, int bookId, String bookTitle, int subscriberId,
                       LocalDate reservationDate, LocalDate expirationDate, String status) {
        this.reservationId = reservationId;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
        this.subscriberId = subscriberId;
        this.reservationDate = reservationDate;
        this.expirationDate = expirationDate;
        this.status = status;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for the reservation.
     *
     * @return The reservation's unique identifier.
     */
    public int getReservationId() {
        return reservationId;
    }

    /**
     * Sets the unique identifier for the reservation.
     *
     * @param reservationId The reservation's unique identifier.
     */
    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    /**
     * Gets the ID of the reserved book.
     *
     * @return The ID of the reserved book.
     */
    public int getBookId() {
        return bookId;
    }

    /**
     * Sets the ID of the reserved book.
     *
     * @param bookId The ID of the reserved book.
     */
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    /**
     * Gets the title of the reserved book.
     *
     * @return The title of the reserved book.
     */
    public String getBookTitle() {
        return bookTitle;
    }

    /**
     * Sets the title of the reserved book.
     *
     * @param bookTitle The title of the reserved book.
     */
    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    /**
     * Gets the ID of the subscriber who made the reservation.
     *
     * @return The subscriber's ID.
     */
    public int getSubscriberId() {
        return subscriberId;
    }

    /**
     * Sets the ID of the subscriber who made the reservation.
     *
     * @param subscriberId The subscriber's ID.
     */
    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    /**
     * Gets the date when the reservation was made.
     *
     * @return The reservation date.
     */
    public LocalDate getReservationDate() {
        return reservationDate;
    }

    /**
     * Sets the date when the reservation was made.
     *
     * @param reservationDate The reservation date.
     */
    public void setReservationDate(LocalDate reservationDate) {
        this.reservationDate = reservationDate;
    }

    /**
     * Gets the expiration date of the reservation.
     *
     * @return The expiration date.
     */
    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    /**
     * Sets the expiration date of the reservation.
     *
     * @param expirationDate The expiration date.
     */
    public void setExpirationDate(LocalDate expirationDate) {
        this.expirationDate = expirationDate;
    }

    /**
     * Gets the status of the reservation.
     *
     * @return The reservation status (e.g., "Active", "Expired").
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the reservation.
     *
     * @param status The reservation status (e.g., "Active", "Expired").
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Checks if the reservation is expired.
     *
     * @return True if the reservation is expired, false otherwise.
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }

    /**
     * Returns a string representation of the reservation.
     *
     * @return A string containing reservation details.
     */
    @Override
    public String toString() {
        return "Reservation{" +
                "reservationId=" + reservationId +
                ", bookId=" + bookId +
                ", bookTitle='" + bookTitle + '\'' +
                ", subscriberId=" + subscriberId +
                ", reservationDate=" + reservationDate +
                ", expirationDate=" + expirationDate +
                ", status='" + status + '\'' +
                '}';
    }
}
