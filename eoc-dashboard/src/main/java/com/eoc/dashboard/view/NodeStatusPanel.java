package com.eoc.dashboard.view;

import com.eoc.dashboard.model.SensorNode;
import com.eoc.dashboard.model.SensorNode.NodeStatus;
import javafx.geometry.*;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.*;

import java.util.*;

public class NodeStatusPanel extends VBox {

    private final Map<String, NodeWidgets> widgetMap = new HashMap<>();

    private static class NodeWidgets {
        Circle dot;
        Label statusLbl, battLbl, sigLbl, nameLbl;
        NodeWidgets(Circle d, Label s, Label b, Label g, Label n) {
            dot = d; statusLbl = s; battLbl = b; sigLbl = g; nameLbl = n;
        }
    }

    public NodeStatusPanel() {
        setSpacing(0);
        setPadding(new Insets(20, 20, 20, 20));
        setStyle("-fx-background-color: #0a1628;");
        getChildren().add(sectionHeader("NODE STATUS"));
    }

    public void loadNodes(List<SensorNode> nodes) {
        widgetMap.clear();
        getChildren().subList(1, getChildren().size()).clear();

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setMaxWidth(Double.MAX_VALUE);

        ColumnConstraints col0 = new ColumnConstraints();
        col0.setPercentWidth(50);
        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        grid.getColumnConstraints().addAll(col0, col1);

        int col = 0, row = 0;
        for (SensorNode node : nodes) {
            HBox card = buildCard(node);
            GridPane.setHgrow(card, Priority.ALWAYS);
            grid.add(card, col, row);
            col++;
            if (col == 2) { col = 0; row++; }
        }
        getChildren().add(grid);
    }

    private HBox buildCard(SensorNode node) {
        HBox card = new HBox(10);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(10, 14, 10, 14));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
            "-fx-background-color: #0d1e2e;" +
            "-fx-border-color: #1a2e42;" +
            "-fx-border-radius: 6; -fx-background-radius: 6; -fx-border-width: 1;"
        );

        Color sc = statusColor(node.getStatus());
        Circle dot = new Circle(5, sc);

        VBox info = new VBox(3);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLbl = new Label(node.getNameVn());
        nameLbl.setFont(Font.font("Monospace", FontWeight.BOLD, 12));
        nameLbl.setTextFill(Color.web("#7ab8cc"));

        HBox meta = new HBox(10);
        Label battLbl = new Label("Battery: " + node.getBattery() + "%");
        battLbl.setFont(Font.font("Monospace", 10));
        battLbl.setTextFill(Color.web("#5a8090"));

        Label sigLbl = new Label("Signal: " + node.getSignal() + "%");
        sigLbl.setFont(Font.font("Monospace", 10));
        sigLbl.setTextFill(Color.web("#5a8090"));

        meta.getChildren().addAll(battLbl, sigLbl);
        info.getChildren().addAll(nameLbl, meta);

        Label statusLbl = new Label(node.getStatus().name());
        statusLbl.setFont(Font.font("Monospace", FontWeight.BOLD, 10));
        statusLbl.setTextFill(sc);
        statusLbl.setPadding(new Insets(2, 7, 2, 7));
        statusLbl.setStyle(
            "-fx-background-color: " + toHex(sc) + "22;" +
            "-fx-border-color: "     + toHex(sc) + "55;" +
            "-fx-border-radius: 3; -fx-background-radius: 3;"
        );

        card.getChildren().addAll(dot, info, statusLbl);
        widgetMap.put(node.getId(), new NodeWidgets(dot, statusLbl, battLbl, sigLbl, nameLbl));
        return card;
    }

    public void updateNode(SensorNode node) {
        NodeWidgets w = widgetMap.get(node.getId());
        if (w == null) return;
        Color c = statusColor(node.getStatus());
        w.dot.setFill(c);
        w.statusLbl.setTextFill(c);
        w.statusLbl.setText(node.getStatus().name());
        w.statusLbl.setStyle(
            "-fx-background-color: " + toHex(c) + "22;" +
            "-fx-border-color: "     + toHex(c) + "55;" +
            "-fx-border-radius: 3; -fx-background-radius: 3;"
        );
        w.battLbl.setText("Battery: " + node.getBattery() + "%");
        w.sigLbl.setText("Signal: "   + node.getSignal()  + "%");
    }

    private Color statusColor(NodeStatus s) {
        return switch (s) {
            case OK      -> Color.web("#00cc66");
            case WARNING -> Color.web("#e5a840");
            case ERROR   -> Color.web("#e06c75");
            case OFFLINE -> Color.web("#6a7a8a");
        };
    }

    private String toHex(Color c) {
        return String.format("#%02x%02x%02x",
            (int)(c.getRed()   * 255),
            (int)(c.getGreen() * 255),
            (int)(c.getBlue()  * 255));
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        l.setTextFill(Color.web("#4a8fa8"));
        l.setPadding(new Insets(0, 0, 10, 0));
        return l;
    }
}
