package distsys26.national_eoc.monitor_dashboard;

import distsys26.national_eoc.enums.*;
import distsys26.national_eoc.models.Alert;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;
import org.springframework.util.StringUtils;

import javafx.geometry.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.util.Duration;
import javafx.scene.Group;

public class AlertTable extends ScrollPane {
    // --- UI Configuration Constants ---
    private static final double CELL_SIZE = 20.0; // Perfect squares
    private static final double EOC_HEADER_WIDTH = 120.0; // Room for local EOC ids
    private static final double DISASTER_HEADER_HEIGHT = 90.0; // Room for disaster type labels

    // --- UI & State Variables ---
    private final GridPane gridPane = new GridPane();
    private final Set<String> localEocSet = new HashSet<>();
    
    // Quick lookups to find exact matrix positions
    private final Map<String, Integer> eocRowMap = new HashMap<>();
    private final Map<DisasterType, Integer> disasterColMap = new HashMap<>();
    // Tracks individual cell regions via a composite key: "localEocId-disasterType"
    private final Map<String, Region> cellMap = new HashMap<>();

    public AlertTable() {
        gridPane.setPadding(new Insets(10));
        gridPane.setHgap(5);
        gridPane.setVgap(5);
        gridPane.setStyle("-fx-background-color: #F4F6F7;");

        setupHeaders();

        this.setContent(gridPane);
        this.setFitToWidth(false);
        this.setFitToHeight(false);
    }

    /**
     * Initializes fixed group headers using the DisasterType Enum
     */
    private void setupHeaders() {
        // 1. Set Column Constraint for EOC Ids Header Column (Col 0)
        ColumnConstraints col0 = new ColumnConstraints(EOC_HEADER_WIDTH);
        gridPane.getColumnConstraints().add(col0);
        // 2. Set Row Constraint for Disaster Types Header Row (Row 0)
        RowConstraints row0 = new RowConstraints(DISASTER_HEADER_HEIGHT);
        gridPane.getRowConstraints().add(row0);

        // 3. Populate Column Headers from Enum (Starting at Column 1)
        DisasterType[] disasters = DisasterType.values();
        for (int i = 0; i < disasters.length; i++) {
            DisasterType disaster = disasters[i];
            int colIndex = i + 1;
            disasterColMap.put(disaster, colIndex);

            // Column constraint for status cells to enforce exact square size
            ColumnConstraints colConstraint = new ColumnConstraints(CELL_SIZE);
            gridPane.getColumnConstraints().add(colConstraint);

            // Label setup with tooltip
            String labelText = StringUtils.capitalize(disaster.toString());
            Label headerLabel = new Label(labelText);
            headerLabel.setMaxWidth(DISASTER_HEADER_HEIGHT - 5);
            headerLabel.setAlignment(Pos.CENTER);
            Tooltip tooltip = new Tooltip(labelText);
            tooltip.setShowDelay(Duration.ZERO);
            headerLabel.setTooltip(tooltip); // Full name on hover
            headerLabel.setStyle("-fx-font-weight: bold; -fx-text-fill: #34495E;");
            headerLabel.setRotate(-90); // Vertical text for compactness

            gridPane.add(new Group(headerLabel), colIndex, 0);
        }
    }

    /**
     * Triggers dynamically when a new eocId is added
     */
    private void addNewTaskRow(String eocId) {
        int rowIndex = localEocSet.size(); // To offset header row
        eocRowMap.put(eocId, rowIndex);

        // Enforce strict square height for this new row
        RowConstraints rowConstraint = new RowConstraints(CELL_SIZE);
        gridPane.getRowConstraints().add(rowConstraint);

        // Add Task Header Label (Col 0)
        Label taskLabel = new Label(eocId);
        taskLabel.setMaxWidth(EOC_HEADER_WIDTH - 10);
        Tooltip tooltip = new Tooltip(eocId);
        tooltip.setShowDelay(Duration.ZERO);
        taskLabel.setTooltip(tooltip); // Tooltip if name is too long
        taskLabel.setStyle("-fx-font-weight: bold; -fx-padding: 0 0 0 5;");
        gridPane.add(taskLabel, 0, rowIndex);

        // Fill row columns with default grey (No Info) cells
        for (Map.Entry<DisasterType, Integer> group : disasterColMap.entrySet()) {
            int colIndex = group.getValue();

            Region cell = new Region();
            updateCellColor(cell, null); // Default color setup

            Tooltip cellTooltip = new Tooltip(eocId + "\n" + StringUtils.capitalize(group.getKey().toString()));
            cellTooltip.setShowDelay(Duration.ZERO);
            Tooltip.install(cell, cellTooltip); // Tooltip for each cell

            gridPane.add(cell, colIndex, rowIndex);
            cellMap.put(eocId + "-" + group.getKey(), cell);
        }
    }

    /**
     * Call this method when consume Kafka message
     */
    public void receiveAlertMessage(Alert alert) {
        // Rule: If taskName isn't in the list, add it and handles grid instantiation.
        if (localEocSet.add(alert.getLocalEocId())) {
            addNewTaskRow(alert.getLocalEocId());
        }

        // Locate the target cell inside our matrix map and paint it
        Region targetCell = cellMap.get(alert.getLocalEocId() + "-" + alert.getDisasterType().toString());
        if (targetCell != null) {
            updateCellColor(targetCell, alert.getAlertType());
        }
    }

    /**
     * Changes background fill colors programmatically via uniform JavaFX CSS styles
     */
    private void updateCellColor(Region cell, AlertType color) {
        String hexColor = color == null? "grey" : color.toString().toLowerCase();
        cell.setStyle(String.format("-fx-background-color: %s; -fx-border-color: transparent;", hexColor));
    }
}
