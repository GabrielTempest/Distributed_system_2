package distsys26.national_eoc.view;

import distsys26.national_eoc.model.SensorNode;
import distsys26.national_eoc.model.SensorNode.NodeStatus;
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
        Circle dot; Label statusLbl, battLbl, sigLbl;
        NodeWidgets(Circle d, Label s, Label b, Label g) {
            dot = d; statusLbl = s; battLbl = b; sigLbl = g;
        }
    }

    public NodeStatusPanel() {
        setSpacing(0);
        setPadding(new Insets(14, 16, 16, 16));
        setStyle("-fx-background-color: #070e18;");
        getChildren().add(sectionHeader("NODE STATUS"));
    }

    public void loadNodes(List<SensorNode> nodes) {
        widgetMap.clear();
        getChildren().subList(1, getChildren().size()).clear();

        GridPane grid = new GridPane();
        grid.setHgap(4);
        grid.setVgap(4);
        grid.setMaxWidth(Double.MAX_VALUE);

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
        HBox card = new HBox(8);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setMaxWidth(Double.MAX_VALUE);
        card.setStyle(
            "-fx-background-color: #0c1620;" +
            "-fx-border-color: #101e2c;" +
            "-fx-border-radius: 3; -fx-background-radius: 3;"
        );

        Color sc = statusColor(node.getStatus());
        Circle dot = new Circle(4, sc);

        VBox info = new VBox(2);
        HBox.setHgrow(info, Priority.ALWAYS);

        Label nameLbl = new Label(node.getNameVn());
        nameLbl.setFont(Font.font("Monospace", FontWeight.BOLD, 11));
        nameLbl.setTextFill(Color.web("#56b6c2"));

        HBox meta = new HBox(8);
        Label battLbl = new Label("Bat:" + node.getBattery() + "%");
        battLbl.setFont(Font.font("Monospace", 9));
        battLbl.setTextFill(Color.web("#1e3848"));

        Label sigLbl = new Label("Sig:" + node.getSignal() + "%");
        sigLbl.setFont(Font.font("Monospace", 9));
        sigLbl.setTextFill(Color.web("#1e3848"));

        meta.getChildren().addAll(battLbl, sigLbl);
        info.getChildren().addAll(nameLbl, meta);

        Label statusLbl = new Label(node.getStatus().name());
        statusLbl.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        statusLbl.setTextFill(sc);

        card.getChildren().addAll(dot, info, statusLbl);
        widgetMap.put(node.getId(), new NodeWidgets(dot, statusLbl, battLbl, sigLbl));
        return card;
    }

    public void updateNode(SensorNode node) {
        NodeWidgets w = widgetMap.get(node.getId());
        if (w == null) return;
        Color c = statusColor(node.getStatus());
        w.dot.setFill(c);
        w.statusLbl.setTextFill(c);
        w.statusLbl.setText(node.getStatus().name());
        w.battLbl.setText("Bat:" + node.getBattery() + "%");
        w.sigLbl.setText("Sig:"  + node.getSignal()  + "%");
    }

    private Color statusColor(NodeStatus s) {
        return switch (s) {
            case OK      -> Color.web("#00cc66");
            case WARNING -> Color.web("#e5a840");
            case ERROR   -> Color.web("#e06c75");
            case OFFLINE -> Color.web("#444c5a");
        };
    }

    private Label sectionHeader(String text) {
        Label l = new Label(text);
        l.setFont(Font.font("Monospace", FontWeight.BOLD, 9));
        l.setTextFill(Color.web("#1e3848"));
        l.setPadding(new Insets(0, 0, 8, 0));
        return l;
    }
}
