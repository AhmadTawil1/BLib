/**
 * Represents a message in the library system.
 * This class provides details about a specific message, including its unique identifier,
 * the associated user, content, timestamp, and type.
 * It is part of the library management system's data model.
 */
package models;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * The Messages class represents a communication or notification in the library system.
 * It contains attributes related to the message's identity, content, timestamp, and type.
 * Implements {@link Serializable} for transferring message data over a network
 * or saving it to persistent storage.
 */
public class Messages implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for the message. */
    private int messageId;

    /** ID of the user (subscriber) associated with the message. */
    private int userId;

    /** Content of the message. */
    private String content;

    /** Timestamp indicating when the message was sent. */
    private LocalDateTime timestamp;

    /** Type of message (e.g., "Request", "Notification"). */
    private String type;

    /**
     * Constructs a new Messages object with the specified details.
     *
     * @param messageId  Unique identifier for the message.
     * @param userId     ID of the user (subscriber) associated with the message.
     * @param content    Content of the message.
     * @param timestamp  Timestamp indicating when the message was sent.
     * @param type       Type of message (e.g., "Request", "Notification").
     */
    public Messages(int messageId, int userId, String content, LocalDateTime timestamp, String type) {
        this.messageId = messageId;
        this.userId = userId;
        this.content = content;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for the message.
     *
     * @return The message's unique identifier.
     */
    public int getMessageId() {
        return messageId;
    }

    /**
     * Sets the unique identifier for the message.
     *
     * @param messageId The message's unique identifier.
     */
    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    /**
     * Gets the ID of the user (subscriber) associated with the message.
     *
     * @return The user ID associated with the message.
     */
    public int getSubscriberId() {
        return userId;
    }

    /**
     * Sets the ID of the user (subscriber) associated with the message.
     *
     * @param subscriberId The user ID associated with the message.
     */
    public void setSubscriberId(int subscriberId) {
        this.userId = subscriberId;
    }

    /**
     * Gets the content of the message.
     *
     * @return The content of the message.
     */
    public String getContent() {
        return content;
    }

    /**
     * Sets the content of the message.
     *
     * @param content The content of the message.
     */
    public void setContent(String content) {
        this.content = content;
    }

    /**
     * Gets the timestamp indicating when the message was sent.
     *
     * @return The timestamp of the message.
     */
    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    /**
     * Sets the timestamp indicating when the message was sent.
     *
     * @param timestamp The timestamp of the message.
     */
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    /**
     * Gets the type of the message (e.g., "Request", "Notification").
     *
     * @return The type of the message.
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type of the message (e.g., "Request", "Notification").
     *
     * @param type The type of the message.
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Returns a string representation of the message.
     *
     * @return A string containing the message's details.
     */
    @Override
    public String toString() {
        return "Message{" +
                "messageId=" + messageId +
                ", subscriberId=" + userId +
                ", content='" + content + '\'' +
                ", timestamp=" + timestamp +
                ", type='" + type + '\'' +
                '}';
    }
}
