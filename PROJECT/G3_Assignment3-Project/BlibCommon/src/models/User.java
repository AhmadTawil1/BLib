/**
 * Represents a user in the library system.
 * A user can either be a subscriber or a librarian, with an associated role, username, and password.
 * If the user is a subscriber, they are linked to a {@link Subscriber} object.
 * This class is part of the library management system's data model.
 */
package models;

import java.io.Serializable;

/**
 * The User class represents a library system user.
 * It contains attributes related to the user's identity, role, and credentials,
 * along with an optional association to a {@link Subscriber}.
 * Implements {@link Serializable} for transferring user data over a network
 * or saving it to persistent storage.
 */
public class User implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for the user. */
    private int userId;

    /** Login username for the user. */
    private String username;

    /** Hashed password for the user. */
    private String password;

    /** Role of the user (e.g., "Subscriber", "Librarian"). */
    private String role;

    /** Associated Subscriber object (if the user is a Subscriber). */
    private Subscriber subscriber;

    /**
     * Constructs a new User object with the specified details.
     *
     * @param userId     Unique user ID.
     * @param username   Login username.
     * @param password   Hashed password for the user.
     * @param role       Role of the user ("Subscriber" or "Librarian").
     * @param subscriber Associated Subscriber object (can be null for Librarians).
     */
    public User(int userId, String username, String password, String role, Subscriber subscriber) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.role = role;
        this.subscriber = subscriber;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for the user.
     *
     * @return The user's unique ID.
     */
    public int getUserId() {
        return userId;
    }

    /**
     * Sets the unique identifier for the user.
     *
     * @param userId The user's unique ID.
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * Gets the login username of the user.
     *
     * @return The user's username.
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the login username of the user.
     *
     * @param username The user's username.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the hashed password of the user.
     *
     * @return The user's password.
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the hashed password of the user.
     *
     * @param password The user's password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the role of the user.
     *
     * @return The user's role (e.g., "Subscriber", "Librarian").
     */
    public String getRole() {
        return role;
    }

    /**
     * Sets the role of the user.
     *
     * @param role The user's role (e.g., "Subscriber", "Librarian").
     */
    public void setRole(String role) {
        this.role = role;
    }

    /**
     * Gets the associated Subscriber object.
     *
     * @return The associated Subscriber object, or null if the user is a Librarian.
     */
    public Subscriber getSubscriber() {
        return subscriber;
    }

    /**
     * Sets the associated Subscriber object.
     *
     * @param subscriber The associated Subscriber object.
     */
    public void setSubscriber(Subscriber subscriber) {
        this.subscriber = subscriber;
    }

    /**
     * Checks if the user is a subscriber.
     *
     * @return True if the user's role is "Subscriber", false otherwise.
     */
    public boolean isSubscriber() {
        return "Subscriber".equalsIgnoreCase(role);
    }

    /**
     * Returns a string representation of the user.
     *
     * @return A string containing user details.
     */
    @Override
    public String toString() {
        return "User{" +
                "userId=" + userId +
                ", username='" + username + '\'' +
                ", role='" + role + '\'' +
                ", subscriber=" + subscriber +
                '}';
    }
}
