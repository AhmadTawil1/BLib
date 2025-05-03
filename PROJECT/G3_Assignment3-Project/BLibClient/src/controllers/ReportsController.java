package controllers;

import client.ClientControllerNew;
import enums.ClientAction;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.scene.SnapshotParameters;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.util.Callback;
import javafx.util.StringConverter;
import models.Reports;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.imageio.ImageIO;


/**
 * The {@code ReportsController} class is responsible for managing the 
 * generation, visualization, and export of library-related reports.
 * 
 * <p>
 * The controller supports two types of reports:
 * <ul>
 *     <li>Borrowing Times Report: Displays the frequency of book borrowings.</li>
 *     <li>Subscription Status Report: Displays the current subscription statuses of users.</li>
 * </ul>
 * </p>
 * 
 * <p>
 * Features include:
 * <ul>
 *     <li>Generating reports based on a selected month.</li>
 *     <li>Displaying data using bar charts and pie charts.</li>
 *     <li>Exporting reports as images or CSV files.</li>
 * </ul>
 * </p>
 * 
 * 
 * @version 1.0
 */
public class ReportsController {

    @FXML
    private DatePicker monthPicker;

    @FXML
    private ChoiceBox<String> reportChoiceBox;

    @FXML
	public BarChart<String, Number> borrowingTimesBarChart;

    @FXML
    private PieChart subscriptionStatusPieChart;

    @FXML
    private StackPane chartContainer;

    private final DateTimeFormatter monthYearFormatter = DateTimeFormatter.ofPattern("MM/yyyy");

    /**
     * Initializes the {@code ReportsController}. Sets up UI components, binds 
     * the controller to the client, and configures default settings for charts 
     * and controls.
     */
    public void initialize() {
    	// Link the controller to the client
        ClientControllerNew.getInstance().getChatClient().setReportsController(this);

        // Configure the month picker to allow selection of month and year only
        configureMonthPicker();

        // Populate the ChoiceBox with report options
        reportChoiceBox.getItems().addAll("Borrowing Times Report", "Subscription Status Report");
        reportChoiceBox.getSelectionModel().selectFirst();

        // Add listener to switch reports dynamically based on the selected option
        reportChoiceBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            updateReportVisibility(newValue);
        });

        // Initialize charts as invisible by default
        borrowingTimesBarChart.setVisible(false);
        subscriptionStatusPieChart.setVisible(false);
    }

    /**
     * Configures the {@link DatePicker} to allow selection of only the month and year.
     * The selected date is automatically adjusted to the first day of the month.
     */
    private void configureMonthPicker() {
        monthPicker.setConverter(new StringConverter<LocalDate>() { // Explicitly specify LocalDate
            @Override
            public String toString(LocalDate date) {
                return (date != null) ? date.format(monthYearFormatter) : "";
            }

            @Override
            public LocalDate fromString(String string) {
                return (string != null && !string.isEmpty()) ? LocalDate.parse(string, monthYearFormatter) : null;
            }
        });

        monthPicker.setDayCellFactory(new Callback<DatePicker, DateCell>() { // Explicitly specify types
            @Override
            public DateCell call(DatePicker param) {
                return new DateCell() {
                    @Override
                    public void updateItem(LocalDate date, boolean empty) {
                        super.updateItem(date, empty);
                        setDisable(date.getDayOfMonth() != 1); // Disable all dates except the first day
                    }
                };
            }
        });

        // Ensure the selected date always defaults to the first day of the month
        monthPicker.setOnAction(event -> {
            LocalDate selectedDate = monthPicker.getValue();
            if (selectedDate != null) {
                monthPicker.setValue(selectedDate.withDayOfMonth(1));
            }
        });
    }

    /**
     * Handles the "Generate Report" button action. Fetches the selected report 
     * type and the selected month from the UI, and sends a request to the server 
     * to generate the report.
     */
    @FXML
    private void onGenerateReportClicked() {
        LocalDate selectedMonth = monthPicker.getValue();
        
        // Clear data from the BarChart
        borrowingTimesBarChart.getData().clear();

        // Clear data from the PieChart
        subscriptionStatusPieChart.getData().clear();

        if (selectedMonth == null) {
        	ShowMessageController.showWarning("No Month Selected", "Please select a month to view the report.");
            return;
        }

        String selectedReport = reportChoiceBox.getValue();

        if (selectedReport == null || selectedReport.isEmpty()) {
        	ShowMessageController.showWarning("No Report Selected", "Please select a report type.");
            return;
        }

        try {
            String reportType = selectedReport.equals("Borrowing Times Report") ? "BorrowingTimes" : "SubscriptionStatus";
            ClientControllerNew.getInstance().accept(new Object[]{ClientAction.FETCH_REPORT, reportType, selectedMonth});
        } catch (Exception e) {
        	ShowMessageController.showError("Error", "Failed to fetch report. Please try again.");
        }
    }

    /**
     * Updates the Borrowing Times Report chart with the provided data.
     * 
     * @param reports A list of {@link Reports} containing the data to be visualized.
     */
    public void updateBorrowingTimesReport(List<Reports> reports) {
        Platform.runLater(() -> {
        	System.out.println(reports);
            if (reports == null || reports.isEmpty()) {
            	ShowMessageController.showInfo("No Data", "No data available for Borrowing Times Report.");
                return;
            }

            // Clear existing chart data
            borrowingTimesBarChart.getData().clear();

            // Create a series for the bar chart
            XYChart.Series<String, Number> series = new XYChart.Series<>();
            
            // Define a set of colors for the bars
            String[] colors = {"#3498db", "#e74c3c", "#f1c40f", "#2ecc71", "#9b59b6"};
            
            // Add data to the series
            int[] index = {0}; // Use an array to make the index effectively final for lambda
            for (Reports report : reports) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(report.getCategory(), report.getValue());
                series.getData().add(data);

                // Apply colors to bars dynamically
                data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                    if (newNode != null) {
                        newNode.setStyle("-fx-bar-fill: " + colors[index[0] % colors.length] + ";");
                        index[0]++;
                    }
                });
            }

            // Add the series to the chart
            borrowingTimesBarChart.getData().add(series);

            // Configure Y-axis to show natural numbers
            NumberAxis yAxis = (NumberAxis) borrowingTimesBarChart.getYAxis();
            yAxis.setAutoRanging(false);
            yAxis.setTickUnit(1); // Interval of 1
            yAxis.setForceZeroInRange(true); // Include zero
            yAxis.setLowerBound(0); // Start from 0
            yAxis.setUpperBound(getMaxYValue(reports) + 1); // Set upper bound dynamically based on data

            // Show the chart
            updateReportVisibility("Borrowing Times Report");
        });
    }

    /**
     * Helper method to get the maximum value from the reports.
     *
     * @param reports List of report data.
     * @return The maximum value of the reports or 1 if no data exists.
     */
    private int getMaxYValue(List<Reports> reports) {
        return reports.stream().mapToInt(Reports::getValue).max().orElse(1);
    }



    /**
     * Updates the Subscription Status Report chart with the provided data.
     *
     * @param reports A list of {@link Reports} containing the data to be visualized.
     */
    public void updateSubscriptionStatusReport(List<Reports> reports) {
        Platform.runLater(() -> {
        	System.out.println(reports);
            if (reports == null || reports.isEmpty()) {
            	ShowMessageController.showInfo("No Data", "No data available for Subscription Status Report.");
                return;
            }

            // Clear existing chart data
            subscriptionStatusPieChart.getData().clear();

            // Calculate the total number of subscriptions for percentage calculation
            int totalSubscriptions = reports.stream().mapToInt(Reports::getValue).sum();

            // Add data to the pie chart with percentages
            for (Reports report : reports) {
                double percentage = (report.getValue() * 100.0) / totalSubscriptions;
                String labelWithPercentage = String.format("%s (%.2f%%)", report.getCategory(), percentage);

                PieChart.Data data = new PieChart.Data(labelWithPercentage, report.getValue());
                subscriptionStatusPieChart.getData().add(data);
            }

            // Show the pie chart
            updateReportVisibility("Subscription Status Report");
        });
    }

    /**
     * Toggles the visibility of the charts based on the selected report type.
     *
     * @param selectedReport The selected report type.
     */
    private void updateReportVisibility(String selectedReport) {
        borrowingTimesBarChart.setVisible("Borrowing Times Report".equals(selectedReport));
        subscriptionStatusPieChart.setVisible("Subscription Status Report".equals(selectedReport));
    }
    
    /**
     * Handles the "Export Report" button action. Allows exporting the chart 
     * as an image or a CSV file.
     */
    @FXML
    private void onExportReportClicked() {
        String selectedReport = reportChoiceBox.getValue();

        if (selectedReport == null || selectedReport.isEmpty()) {
        	ShowMessageController.showWarning("No Report Selected", "Please select a report type.");
            return;
        }

        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Report");

        // Allow exporting as an image or CSV
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg"),
                new FileChooser.ExtensionFilter("CSV Files", "*.csv")
        );

        File selectedFile = fileChooser.showSaveDialog(null);

        if (selectedFile != null) {
            try {
                if (selectedFile.getName().endsWith(".csv")) {
                    exportReportAsCSV(selectedReport, selectedFile);
                } else {
                    exportReportAsImage(selectedReport, selectedFile);
                }
            } catch (Exception e) {
            	ShowMessageController.showError("Export Error", "Failed to export the report. Please try again.");
                e.printStackTrace();
            }
        }
    }

    /**
     * Exports the report data as a CSV file.
     * 
     * @param reportType The type of the report to be exported.
     * @param file       The file to which the report will be exported.
     * @throws Exception If an error occurs during export.
     */
    private void exportReportAsCSV(String reportType, File file) throws Exception {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write("Category,Value\n");
            if ("Borrowing Times Report".equals(reportType)) {
                for (XYChart.Series<String, Number> series : borrowingTimesBarChart.getData()) {
                    for (XYChart.Data<String, Number> data : series.getData()) {
                        // Sanitize category and export
                        String sanitizedCategory = sanitizeString(data.getXValue());
                        writer.write(sanitizedCategory + "," + data.getYValue() + "\n");
                    }
                }
            } else if ("Subscription Status Report".equals(reportType)) {
                for (PieChart.Data data : subscriptionStatusPieChart.getData()) {
                    // Sanitize category and export
                    String sanitizedCategory = sanitizeString(data.getName());
                    writer.write(sanitizedCategory + "," + data.getPieValue() + "\n");
                }
            }
        }
        ShowMessageController.showInfo("Export Successful", "The report has been exported as a CSV file.");
    }



    /**
     * Sanitizes a string by removing or replacing problematic characters.
     */
    private String sanitizeString(String input) {
        if (input == null) {
            return "";
        }
        // Replace en dash with a standard hyphen
        return input.replaceAll("–", "-") // Replace en dash with hyphen
                    .replaceAll("[“”€]", "") // Remove characters like “, ”, and €
                    .replaceAll("\\s+", " ") // Replace multiple spaces with a single space
                    .trim(); // Remove leading and trailing spaces
        
    }





    /**
     * Exports the report chart as an image file.
     * 
     * @param reportType The type of the report to be exported.
     * @param file       The file to which the chart will be exported.
     * @throws Exception If an error occurs during export.
     */
    private void exportReportAsImage(String reportType, File file) throws Exception {
        WritableImage image;

        if ("Borrowing Times Report".equals(reportType)) {
            image = borrowingTimesBarChart.snapshot(new SnapshotParameters(), null);
        } else if ("Subscription Status Report".equals(reportType)) {
            image = subscriptionStatusPieChart.snapshot(new SnapshotParameters(), null);
        } else {
            throw new Exception("Unknown report type.");
        }

        String format = file.getName().endsWith(".png") ? "png" : "jpg";
        ImageIO.write(SwingFXUtils.fromFXImage(image, null), format, file);
        ShowMessageController.showInfo("Export Successful", "The report has been exported as an image file.");
    }

    @FXML
    private void onBackToDashboardClicked() {
        try {
        	ClientControllerNew.getInstance().getChatClient().setReportsController(null);
            MainController.loadView("/views/librarianDashboard.fxml", "Librarian Dashboard");
        } catch (Exception e) {
        	ShowMessageController.showError("Navigation Error", "Failed to navigate back to the dashboard.");
        }
    }

}
