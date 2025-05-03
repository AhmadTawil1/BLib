/**
 * Represents a subscriber in the library system.
 * This class contains information about the subscriber, including contact details,
 * account status, borrowing history, reservation history, and reported issues.
 * It is part of the library management system's data model.
 */
package models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * The Subscriber class represents a library subscriber.
 * It contains attributes related to the subscriber's identity, contact details,
 * account status, borrowing history, reservation history, and usage issues.
 * Implements {@link Serializable} for transferring subscriber data over a network
 * or saving it to persistent storage.
 */
public class Subscriber implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for the subscriber. */
    private int subscriberId;

    /** Subscription number associated with the subscriber. */
    private String subscriptionNumber;

    /** Email address of the subscriber. */
    private String email;

    /** Mobile phone number of the subscriber. */
    private String mobilePhoneNumber;

    /** Current account status of the subscriber (e.g., "Active", "Frozen"). */
    private String accountStatus;

    /** List of borrowing records associated with the subscriber. */
    private List<BorrowRecord> borrowingHistory;

    /** List of reservations made by the subscriber. */
    private List<Reservation> reservationHistory;

    /** List of usage issues reported by the subscriber. */
    private List<Issues> usageIssues;

    /**
     * Constructs a new Subscriber object with the specified details.
     *
     * @param subscriberId       Unique identifier for the subscriber.
     * @param subscriptionNumber Subscription number of the subscriber.
     * @param email              Email address of the subscriber.
     * @param mobilePhoneNumber  Mobile phone number of the subscriber.
     * @param accountStatus      Current account status of the subscriber.
     */
    public Subscriber(int subscriberId, String subscriptionNumber, String email, String mobilePhoneNumber, String accountStatus) {
        this.subscriberId = subscriberId;
        this.subscriptionNumber = subscriptionNumber;
        this.email = email;
        this.mobilePhoneNumber = mobilePhoneNumber;
        this.accountStatus = accountStatus;
        this.borrowingHistory = new ArrayList<>();
        this.reservationHistory = new ArrayList<>();
        this.usageIssues = new ArrayList<>();
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for the subscriber.
     *
     * @return The subscriber's unique identifier.
     */
    public int getSubscriberId() {
        return subscriberId;
    }

    /**
     * Sets the unique identifier for the subscriber.
     *
     * @param subscriberId The subscriber's unique identifier.
     */
    public void setSubscriberId(int subscriberId) {
        this.subscriberId = subscriberId;
    }

    /**
     * Gets the subscription number of the subscriber.
     *
     * @return The subscription number of the subscriber.
     */
    public String getSubscriptionNumber() {
        return subscriptionNumber;
    }

    /**
     * Sets the subscription number of the subscriber.
     *
     * @param subscriptionNumber The subscription number of the subscriber.
     */
    public void setSubscriptionNumber(String subscriptionNumber) {
        this.subscriptionNumber = subscriptionNumber;
    }

    /**
     * Gets the email address of the subscriber.
     *
     * @return The email address of the subscriber.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the subscriber.
     *
     * @param email The email address of the subscriber.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the mobile phone number of the subscriber.
     *
     * @return The mobile phone number of the subscriber.
     */
    public String getMobilePhoneNumber() {
        return mobilePhoneNumber;
    }

    /**
     * Sets the mobile phone number of the subscriber.
     *
     * @param mobilePhoneNumber The mobile phone number of the subscriber.
     */
    public void setMobilePhoneNumber(String mobilePhoneNumber) {
        this.mobilePhoneNumber = mobilePhoneNumber;
    }

    /**
     * Gets the current account status of the subscriber.
     *
     * @return The account status of the subscriber.
     */
    public String getAccountStatus() {
        return accountStatus;
    }

    /**
     * Sets the current account status of the subscriber.
     *
     * @param accountStatus The account status of the subscriber.
     */
    public void setAccountStatus(String accountStatus) {
        this.accountStatus = accountStatus;
    }

    /**
     * Gets the borrowing history of the subscriber.
     *
     * @return The list of borrowing records.
     */
    public List<BorrowRecord> getBorrowingHistory() {
        return borrowingHistory;
    }

    /**
     * Sets the borrowing history of the subscriber.
     *
     * @param borrowingHistory The list of borrowing records.
     */
    public void setBorrowingHistory(List<BorrowRecord> borrowingHistory) {
        this.borrowingHistory = borrowingHistory;
    }

    /**
     * Gets the reservation history of the subscriber.
     *
     * @return The list of reservations made by the subscriber.
     */
    public List<Reservation> getReservationHistory() {
        return reservationHistory;
    }

    /**
     * Sets the reservation history of the subscriber.
     *
     * @param reservationHistory The list of reservations made by the subscriber.
     */
    public void setReservationHistory(List<Reservation> reservationHistory) {
        this.reservationHistory = reservationHistory;
    }

    /**
     * Gets the usage issues reported by the subscriber.
     *
     * @return The list of usage issues reported by the subscriber.
     */
    public List<Issues> getUsageIssues() {
        return usageIssues;
    }

    /**
     * Sets the usage issues reported by the subscriber.
     *
     * @param usageIssues The list of usage issues reported by the subscriber.
     */
    public void setUsageIssues(List<Issues> usageIssues) {
        this.usageIssues = usageIssues;
    }

    /**
     * Adds a usage issue to the subscriber's list of reported issues.
     *
     * @param issue The usage issue to add.
     */
    public void addUsageIssue(Issues issue) {
        usageIssues.add(issue);
    }

    /**
     * Adds a borrowing record to the subscriber's borrowing history.
     *
     * @param record The borrowing record to add.
     */
    public void addBorrowingRecord(BorrowRecord record) {
        borrowingHistory.add(record);
    }

    /**
     * Returns a string representation of the subscriber.
     *
     * @return A string containing the subscriber's details.
     */
    @Override
    public String toString() {
        return "Subscriber{" +
                "subscriberId=" + subscriberId +
                ", subscriptionNumber='" + subscriptionNumber + '\'' +
                ", email='" + email + '\'' +
                ", mobilePhoneNumber='" + mobilePhoneNumber + '\'' +
                ", accountStatus='" + accountStatus + '\'' +
                ", borrowingHistory=" + borrowingHistory +
                ", reservationHistory=" + reservationHistory +
                ", usageIssues=" + usageIssues +
                '}';
    }
}
