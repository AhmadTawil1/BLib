/**
 * Represents a borrowing record in the library system.
 * This class provides details about a specific borrowing transaction, 
 * including the subscriber, book, and relevant dates.
 * It is used as part of the library management system's data model.
 */
package models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * The BorrowRecord class represents a borrowing transaction in the library.
 * It contains attributes related to the borrowing process, including the subscriber, 
 * the borrowed book, and the borrowing/return dates.
 * Implements {@link Serializable} for transferring borrowing data over a network 
 * or saving it to persistent storage.
 */
public class BorrowRecord implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique borrowing record ID. */
    private int recordId;

    /** ID of the subscriber who borrowed the book. */
    private int subscriberId;

    /** ID of the borrowed book. */
    private int bookId;

    /** Book object associated with this borrowing record (optional). */
    private Books book;

    /** Date the book was borrowed. */
    private LocalDate borrowDate;

    /** Due date for returning the book. */
    private LocalDate dueDate;

    /** Actual date the book was returned (nullable). */
    private LocalDate returnDate;

    /** Status of the borrowing record ("Active", "Extended", etc.). */
    private String status;

    /**
     * Constructs a new BorrowRecord object with the specified details.
     *
     * @param recordId     Unique borrowing record ID.
     * @param subscriberId ID of the subscriber who borrowed the book.
     * @param bookId       ID of the borrowed book.
     * @param borrowDate   Date the book was borrowed.
     * @param dueDate      Due date for returning the book.
     * @param returnDate   Date the book was returned (nullable).
     * @param status       Status of the borrowing record ("Active" or "Extended").
     */
    public BorrowRecord(int recordId, int subscriberId, int bookId, LocalDate borrowDate,
                        LocalDate dueDate, LocalDate returnDate, String status) {
        this.recordId = recordId;
        this.subscriberId = subscriberId;
        this.bookId = bookId;
        this.borrowDate = borrowDate;
        this.dueDate = dueDate;
        this.returnDate = returnDate;
        this.status = status;
        this.book = null; // Book can be set later
    }

    // Getters and Setters

    /**
     * Gets the unique borrowing record ID.
     *
     * @return The borrowing record ID.
     */
    public int getRecordId() {
        return recordId;
    }

    /**
     * Sets the unique borrowing record ID.
     *
     * @param recordId The borrowing record ID.
     */
    public void setRecordId(int recordId) {
        this.recordId = recordId;
    }

    /**
     * Gets the ID of the subscriber who borrowed the book.
     *
     * @return The subscriber ID.
     */
    public int getSubscriberId() {
        return subscriberId;
    }

    /**
     * Sets the ID of the subscriber who borrowed the book.
     *
     * @param subscriberId The subscriber ID.
     */
    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    /**
     * Gets the ID of the borrowed book.
     *
     * @return The book ID.
     */
    public int getBookId() {
        return bookId;
    }

    /**
     * Sets the ID of the borrowed book.
     *
     * @param bookId The book ID.
     */
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    /**
     * Gets the Book object associated with this borrowing record.
     *
     * @return The associated Book object.
     */
    public Books getBook() {
        return book;
    }

    /**
     * Sets the Book object associated with this borrowing record.
     *
     * @param book The Book object to associate.
     */
    public void setBook(Books book) {
        this.book = book;
    }

    /**
     * Gets the borrowing date of the book.
     *
     * @return The borrowing date.
     */
    public LocalDate getBorrowDate() {
        return borrowDate;
    }

    /**
     * Sets the borrowing date of the book.
     *
     * @param borrowDate The borrowing date.
     */
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
    }

    /**
     * Gets the due date for returning the book.
     *
     * @return The due date.
     */
    public LocalDate getDueDate() {
        return dueDate;
    }

    /**
     * Sets the due date for returning the book.
     *
     * @param dueDate The due date.
     */
    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    /**
     * Gets the actual return date of the book.
     *
     * @return The return date.
     */
    public LocalDate getReturnDate() {
        return returnDate;
    }

    /**
     * Sets the actual return date of the book.
     *
     * @param returnDate The return date.
     */
    public void setReturnDate(LocalDate returnDate) {
        this.returnDate = returnDate;
    }

    /**
     * Gets the status of the borrowing record.
     *
     * @return The borrowing record status ("Active", "Extended", etc.).
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the status of the borrowing record.
     *
     * @param status The borrowing record status ("Active", "Extended").
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Checks if the book is currently overdue.
     *
     * @return True if the due date has passed and the book is not returned, false otherwise.
     */
    public boolean isOverdue() {
        return returnDate == null && LocalDate.now().isAfter(dueDate);
    }

    /**
     * Updates the status of the borrowing record.
     *
     * @param status New status for the record ("Active", "Extended").
     */
    public void updateStatus(String status) {
        this.status = status;
    }

    /**
     * Returns a string representation of the borrowing record.
     *
     * @return A string containing borrowing record details.
     */
    @Override
    public String toString() {
        return "BorrowRecord{" +
                "recordId=" + recordId +
                ", subscriberId=" + subscriberId +
                ", bookId=" + bookId +
                ", book=" + (book != null ? book.getTitle() : "null") +
                ", borrowDate=" + borrowDate +
                ", dueDate=" + dueDate +
                ", returnDate=" + returnDate +
                ", status='" + status + '\'' +
                '}';
    }
}
