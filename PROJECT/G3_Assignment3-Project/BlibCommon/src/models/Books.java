/**
 * Represents a book in the library system.
 * This class provides details about a book, including its unique identifier,
 * title, author(s), subject, description, location, and availability information.
 * It is used as part of the library management system's data model.
 */
package models;

import java.io.Serializable;
import java.time.LocalDate;
/**
 * The Books class represents a book in the library.
 * It contains attributes related to a book's identity, description, and availability.
 * Implements {@link Serializable} for transferring book data over a network or saving it to persistent storage.
 */
public class Books  implements Serializable{
	private static final long serialVersionUID = 1L;
    /** Unique identifier for the book. */
    private int bookId;

    /** Title of the book. */
    private String title;

    /** Author(s) of the book. */
    private String author;

    /** Subject or category of the book. */
    private String subject;

    /** A short description of the book. */
    private String description;

    /** The location of the book in the library (e.g., shelf number). */
    private String location;

    /** The number of copies currently available in the library. */
    private int availableCopies;

    /** Unique barcode associated with the book. */
    private String barcode;

    /** The nearest return date for the book, if borrowed. */
    private LocalDate nearestReturnDate;

    /**
     * Constructs a new Books object with the specified details.
     *
     * @param bookId         Unique identifier for the book.
     * @param title          Title of the book.
     * @param author         Author(s) of the book.
     * @param subject        Subject or category of the book.
     * @param description    A short description of the book.
     * @param location       Location of the book in the library.
     * @param availableCopies Number of copies currently available.
     * @param barcode        Unique barcode associated with the book.
     */
	public Books(int bookId, String title, String author, String subject, String description, String location, int availableCopies, String barcode) {
	    this.bookId = bookId;
	    this.title = title;
	    this.author = author;
	    this.subject = subject;
	    this.description = description;
	    this.location = location;
	    this.availableCopies = availableCopies;
	    this.barcode = barcode;
	}
	// Getters and Setters

    /**
     * Gets the unique identifier for the book.
     *
     * @return The book's unique identifier.
     */
    public int getBookId() {
        return bookId;
    }
    /**
     * Sets the unique identifier for the book.
     *
     * @param bookId The book's unique identifier.
     */
    public void setBookId(int bookId) {
        this.bookId = bookId;
    }

    /**
     * Gets the title of the book.
     *
     * @return The title of the book.
     */
    public String getTitle() {
        return title;
    }

    /**
     * Sets the title of the book.
     *
     * @param title The title of the book.
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * Gets the author(s) of the book.
     *
     * @return The author(s) of the book.
     */
    public String getAuthor() {
        return author;
    }

    /**
     * Sets the author(s) of the book.
     *
     * @param author The author(s) of the book.
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * Gets the subject or category of the book.
     *
     * @return The subject of the book.
     */
    public String getSubject() {
        return subject;
    }

    /**
     * Sets the subject or category of the book.
     *
     * @param subject The subject of the book.
     */
    public void setSubject(String subject) {
        this.subject = subject;
    }

    /**
     * Gets the location of the book in the library.
     *
     * @return The location of the book.
     */
    public String getLocation() {
        return location;
    }

    /**
     * Sets the location of the book in the library.
     *
     * @param location The location of the book.
     */
    public void setLocation(String location) {
        this.location = location;
    }

    /**
     * Gets the number of copies currently available for the book.
     *
     * @return The number of available copies.
     */
    public int getAvailableCopies() {
        return availableCopies;
    }

    /**
     * Sets the number of copies currently available for the book.
     *
     * @param availableCopies The number of available copies.
     */
    public void setAvailableCopies(int availableCopies) {
        this.availableCopies = availableCopies;
    }

    /**
     * Gets the unique barcode associated with the book.
     *
     * @return The barcode of the book.
     */
    public String getBarcode() {
        return barcode;
    }

    /**
     * Sets the unique barcode associated with the book.
     *
     * @param barcode The barcode of the book.
     */
    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    /**
     * Gets a short description of the book.
     *
     * @return The description of the book.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets a short description of the book.
     *
     * @param description The description of the book.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the nearest return date for the book, if borrowed.
     *
     * @return The nearest return date.
     */
    public LocalDate getNearestReturnDate() {
        return nearestReturnDate;
    }

    /**
     * Sets the nearest return date for the book.
     *
     * @param nearestReturnDate The nearest return date.
     */
    public void setNearestReturnDate(LocalDate nearestReturnDate) {
        this.nearestReturnDate = nearestReturnDate;
    }
}
