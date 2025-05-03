/**
 * Represents an issue reported by a subscriber in the library system.
 * This class provides details about the issue, including its unique identifier,
 * the subscriber who reported it, the description, and its current status.
 * It is part of the library management system's data model.
 */
package models;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * The Issues class represents a reported issue in the library system.
 * It contains attributes related to the issue's identity, description,
 * the subscriber who reported it, and its current status.
 * Implements {@link Serializable} for transferring issue data over a network 
 * or saving it to persistent storage.
 */
public class Issues implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for the issue. */
    private int issueId;

    /** ID of the subscriber who reported the issue. */
    private int subscriberId;

    /** A description of the issue reported by the subscriber. */
    private String description;

    /** Date the issue was reported. */
    private LocalDate dateReported;

    /** Current status of the issue (e.g., "Open", "Resolved"). */
    private String status;

    /**
     * Constructs a new Issues object with the specified details.
     *
     * @param issueId       Unique identifier for the issue.
     * @param subscriberId  ID of the subscriber who reported the issue.
     * @param description   A description of the issue.
     * @param dateReported  The date the issue was reported.
     * @param status        Current status of the issue (e.g., "Open", "Resolved").
     */
    public Issues(int issueId, int subscriberId, String description, LocalDate dateReported, String status) {
        this.issueId = issueId;
        this.subscriberId = subscriberId;
        this.description = description;
        this.dateReported = dateReported;
        this.status = status;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for the issue.
     *
     * @return The issue's unique identifier.
     */
    public int getIssueId() {
        return issueId;
    }

    /**
     * Sets the unique identifier for the issue.
     *
     * @param issueId The issue's unique identifier.
     */
    public void setIssueId(int issueId) {
        this.issueId = issueId;
    }

    /**
     * Gets the ID of the subscriber who reported the issue.
     *
     * @return The subscriber's ID.
     */
    public int getSubscriberId() {
        return subscriberId;
    }

    /**
     * Sets the ID of the subscriber who reported the issue.
     *
     * @param subscriberId The subscriber's ID.
     */
    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    /**
     * Gets the description of the issue.
     *
     * @return A description of the issue.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the issue.
     *
     * @param description A description of the issue.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the date the issue was reported.
     *
     * @return The date the issue was reported.
     */
    public LocalDate getDateReported() {
        return dateReported;
    }

    /**
     * Sets the date the issue was reported.
     *
     * @param dateReported The date the issue was reported.
     */
    public void setDateReported(LocalDate dateReported) {
        this.dateReported = dateReported;
    }

    /**
     * Gets the current status of the issue.
     *
     * @return The current status of the issue (e.g., "Open", "Resolved").
     */
    public String getStatus() {
        return status;
    }

    /**
     * Sets the current status of the issue.
     *
     * @param status The current status of the issue (e.g., "Open", "Resolved").
     */
    public void setStatus(String status) {
        this.status = status;
    }

    /**
     * Returns a string representation of the issue.
     *
     * @return A string containing the issue's ID, description, and status.
     */
    @Override
    public String toString() {
        return "Issue [ID: " + issueId + ", Description: " + description + ", Status: " + status + "]";
    }
}
