package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SubscriberCard implements Serializable{
	private static final long serialVersionUID = 1L;
	private int cardId;
	private String subscriptionNumber;             // Same as in Subscriber
	private String accountStatus;                 // Account status: "Active" or "Frozen"
	private List<BorrowRecord> borrowingHistory;  // List of BorrowRecords
	private List<String> usageIssues;             // List of usage issues
	private LocalDate frozenUntil;                // If frozen, until when
	
	
	public SubscriberCard(int cardId, String subscriptionNumber, String accountStatus) {
	    this.cardId = cardId;
	    this.subscriptionNumber = subscriptionNumber;
	    this.accountStatus = accountStatus;
	    this.borrowingHistory = new ArrayList<>();
	    this.usageIssues = new ArrayList<>();
	}

	// Getters and Setters
	public int getCardId() {
	    return cardId;
	}

	public void setCardId(int cardId) {
	    this.cardId = cardId;
	}

	public String getSubscriptionNumber() {
	    return subscriptionNumber;
	}

	public void setSubscriptionNumber(String subscriptionNumber) {
	    this.subscriptionNumber = subscriptionNumber;
	}

	public String getAccountStatus() {
	    return accountStatus;
	}

	public void setAccountStatus(String accountStatus) {
	    this.accountStatus = accountStatus;
	}

	public List<BorrowRecord> getBorrowingHistory() {
	    return borrowingHistory;
	}

	public void setBorrowingHistory(List<BorrowRecord> borrowingHistory) {
	    this.borrowingHistory = borrowingHistory;
	}

	public List<String> getUsageIssues() {
	    return usageIssues;
	}

	public void setUsageIssues(List<String> usageIssues) {
	    this.usageIssues = usageIssues;
	}

	public LocalDate getFrozenUntil() {
	    return frozenUntil;
	}

	public void setFrozenUntil(LocalDate frozenUntil) {
	    this.frozenUntil = frozenUntil;
	}
	// Utility Methods
	public void addBorrowRecord(BorrowRecord record) {
	    this.borrowingHistory.add(record);
	}

	public void addUsageIssue(String issue) {
	    this.usageIssues.add(issue);
	}

	public void clearUsageIssues() {
	    this.usageIssues.clear();
	}

	@Override
	public String toString() {
	    return "SubscriberCard{" +
	            "cardId=" + cardId +
	            ", subscriptionNumber='" + subscriptionNumber + '\'' +
	            ", accountStatus='" + accountStatus + '\'' +
	            ", borrowingHistory=" + borrowingHistory +
	            ", usageIssues=" + usageIssues +
	            ", frozenUntil=" + frozenUntil +
	            '}';
	}
}
