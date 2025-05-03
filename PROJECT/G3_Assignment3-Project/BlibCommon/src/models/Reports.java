/**
 * Represents a report in the library system.
 * This class provides details about reports generated for tracking borrowing times, 
 * subscription statuses, or other metrics within the library system.
 * It is part of the library management system's data model.
 */
package models;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * The Reports class represents a system-generated report.
 * It contains attributes related to the report's identity, type, category, and value,
 * as well as metadata such as the creation timestamp and reporting month.
 * Implements {@link Serializable} for transferring report data over a network 
 * or saving it to persistent storage.
 */
public class Reports implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Unique identifier for the report. */
    private int reportId;

    /** The type of report (e.g., "Borrowing Times", "Subscription Status"). */
    private String reportType;

    /** The month for which the report was generated. */
    private LocalDate reportMonth;

    /** The category of the report (e.g., "1–7 days", "Active", etc.). */
    private String category;

    /** The numerical value or count related to the report (e.g., number of records). */
    private int value;

    /** Timestamp indicating when the report was created. */
    private LocalDateTime createdAt;

    /**
     * Constructs a new Reports object with the specified details.
     *
     * @param reportId   Unique identifier for the report.
     * @param reportType The type of report (e.g., "Borrowing Times", "Subscription Status").
     * @param reportMonth The month for which the report was generated.
     * @param category    The category of the report (e.g., "1–7 days", "Active").
     * @param value       The numerical value or count related to the report.
     * @param createdAt   Timestamp indicating when the report was created.
     */
    public Reports(int reportId, String reportType, LocalDate reportMonth, String category, int value, LocalDateTime createdAt) {
        this.reportId = reportId;
        this.reportType = reportType;
        this.reportMonth = reportMonth;
        this.category = category;
        this.value = value;
        this.createdAt = createdAt;
    }

    // Getters and Setters

    /**
     * Gets the unique identifier for the report.
     *
     * @return The report's unique identifier.
     */
    public int getReportId() {
        return reportId;
    }

    /**
     * Sets the unique identifier for the report.
     *
     * @param reportId The report's unique identifier.
     */
    public void setReportId(int reportId) {
        this.reportId = reportId;
    }

    /**
     * Gets the type of the report.
     *
     * @return The report type (e.g., "Borrowing Times", "Subscription Status").
     */
    public String getReportType() {
        return reportType;
    }

    /**
     * Sets the type of the report.
     *
     * @param reportType The report type (e.g., "Borrowing Times", "Subscription Status").
     */
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    /**
     * Gets the month for which the report was generated.
     *
     * @return The reporting month.
     */
    public LocalDate getReportMonth() {
        return reportMonth;
    }

    /**
     * Sets the month for which the report was generated.
     *
     * @param reportMonth The reporting month.
     */
    public void setReportMonth(LocalDate reportMonth) {
        this.reportMonth = reportMonth;
    }

    /**
     * Gets the category of the report.
     *
     * @return The report category (e.g., "1–7 days", "Active").
     */
    public String getCategory() {
        return category;
    }

    /**
     * Sets the category of the report.
     *
     * @param category The report category (e.g., "1–7 days", "Active").
     */
    public void setCategory(String category) {
        this.category = category;
    }

    /**
     * Gets the numerical value or count related to the report.
     *
     * @return The value or count of the report.
     */
    public int getValue() {
        return value;
    }

    /**
     * Sets the numerical value or count related to the report.
     *
     * @param value The value or count of the report.
     */
    public void setValue(int value) {
        this.value = value;
    }

    /**
     * Gets the timestamp indicating when the report was created.
     *
     * @return The creation timestamp of the report.
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the timestamp indicating when the report was created.
     *
     * @param createdAt The creation timestamp of the report.
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns a string representation of the report.
     *
     * @return A string containing the report's details.
     */
    @Override
    public String toString() {
        return "Reports{" +
                "reportId=" + reportId +
                ", reportType='" + reportType + '\'' +
                ", reportMonth=" + reportMonth +
                ", category='" + category + '\'' +
                ", value=" + value +
                ", createdAt=" + createdAt +
                '}';
    }
}
