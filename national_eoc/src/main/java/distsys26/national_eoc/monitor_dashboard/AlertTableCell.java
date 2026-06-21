package distsys26.national_eoc.monitor_dashboard;

import distsys26.national_eoc.enums.AlertType;

import javafx.scene.layout.Region;
import javafx.util.Duration;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.control.Tooltip;
import javafx.application.Platform;

public class AlertTableCell extends Region {
    /**
     * Duration to show alert status before resetting to "No Info"
     */
    private static final int STATUS_LIVE_DURATION = 5000;
    private String rowId;
    private String colId;
    private String rowColKey;
    private Timeline timeline;
    private Tooltip tooltip;
    private String noInfoColor = "grey"; // Default color for "No Info" state

    public AlertTableCell(String rowId, String colId) {
        this.rowId = rowId;
        this.colId = colId;
        this.rowColKey = rowId + "-" + colId;
        updateCellColor(); // Set initial color to "No Info"
        installTooltip(rowId + "\n" + colId);
        installAutoResetColor();
    }

    /**
     * Updates the cell color based on the alert type.
     * @param color
     */
    public void updateCellColor(AlertType color) {
        updateCellColor(color.toString().toLowerCase());
    }

    /**
     * Resets the cell color to "No Info" state.
     */
    public void updateCellColor() {
        updateCellColor(noInfoColor);
    }

    public String getRowId() {
        return rowId;
    }

    public String getColId() {
        return colId;
    }

    public String getRowColKey() {
        return rowColKey;
    }




    private void updateCellColor(String color) {
        Platform.runLater(() -> {
            // Stop the existing countdown
            timeline.stop();
            this.setStyle(String.format("-fx-background-color: %s; -fx-border-color: transparent;", color));
            // Start/Restart the countdown from zero
            timeline.playFromStart();
        });
    }

    private void installTooltip(String tooltipText) {
        if (tooltip != null) {
            Tooltip.uninstall(this, tooltip);
        }
        tooltip = new Tooltip(tooltipText);
        tooltip.setShowDelay(Duration.ZERO);
        Tooltip.install(this, tooltip);
    }

    private void installAutoResetColor() {
        timeline = new Timeline(new KeyFrame(Duration.millis(STATUS_LIVE_DURATION), event -> {
            updateCellColor();
        }));
        timeline.setCycleCount(1); // Only run once per trigger
    }
    
}
